package net.villagerzock.compiler.ast.stmt;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.ast.expr.Expression;

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


	@Override
	public String getString() {
		return "Return";
	}
}
