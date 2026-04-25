package net.villagerzock.compiler.ast.stmt;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.ast.expr.Expression;

public final class WhileStatement extends AstNode implements Statement {
	private final Expression condition;
	private final Statement body;

	public WhileStatement(Expression condition, Statement body) {
		this(condition, body, SourceRange.UNKNOWN);
	}

	public WhileStatement(Expression condition, Statement body, SourceRange sourceRange) {
		super(sourceRange);
		this.condition = condition;
		this.body = body;
	}

	public Expression condition() {
		return condition;
	}

	public Statement body() {
		return body;
	}

	@Override
	public String getString() {
		return "While";
	}
}
