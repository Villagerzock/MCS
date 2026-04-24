package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;

public final class UnaryExpression extends AstNode implements Expression {
	private final UnaryOperator operator;
	private final Expression operand;

	public UnaryExpression(UnaryOperator operator, Expression operand) {
		this(operator, operand, SourceRange.UNKNOWN);
	}

	public UnaryExpression(UnaryOperator operator, Expression operand, SourceRange sourceRange) {
		super(sourceRange);
		this.operator = operator;
		this.operand = operand;
	}

	public UnaryOperator operator() {
		return operator;
	}

	public Expression operand() {
		return operand;
	}


	@Override
	public String getString() {
		return "Unary(" + operator().name() + ")";
	}
}
