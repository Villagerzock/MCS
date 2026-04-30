package net.villagerzock.compiler.ast.decl;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;

import java.util.ArrayList;
import java.util.List;

public final class ProgramNode extends AstNode implements Declaration {
	private final QualifiedPathNode packagePath;
	private final List<ImportDeclaration> imports;
	private final List<StaticImportDeclaration> staticImports;
	private final List<ClassDeclaration> classes;
	private final boolean isLib;
	public ProgramNode(QualifiedPathNode packagePath, List<ClassDeclaration> classes){
		this.packagePath = packagePath;
		this.imports = new ArrayList<>();
		this.staticImports = new ArrayList<>();
		this.classes = classes;
		this.isLib = true;
	}
	public ProgramNode(QualifiedPathNode packagePath, List<ImportDeclaration> imports, List<ClassDeclaration> classes, List<StaticImportDeclaration> staticImports) {
		this(packagePath, imports, classes, SourceRange.UNKNOWN, staticImports);
	}

	public ProgramNode(QualifiedPathNode packagePath, List<ImportDeclaration> imports, List<ClassDeclaration> classes, SourceRange sourceRange, List<StaticImportDeclaration> staticImports) {
		super(sourceRange);
		this.packagePath = packagePath;
		this.imports = List.copyOf(imports);
		this.classes = List.copyOf(classes);
        this.staticImports = staticImports;
		this.isLib = false;
    }

	public QualifiedPathNode packagePath() {
		return packagePath;
	}

	public List<ImportDeclaration> imports() {
		return imports;
	}

	public List<StaticImportDeclaration> staticImports() {
		return staticImports;
	}

	public List<ClassDeclaration> classes() {
		return classes;
	}

	public boolean isLib() {
		return isLib;
	}

	@Override
	public String getString() {
		return "Program";
	}
}
