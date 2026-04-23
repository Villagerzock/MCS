package mylang.ast;

public abstract class AstNode implements Node {
	private final SourceRange sourceRange;

	protected AstNode() {
		this(SourceRange.UNKNOWN);
	}

	protected AstNode(SourceRange sourceRange) {
		this.sourceRange = sourceRange == null ? SourceRange.UNKNOWN : sourceRange;
	}

	public SourceRange sourceRange() {
		return sourceRange;
	}
}
