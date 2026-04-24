package net.villagerzock.compiler.ast.decl;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;

public final class ImportDeclaration extends AstNode {
	private final QualifiedPathNode path;

	public ImportDeclaration(QualifiedPathNode path) {
		this(path, SourceRange.UNKNOWN);
	}

	public ImportDeclaration(QualifiedPathNode path, SourceRange sourceRange) {
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
