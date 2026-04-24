package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.semantic.SemanticType;

public final class AssignmentExpression extends AstNode implements Expression {
	private SemanticType resolvedType;
	private final Expression target;
	private final Expression value;

	public AssignmentExpression(Expression target, Expression value) {
		this(target, value, SourceRange.UNKNOWN);
	}

	public AssignmentExpression(Expression target, Expression value, SourceRange sourceRange) {
		super(sourceRange);
		this.target = target;
		this.value = value;
	}

	public Expression target() {
		return target;
	}

	public Expression value() {
		return value;
	}


	public SemanticType resolvedType() {
		return resolvedType;
	}

	public void setResolvedType(SemanticType resolvedType) {
		this.resolvedType = resolvedType;
	}

	@Override
	public String getString() {
		return "Assignment";
	}
}
