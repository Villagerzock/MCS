package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;

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


	@Override
	public String getString() {
		return "Binary(" + operator().name() + ")";
	}
}
