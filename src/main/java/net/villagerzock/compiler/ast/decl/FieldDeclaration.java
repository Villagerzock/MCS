package mylang.ast.decl;

import mylang.ast.AstNode;
import mylang.ast.SourceRange;
import mylang.ast.expr.Expression;
import mylang.ast.type.TypeNode;

public final class FieldDeclaration extends AstNode implements Declaration {
	private final TypeNode type;
	private final String name;
	private final Expression initializer;

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
}
