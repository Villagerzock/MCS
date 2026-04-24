package net.villagerzock.compiler.ast.type;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.semantic.SemanticType;
import net.villagerzock.compiler.ast.decl.ClassDeclaration;

public final class TypeNode extends AstNode {
	private final String name;
	private SemanticType resolvedType;
	private ClassDeclaration resolvedClass;

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


	public SemanticType resolvedType() {
		return resolvedType;
	}

	public void setResolvedType(SemanticType resolvedType) {
		this.resolvedType = resolvedType;
	}

	public ClassDeclaration resolvedClass() {
		return resolvedClass;
	}

	public void setResolvedClass(ClassDeclaration resolvedClass) {
		this.resolvedClass = resolvedClass;
	}

	@Override
	public String getString() {
		return "Type(" + name() + ")";
	}
}
