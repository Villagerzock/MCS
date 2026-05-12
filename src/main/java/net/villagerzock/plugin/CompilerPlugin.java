package net.villagerzock.plugin;

import net.villagerzock.compiler.ast.CompilationUnit;

public interface CompilerPlugin {
    String getName();
    void init();
    void onTaskFinished(TaskType task, CompilationUnit compilationUnit);
    void onTaskStarted(TaskType task, CompilationUnit compilationUnit);
}
