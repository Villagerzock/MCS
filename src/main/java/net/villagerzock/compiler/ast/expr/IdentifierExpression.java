package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;

public final class IdentifierExpression extends AstNode implements Expression {
	private final String name;

	public IdentifierExpression(String name) {
		this(name, SourceRange.UNKNOWN);
	}

	public IdentifierExpression(String name, SourceRange sourceRange) {
		super(sourceRange);
		this.name = name;
	}

	public String name() {
		return name;
	}


	@Override
	public String getString() {
		return "Identifier(" + name() + ")";
	}
}
