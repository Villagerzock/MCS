package net.villagerzock.compiler.ast.decl;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.semantic.Symbol;
import net.villagerzock.compiler.ast.type.TypeNode;

public final class ParameterDeclaration extends AstNode implements Declaration {
	private Symbol resolvedSymbol;
	private final TypeNode type;
	private final String name;

	public ParameterDeclaration(TypeNode type, String name) {
		this(type, name, SourceRange.UNKNOWN);
	}

	public ParameterDeclaration(TypeNode type, String name, SourceRange sourceRange) {
		super(sourceRange);
		this.type = type;
		this.name = name;
	}

	public TypeNode type() {
		return type;
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

	@Override
	public String getString() {
		return "Parameter(" + name() + ")";
	}
}
