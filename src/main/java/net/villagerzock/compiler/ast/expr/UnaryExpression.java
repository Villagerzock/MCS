package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.semantic.SemanticType;

public final class UnaryExpression extends AstNode implements Expression {
	private SemanticType resolvedType;
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


	public SemanticType resolvedType() {
		return resolvedType;
	}

	public void setResolvedType(SemanticType resolvedType) {
		this.resolvedType = resolvedType;
	}

	@Override
	public String getString() {
		return "Unary(" + operator().name() + ")";
	}
}
