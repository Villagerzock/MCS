package mylang.ast.decl;

import mylang.ast.AstNode;
import mylang.ast.SourceRange;

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
}
