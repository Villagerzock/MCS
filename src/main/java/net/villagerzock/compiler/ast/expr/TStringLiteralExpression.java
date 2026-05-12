package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.semantic.SemanticType;

import java.util.List;

public final class TStringLiteralExpression extends AstNode implements Expression {
	private SemanticType resolvedType;
	private final String rawValue;
	private String analyzedText;
	private List<Expression> inlineExpressions = List.of();

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

	public String analyzedText() {
		return analyzedText == null ? rawValue : analyzedText;
	}

	public void setAnalyzedTemplate(String analyzedText, List<Expression> inlineExpressions) {
		this.analyzedText = analyzedText;
		this.inlineExpressions = List.copyOf(inlineExpressions);
	}

	public List<Expression> inlineExpressions() {
		return inlineExpressions;
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
