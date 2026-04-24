package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.semantic.SemanticType;

public final class GroupExpression extends AstNode implements Expression {
	private SemanticType resolvedType;
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


	public SemanticType resolvedType() {
		return resolvedType;
	}

	public void setResolvedType(SemanticType resolvedType) {
		this.resolvedType = resolvedType;
	}

	@Override
	public String getString() {
		return "Group";
	}
}
