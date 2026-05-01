package net.villagerzock.compiler.ast.decl;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.semantic.Symbol;
import net.villagerzock.compiler.ast.expr.Expression;
import net.villagerzock.compiler.ast.type.TypeNode;
import net.villagerzock.mcfunction.MCFunction;

public final class FieldDeclaration extends AstNode implements Declaration {
	private Symbol resolvedSymbol;
	private final TypeNode type;
	private final String name;
	private final Expression initializer;
	private MCFunction getter;
	private MCFunction setter;

	public FieldDeclaration(TypeNode type, String name, Expression initializer) {
		this(type, name, initializer, SourceRange.UNKNOWN);
	}

	public FieldDeclaration(TypeNode type, String name, Expression initializer, SourceRange sourceRange) {
		super(sourceRange);
		this.type = type;
		this.name = name;
		this.initializer = initializer;
	}

	public TypeNode type() {
		return type;
	}

	public String name() {
		return name;
	}

	public Expression initializer() {
		return initializer;
	}


	public Symbol resolvedSymbol() {
		return resolvedSymbol;
	}

	public void setResolvedSymbol(Symbol resolvedSymbol) {
		this.resolvedSymbol = resolvedSymbol;
	}

	public MCFunction getGetter() {
		return getter;
	}

	public MCFunction getSetter() {
		return setter;
	}

	public void setGetter(MCFunction getter) {
		this.getter = getter;
	}

	public void setSetter(MCFunction setter) {
		this.setter = setter;
	}

	@Override
	public String getString() {
		return "Field(" + name() + ")";
	}
}
