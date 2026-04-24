package net.villagerzock.compiler.ast.decl;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.ast.stmt.BlockStatement;
import net.villagerzock.compiler.ast.type.TypeNode;

import java.util.List;

public final class MethodDeclaration extends AstNode implements Declaration {
	private final TypeNode returnType;
	private final String name;
	private final List<ParameterDeclaration> parameters;
	private final BlockStatement body;

	public MethodDeclaration(TypeNode returnType, String name, List<ParameterDeclaration> parameters, BlockStatement body) {
		this(returnType, name, parameters, body, SourceRange.UNKNOWN);
	}

	public MethodDeclaration(TypeNode returnType, String name, List<ParameterDeclaration> parameters, BlockStatement body, SourceRange sourceRange) {
		super(sourceRange);
		this.returnType = returnType;
		this.name = name;
		this.parameters = List.copyOf(parameters);
		this.body = body;
	}

	public TypeNode returnType() {
		return returnType;
	}

	public String name() {
		return name;
	}

	public List<ParameterDeclaration> parameters() {
		return parameters;
	}

	public BlockStatement body() {
		return body;
	}


	@Override
	public String getString() {
		return "Method(" + name() + ")";
	}
}
