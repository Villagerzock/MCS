package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;

public final class BooleanLiteralExpression extends AstNode implements Expression {
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


	@Override
	public String getString() {
		return "BooleanLiteral(" + value() + ")";
	}
}
