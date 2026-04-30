package net.villagerzock.compiler.ast.decl;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.ast.stmt.BlockStatement;
import net.villagerzock.compiler.ast.type.TypeNode;
import net.villagerzock.compiler.semantic.MethodSymbol;
import net.villagerzock.mcfunction.LightMCFunction;
import net.villagerzock.mcfunction.MCFunction;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class LightMethodDeclaration extends MethodDeclaration {
	private final LightMCFunction mcFunction;
	public LightMethodDeclaration(TypeNode returnType, String name, List<ParameterDeclaration> parameters, LightMCFunction mcFunction) {
		super(returnType, name, parameters, null);
		this.mcFunction = mcFunction;
	}

	@Override
	public MCFunction getFunction() {
		return mcFunction;
	}
}
