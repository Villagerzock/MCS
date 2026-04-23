package mylang.ast.expr;

import mylang.ast.AstNode;
import mylang.ast.SourceRange;

public final class GroupExpression extends AstNode implements Expression {
	private final Expression expression;

	public GroupExpression(Expression expression) {
		this(expression, SourceRange.UNKNOWN);
	}

	public GroupExpression(Expression expression, SourceRange sourceRange) {
		super(sourceRange);
		this.expression = expression;
	}

	public Expression expression() {
		return expression;
	}
}
