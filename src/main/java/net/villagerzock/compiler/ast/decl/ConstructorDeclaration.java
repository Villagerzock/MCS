package net.villagerzock.compiler.ast.decl;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.ast.stmt.BlockStatement;
import net.villagerzock.compiler.semantic.ConstructorSymbol;
import net.villagerzock.compiler.semantic.MethodSymbol;
import net.villagerzock.mcfunction.MCFunction;

import java.util.List;

public class ConstructorDeclaration extends AstNode implements Declaration {
	private final List<ParameterDeclaration> parameters;
	private final BlockStatement body;
	private ConstructorSymbol constructorSymbol;
	private boolean implicitRecordConstructor;

	public ConstructorDeclaration(List<ParameterDeclaration> parameters, BlockStatement body) {
		this(parameters, body, SourceRange.UNKNOWN);
	}

	public ConstructorDeclaration(List<ParameterDeclaration> parameters, BlockStatement body, SourceRange sourceRange) {
		super(sourceRange);
		this.parameters = List.copyOf(parameters);
		this.body = body;
	}

	public List<ParameterDeclaration> parameters() {
		return parameters;
	}

	public BlockStatement body() {
		return body;
	}

	public boolean isImplicitRecordConstructor() {
		return implicitRecordConstructor;
	}

	public void setImplicitRecordConstructor(boolean implicitRecordConstructor) {
		this.implicitRecordConstructor = implicitRecordConstructor;
	}

	@Override
	public String getString() {
		return "Constructor(" + parameters.size() + " params)";
	}

	public MCFunction getFunction() {
		return body == null ? null : body.getAssociatedFunction();
	}

	public void setResolvedConstructorSymbol(ConstructorSymbol constructorSymbol) {
		this.constructorSymbol = constructorSymbol;
	}
	public ConstructorSymbol getResolvedConstructorSymbol(){
		return constructorSymbol;
	}
}
