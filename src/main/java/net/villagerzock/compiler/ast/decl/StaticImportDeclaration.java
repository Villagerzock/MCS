package net.villagerzock.compiler.ast.decl;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;

public final class StaticImportDeclaration extends AstNode {
	private final QualifiedPathNode path;

	public StaticImportDeclaration(QualifiedPathNode path) {
		this(path, SourceRange.UNKNOWN);
	}

	public StaticImportDeclaration(QualifiedPathNode path, SourceRange sourceRange) {
		super(sourceRange);
		this.path = path;
	}

	public QualifiedPathNode path() {
		return path;
	}


	@Override
	public String getString() {
		return "Import";
	}
}
