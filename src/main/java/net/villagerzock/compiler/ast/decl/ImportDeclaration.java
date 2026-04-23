package mylang.ast.decl;

import mylang.ast.AstNode;
import mylang.ast.SourceRange;

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
}
