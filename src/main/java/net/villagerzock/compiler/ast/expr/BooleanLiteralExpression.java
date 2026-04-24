package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.semantic.SemanticType;

public final class BooleanLiteralExpression extends AstNode implements Expression {
	private SemanticType resolvedType;
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


	public SemanticType resolvedType() {
		return resolvedType;
	}

	public void setResolvedType(SemanticType resolvedType) {
		this.resolvedType = resolvedType;
	}

	@Override
	public String getString() {
		return "BooleanLiteral(" + value() + ")";
	}
}
