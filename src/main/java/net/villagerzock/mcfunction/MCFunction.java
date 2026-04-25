package net.villagerzock.mcfunction;

import net.villagerzock.Main;

import java.util.ArrayList;
import java.util.List;

public class MCFunction {
    private final String path;
    private final String name;
    private final String namespace;
    private String finalName;
    private final List<ICommandPart> commands = new ArrayList<>();

    public MCFunction(String namespace, String path, String name) {
        this.path = toSnakeCase(Main.runtimeData.obfuscate ? "" : path);
        this.name = toSnakeCase(Main.runtimeData.obfuscate ? "function" : name);
        this.namespace = toSnakeCase(namespace);
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
        builder.append("# ");
        builder.append("Compiled With MCS Compiler by Villagerzock - https://github.com/Villagerzock/MCS");
        builder.append("\n");
        for (ICommandPart p : commands){
            builder.append(p.apply());
            builder.append("\n");
        }
        return builder.toString();
    }

    public void addCommand(ICommandPart part){
        commands.add(part);
    }

    public String getFullPath() {
        return "%s:%s%s".formatted(namespace,path,getName());
    }

    public String getNamespace() {
        return namespace;
    }

    public String getOriginalFullPath() {
        return "%s:%s%s".formatted(namespace,path,name);
    }


    public static String toSnakeCase(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    char prev = s.charAt(i - 1);

                    if (Character.isLetterOrDigit(prev)) {
                        result.append('_');
                    }
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}
