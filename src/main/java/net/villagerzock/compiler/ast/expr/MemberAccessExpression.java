package mylang.ast.expr;

import mylang.ast.AstNode;
import mylang.ast.SourceRange;

public final class MemberAccessExpression extends AstNode implements Expression {
	private final Expression target;
	private final String memberName;

	public MemberAccessExpression(Expression target, String memberName) {
		this(target, memberName, SourceRange.UNKNOWN);
	}

	public MemberAccessExpression(Expression target, String memberName, SourceRange sourceRange) {
		super(sourceRange);
		this.target = target;
		this.memberName = memberName;
	}

	public Expression target() {
		return target;
	}

	public String memberName() {
		return memberName;
	}
}
