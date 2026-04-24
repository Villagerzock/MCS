package net.villagerzock.compiler.ast.type;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;

public final class TypeNode extends AstNode {
	private final String name;

	public TypeNode(String name) {
		this(name, SourceRange.UNKNOWN);
	}

	public TypeNode(String name, SourceRange sourceRange) {
		super(sourceRange);
		this.name = name;
	}

	public String name() {
		return name;
	}


	@Override
	public String getString() {
		return "Type(" + name() + ")";
	}
}
