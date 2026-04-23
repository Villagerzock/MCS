package mylang.ast.decl;

import mylang.ast.AstNode;
import mylang.ast.SourceRange;

import java.util.List;

public final class ProgramNode extends AstNode implements Declaration {
	private final QualifiedPathNode packagePath;
	private final List<ImportDeclaration> imports;
	private final List<ClassDeclaration> classes;

	public ProgramNode(QualifiedPathNode packagePath, List<ImportDeclaration> imports, List<ClassDeclaration> classes) {
		this(packagePath, imports, classes, SourceRange.UNKNOWN);
	}

	public ProgramNode(QualifiedPathNode packagePath, List<ImportDeclaration> imports, List<ClassDeclaration> classes, SourceRange sourceRange) {
		super(sourceRange);
		this.packagePath = packagePath;
		this.imports = List.copyOf(imports);
		this.classes = List.copyOf(classes);
	}

	public QualifiedPathNode packagePath() {
		return packagePath;
	}

	public List<ImportDeclaration> imports() {
		return imports;
	}

	public List<ClassDeclaration> classes() {
		return classes;
	}
}
