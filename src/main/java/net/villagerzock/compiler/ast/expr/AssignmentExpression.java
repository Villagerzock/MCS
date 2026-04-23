package mylang.ast.expr;

import mylang.ast.AstNode;
import mylang.ast.SourceRange;

public final class AssignmentExpression extends AstNode implements Expression {
	private final Expression target;
	private final Expression value;

	public AssignmentExpression(Expression target, Expression value) {
		this(target, value, SourceRange.UNKNOWN);
	}

	public AssignmentExpression(Expression target, Expression value, SourceRange sourceRange) {
		super(sourceRange);
		this.target = target;
		this.value = value;
	}

	public Expression target() {
		return target;
	}

	public Expression value() {
		return value;
	}
}
