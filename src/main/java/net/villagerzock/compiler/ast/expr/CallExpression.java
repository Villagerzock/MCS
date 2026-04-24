package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;

import java.util.List;

public final class CallExpression extends AstNode implements Expression {
	private final Expression callee;
	private final List<Expression> arguments;

	public CallExpression(Expression callee, List<Expression> arguments) {
		this(callee, arguments, SourceRange.UNKNOWN);
	}

	public CallExpression(Expression callee, List<Expression> arguments, SourceRange sourceRange) {
		super(sourceRange);
		this.callee = callee;
		this.arguments = List.copyOf(arguments);
	}

	public Expression callee() {
		return callee;
	}

	public List<Expression> arguments() {
		return arguments;
	}


	@Override
	public String getString() {
		return "Call";
	}
}
