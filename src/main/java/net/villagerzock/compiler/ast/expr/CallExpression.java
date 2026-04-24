package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.semantic.SemanticType;
import net.villagerzock.compiler.semantic.MethodSymbol;
import net.villagerzock.compiler.ast.decl.MethodDeclaration;

import java.util.List;

public final class CallExpression extends AstNode implements Expression {
	private SemanticType resolvedType;
	private MethodSymbol resolvedMethod;
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


	public MethodSymbol resolvedMethod() {
		return resolvedMethod;
	}

	public MethodDeclaration resolvedMethodDeclaration() {
		return resolvedMethod == null ? null : resolvedMethod.declaration();
	}

	public void setResolvedMethod(MethodSymbol resolvedMethod) {
		this.resolvedMethod = resolvedMethod;
	}

	public SemanticType resolvedType() {
		return resolvedType;
	}

	public void setResolvedType(SemanticType resolvedType) {
		this.resolvedType = resolvedType;
	}

	@Override
	public String getString() {
		return "Call";
	}
}
