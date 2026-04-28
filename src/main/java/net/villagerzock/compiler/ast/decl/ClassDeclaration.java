package net.villagerzock.compiler.ast.decl;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;

import java.util.List;

public final class ClassDeclaration extends AstNode implements Declaration {
	private final String name;
	private final List<Declaration> members;

	public ClassDeclaration(String name, List<Declaration> members) {
		this(name, members, SourceRange.UNKNOWN);
	}

	public ClassDeclaration(String name, List<Declaration> members, SourceRange sourceRange) {
		super(sourceRange);
		this.name = name;
		this.members = List.copyOf(members);
	}

	public String name() {
		return name;
	}

	public List<Declaration> members() {
		return members;
	}


	@Override
	public String getString() {
		return "Class(" + name() + ")";
	}

    public String getCanonnicalName() {
        return "";
    }
}
