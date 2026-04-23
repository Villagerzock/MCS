package mylang.ast.stmt;

import mylang.ast.AstNode;
import mylang.ast.SourceRange;
import mylang.ast.expr.Expression;

public final class ReturnStatement extends AstNode implements Statement {
	private final Expression value;

	public ReturnStatement(Expression value) {
		this(value, SourceRange.UNKNOWN);
	}

	public ReturnStatement(Expression value, SourceRange sourceRange) {
		super(sourceRange);
		this.value = value;
	}

	public Expression value() {
		return value;
	}
}
