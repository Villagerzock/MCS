package net.villagerzock.mcfunction;

import java.util.ArrayList;
import java.util.List;

public class MCFunction {
    private final String path;
    private final String name;
    private final String namespace;
    private String finalName;
    private List<ICommandPart> commands = new ArrayList<>();

    public MCFunction(String namespace, String path, String name) {
        this.path = path;
        this.name = name;
        this.namespace = namespace;
    }
    public String getOriginalName(){
        return name;
    }

    public String getPath() {
        return path;
    }

    public void updateFinalName(String finalName) {
        this.finalName = finalName;
    }

    public String getName() {
        if (finalName != null){
            return finalName;
        }
        throw new IllegalStateException("Trying to read Function name when Function before Semantic Resolving");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (ICommandPart p : commands){
            builder.append(p.apply(namespace));
            builder.append("\n");
        }
        return builder.toString();
    }

    public void addCommand(ICommandPart part){
        commands.add(part);
    }

    public String getFullPath() {
        return "%s:%s%s".formatted(namespace,path,finalName);
    }

    public String getNamespace() {
        return namespace;
    }

    public String getOriginalFullPath() {
        return "%s:%s%s".formatted(namespace,path,name);
    }
}
