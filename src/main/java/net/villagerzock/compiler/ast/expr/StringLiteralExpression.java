package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;

public final class StringLiteralExpression extends AstNode implements Expression {
	private final String rawValue;

	public StringLiteralExpression(String rawValue) {
		this(rawValue, SourceRange.UNKNOWN);
	}

	public StringLiteralExpression(String rawValue, SourceRange sourceRange) {
		super(sourceRange);
		this.rawValue = rawValue;
	}

	public String rawValue() {
		return rawValue;
	}


	@Override
	public String getString() {
		return "StringLiteral(\"" + rawValue() + "\")";
	}
}
