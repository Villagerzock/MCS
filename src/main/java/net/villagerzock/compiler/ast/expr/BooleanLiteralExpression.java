package mylang.ast.expr;

import mylang.ast.AstNode;
import mylang.ast.SourceRange;

public final class BooleanLiteralExpression extends AstNode implements Expression {
	private final boolean value;

	public BooleanLiteralExpression(boolean value) {
		this(value, SourceRange.UNKNOWN);
	}

	public BooleanLiteralExpression(boolean value, SourceRange sourceRange) {
		super(sourceRange);
		this.value = value;
	}

	public boolean value() {
		return value;
	}
}
