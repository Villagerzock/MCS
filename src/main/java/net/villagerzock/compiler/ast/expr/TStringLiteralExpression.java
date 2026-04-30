package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.semantic.SemanticType;

public final class TStringLiteralExpression extends AstNode implements Expression {
	private SemanticType resolvedType;
	private final String rawValue;

	public TStringLiteralExpression(String rawValue) {
		this(rawValue, SourceRange.UNKNOWN);
	}

	public TStringLiteralExpression(String rawValue, SourceRange sourceRange) {
		super(sourceRange);
		this.rawValue = rawValue;
	}

	public String rawValue() {
		return rawValue;
	}


	public SemanticType resolvedType() {
		return resolvedType;
	}

	public void setResolvedType(SemanticType resolvedType) {
		this.resolvedType = resolvedType;
	}

	@Override
	public String getString() {
		return "StringLiteral(\"" + rawValue() + "\")";
	}
}
