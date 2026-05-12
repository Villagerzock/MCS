package net.villagerzock.compiler.ast.decl;

import net.villagerzock.compiler.ast.SourceRange;

import java.util.List;

public final class RecordDeclaration extends ClassDeclaration {
	private final List<ParameterDeclaration> components;

	public RecordDeclaration(String name, List<ParameterDeclaration> components, List<Declaration> members) {
		this(name, components, members, SourceRange.UNKNOWN);
	}

	public RecordDeclaration(String name, List<ParameterDeclaration> components, List<Declaration> members, SourceRange sourceRange) {
		super(name, members, sourceRange);
		this.components = List.copyOf(components);
	}

	public List<ParameterDeclaration> components() {
		return components;
	}

	@Override
	public String getString() {
		return "Record(" + name() + ")";
	}
}
