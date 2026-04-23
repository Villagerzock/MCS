package mylang.ast.stmt;

import mylang.ast.AstNode;
import mylang.ast.SourceRange;
import mylang.ast.expr.Expression;

public final class ExpressionStatement extends AstNode implements Statement {
	private final Expression expression;

	public ExpressionStatement(Expression expression) {
		this(expression, SourceRange.UNKNOWN);
	}

	public ExpressionStatement(Expression expression, SourceRange sourceRange) {
		super(sourceRange);
		this.expression = expression;
	}

	public Expression expression() {
		return expression;
	}
}
