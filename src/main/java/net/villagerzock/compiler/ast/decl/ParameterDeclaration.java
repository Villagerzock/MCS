package mylang.ast.decl;

import mylang.ast.AstNode;
import mylang.ast.SourceRange;
import mylang.ast.type.TypeNode;

public final class ParameterDeclaration extends AstNode implements Declaration {
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
}
