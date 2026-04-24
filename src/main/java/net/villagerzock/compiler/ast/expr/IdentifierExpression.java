package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.semantic.SemanticType;
import net.villagerzock.compiler.semantic.Symbol;

public final class IdentifierExpression extends AstNode implements Expression {
	private SemanticType resolvedType;
	private Symbol resolvedSymbol;
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


	public Symbol resolvedSymbol() {
		return resolvedSymbol;
	}

	public void setResolvedSymbol(Symbol resolvedSymbol) {
		this.resolvedSymbol = resolvedSymbol;
	}

	public SemanticType resolvedType() {
		return resolvedType;
	}

	public void setResolvedType(SemanticType resolvedType) {
		this.resolvedType = resolvedType;
	}

	@Override
	public String getString() {
		return "Identifier(" + name() + ")";
	}
}
