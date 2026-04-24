package net.villagerzock.compiler.ast.stmt;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.ast.expr.Expression;

public final class ExpressionStatement extends AstNode implements Statement {
	private final Expression expression;

	public ExpressionStatement(Expression expression) {
		this(expression, SourceRange.UNKNOWN);
	}

	public ExpressionStatement(Expression expression, SourceRange sourceRange) {
		super(sourceRange);
		this.expression = expression;
	}

	public Expression expression() {
		return expression;
	}


	@Override
	public String getString() {
		return "ExpressionStatement";
	}
}
