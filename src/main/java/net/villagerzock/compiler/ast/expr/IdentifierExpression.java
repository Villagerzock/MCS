package mylang.ast.expr;

import mylang.ast.AstNode;
import mylang.ast.SourceRange;

public final class IdentifierExpression extends AstNode implements Expression {
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
}
