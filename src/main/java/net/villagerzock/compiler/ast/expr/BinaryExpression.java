package mylang.ast.expr;

import mylang.ast.AstNode;
import mylang.ast.SourceRange;

public final class BinaryExpression extends AstNode implements Expression {
	private final Expression left;
	private final BinaryOperator operator;
	private final Expression right;

	public BinaryExpression(Expression left, BinaryOperator operator, Expression right) {
		this(left, operator, right, SourceRange.UNKNOWN);
	}

	public BinaryExpression(Expression left, BinaryOperator operator, Expression right, SourceRange sourceRange) {
		super(sourceRange);
		this.left = left;
		this.operator = operator;
		this.right = right;
	}

	public Expression left() {
		return left;
	}

	public BinaryOperator operator() {
		return operator;
	}

	public Expression right() {
		return right;
	}
}
