package net.villagerzock.compiler.ast.stmt;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;

import java.util.List;

public final class BlockStatement extends AstNode implements Statement {
	private final List<Statement> statements;

	public BlockStatement(List<Statement> statements) {
		this(statements, SourceRange.UNKNOWN);
	}

	public BlockStatement(List<Statement> statements, SourceRange sourceRange) {
		super(sourceRange);
		this.statements = List.copyOf(statements);
	}

	public List<Statement> statements() {
		return statements;
	}


	@Override
	public String getString() {
		return "Block";
	}
}
