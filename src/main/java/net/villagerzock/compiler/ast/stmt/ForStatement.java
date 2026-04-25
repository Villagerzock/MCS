package net.villagerzock.compiler.ast.stmt;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.ast.expr.Expression;

public final class ForStatement extends AstNode implements Statement {
	private final Statement initializer;
	private final Expression condition;
	private final Expression update;
	private final Statement body;

	public ForStatement(Statement initializer, Expression condition, Expression update, Statement body) {
		this(initializer, condition, update, body, SourceRange.UNKNOWN);
	}

	public ForStatement(Statement initializer, Expression condition, Expression update, Statement body, SourceRange sourceRange) {
		super(sourceRange);
		this.initializer = initializer;
		this.condition = condition;
		this.update = update;
		this.body = body;
	}

	public Statement initializer() {
		return initializer;
	}

	public Expression condition() {
		return condition;
	}

	public Expression update() {
		return update;
	}

	public Statement body() {
		return body;
	}

	@Override
	public String getString() {
		return "For";
	}
}
