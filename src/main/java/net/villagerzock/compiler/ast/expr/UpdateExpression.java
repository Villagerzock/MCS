package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.semantic.SemanticType;

public final class UpdateExpression extends AstNode implements Expression {
	private SemanticType resolvedType;
	private final Expression target;
	private final UpdateOperator operator;

	public UpdateExpression(Expression target, UpdateOperator operator) {
		this(target, operator, SourceRange.UNKNOWN);
	}

	public UpdateExpression(Expression target, UpdateOperator operator, SourceRange sourceRange) {
		super(sourceRange);
		this.target = target;
		this.operator = operator;
	}

	public Expression target() {
		return target;
	}

	public UpdateOperator operator() {
		return operator;
	}

	public SemanticType resolvedType() {
		return resolvedType;
	}

	public void setResolvedType(SemanticType resolvedType) {
		this.resolvedType = resolvedType;
	}

	@Override
	public String getString() {
		return "Update(" + operator().name() + ")";
	}
}
