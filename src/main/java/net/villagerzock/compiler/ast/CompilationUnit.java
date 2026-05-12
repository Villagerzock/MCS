package net.villagerzock.compiler.ast;

import net.villagerzock.compiler.ast.decl.ProgramNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class CompilationUnit extends AstNode {
    private final List<ProgramNode> programs = new ArrayList<>();

    public CompilationUnit() {
        super(SourceRange.UNKNOWN);
    }

    public CompilationUnit(Collection<ProgramNode> programs) {
        this();
        addPrograms(programs);
    }

    public void addProgram(ProgramNode program) {
        programs.add(program);
    }

    public void addPrograms(Collection<ProgramNode> programs) {
        this.programs.addAll(programs);
    }

    public void addCompilationUnit(CompilationUnit compilationUnit) {
        addPrograms(compilationUnit.programs());
    }

    public void setPrograms(Collection<ProgramNode> programs) {
        this.programs.clear();
        this.programs.addAll(programs);
    }

    public List<ProgramNode> programs() {
        return List.copyOf(programs);
    }

    public List<ProgramNode> sourcePrograms() {
        return programs.stream()
                .filter(program -> !program.isLib())
                .toList();
    }

    public List<ProgramNode> libraryPrograms() {
        return programs.stream()
                .filter(ProgramNode::isLib)
                .toList();
    }

    @Override
    public String getString() {
        return "CompilationUnit(" + programs.size() + " programs)";
    }
}
