package net.villagerzock.compiler.ast.stmt;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.ast.expr.Expression;

public final class IfStatement extends AstNode implements Statement {
	private final Expression condition;
	private final Statement thenBranch;
	private final Statement elseBranch;

	public IfStatement(Expression condition, Statement thenBranch, Statement elseBranch) {
		this(condition, thenBranch, elseBranch, SourceRange.UNKNOWN);
	}

	public IfStatement(Expression condition, Statement thenBranch, Statement elseBranch, SourceRange sourceRange) {
		super(sourceRange);
		this.condition = condition;
		this.thenBranch = thenBranch;
		this.elseBranch = elseBranch;
	}

	public Expression condition() {
		return condition;
	}

	public Statement thenBranch() {
		return thenBranch;
	}

	public Statement elseBranch() {
		return elseBranch;
	}


	@Override
	public String getString() {
		return "If";
	}
}
