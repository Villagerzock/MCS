package net.villagerzock.compiler.ast.decl;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;

import java.util.List;

public final class ProgramNode extends AstNode implements Declaration {
	private final QualifiedPathNode packagePath;
	private final List<ImportDeclaration> imports;
	private final List<StaticImportDeclaration> staticImports;
	private final List<ClassDeclaration> classes;

	public ProgramNode(QualifiedPathNode packagePath, List<ImportDeclaration> imports, List<ClassDeclaration> classes, List<StaticImportDeclaration> staticImports) {
		this(packagePath, imports, classes, SourceRange.UNKNOWN, staticImports);
	}

	public ProgramNode(QualifiedPathNode packagePath, List<ImportDeclaration> imports, List<ClassDeclaration> classes, SourceRange sourceRange, List<StaticImportDeclaration> staticImports) {
		super(sourceRange);
		this.packagePath = packagePath;
		this.imports = List.copyOf(imports);
		this.classes = List.copyOf(classes);
        this.staticImports = staticImports;
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


	@Override
	public String getString() {
		return "Program";
	}
}
