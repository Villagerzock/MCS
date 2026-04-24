package net.villagerzock.mcfunction;

import java.util.*;

public class MCFunctionUnit {
    private final List<MCFunction> functions = new ArrayList<>();

    public MCFunctionUnit() {
    }

    public MCFunction create(String namespace,String path, String name){
        MCFunction function = new MCFunction(namespace,path,name);
        functions.add(function);
        return function;
    }

    public List<MCFunction> getFunctions() {
        return functions;
    }

    private static final class AnalyzeData {
        private final MCFunction first;
        private int count;

        private AnalyzeData(MCFunction first) {
            this.first = first;
            this.count = 0;
        }

        public MCFunction first() {
            return first;
        }

        public boolean wasFirst() {
            return count == 0;
        }
        public int getCount(){
            return count;
        }
    }
    public void analyze() {
        Map<String,AnalyzeData> dataMap = new HashMap<>();
        for (MCFunction f : functions){
            f.updateFinalName(f.getOriginalName());
            if (dataMap.containsKey(f.getOriginalFullPath())){
                AnalyzeData data = dataMap.get(f.getOriginalFullPath());
                if (data.wasFirst()){
                    data.first.updateFinalName(data.first.getOriginalName() + "$0");
                }
                data.count++;
                f.updateFinalName(f.getOriginalName() + "$" + data.count);

            }else {
                dataMap.put(f.getOriginalFullPath(),new AnalyzeData(f));
            }
        }
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (MCFunction f : functions){
            builder.append("\n");
            builder.append(f.getFullPath());
            builder.append(":\n");
            builder.append(f.toString());
        }
        return builder.toString();
    }
}
