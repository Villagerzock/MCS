package net.villagerzock.plugin;

import net.villagerzock.Main;
import net.villagerzock.compiler.ast.CompilationUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class PluginSystem {
    private static final List<CompilerPlugin> compilerPlugins = new ArrayList<>();
    public static void collectPlugins(){
        ServiceLoader<CompilerPlugin> serviceLoader = ServiceLoader.load(CompilerPlugin.class);
        for (CompilerPlugin compilerPlugin : serviceLoader) {
            for (String s : Main.runtimeData.plugins){
                if (s.equals(compilerPlugin.getName())){
                    compilerPlugins.add(compilerPlugin);
                    break;
                }
            }
        }
    }
    public static void onTaskFinished(TaskType task, CompilationUnit compilationUnit) {
        for (CompilerPlugin compilerPlugin : compilerPlugins) {
            compilerPlugin.onTaskFinished(task, compilationUnit);
        }
    }
    public static void onTaskStarted(TaskType task, CompilationUnit compilationUnit) {
        for (CompilerPlugin compilerPlugin : compilerPlugins) {
            compilerPlugin.onTaskStarted(task, compilationUnit);
        }
    }
}
