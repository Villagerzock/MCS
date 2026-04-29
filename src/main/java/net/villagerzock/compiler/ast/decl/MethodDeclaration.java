package net.villagerzock.compiler.ast.decl;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.ast.stmt.BlockStatement;
import net.villagerzock.compiler.ast.type.TypeNode;
import net.villagerzock.compiler.semantic.MethodSymbol;
import net.villagerzock.mcfunction.MCFunction;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class MethodDeclaration extends AstNode implements Declaration {
	private final Set<MethodModifier> modifiers;
	private final TypeNode returnType;
	private final String name;
	private final List<ParameterDeclaration> parameters;
	private final BlockStatement body;
	private final NativeBody nativeBody;
	private MethodSymbol resolvedMethodSymbol;

	public static class NativeBody {
		private final String code;
		private MCFunction associatedFunction;

        public NativeBody(String code) {
            this.code = code;
        }

		public void setAssociatedFunction(MCFunction associatedFunction) {
			this.associatedFunction = associatedFunction;
		}

		public MCFunction getAssociatedFunction() {
			return associatedFunction;
		}

		public String getCode() {
			return code;
		}
	}

	public MethodDeclaration(TypeNode returnType, String name, List<ParameterDeclaration> parameters, BlockStatement body) {
		this(Set.of(), returnType, name, parameters, body, null, SourceRange.UNKNOWN);
	}

	public MethodDeclaration(TypeNode returnType, String name, List<ParameterDeclaration> parameters, BlockStatement body, SourceRange sourceRange) {
		this(Set.of(), returnType, name, parameters, body, null, sourceRange);
	}

	public MethodDeclaration(Set<MethodModifier> modifiers, TypeNode returnType, String name, List<ParameterDeclaration> parameters, BlockStatement body) {
		this(modifiers, returnType, name, parameters, body, null, SourceRange.UNKNOWN);
	}

	public MethodDeclaration(Set<MethodModifier> modifiers, TypeNode returnType, String name, List<ParameterDeclaration> parameters, BlockStatement body, SourceRange sourceRange) {
		this(modifiers, returnType, name, parameters, body, null, sourceRange);
	}

	public MethodDeclaration(Set<MethodModifier> modifiers, TypeNode returnType, String name, List<ParameterDeclaration> parameters, BlockStatement body, String nativeBody) {
		this(modifiers, returnType, name, parameters, body, nativeBody, SourceRange.UNKNOWN);
	}

	public MethodDeclaration(Set<MethodModifier> modifiers, TypeNode returnType, String name, List<ParameterDeclaration> parameters, BlockStatement body, String nativeBody, SourceRange sourceRange) {
		super(sourceRange);
		this.modifiers = modifiers.isEmpty() ? Set.of() : EnumSet.copyOf(modifiers);
		this.returnType = returnType;
		this.name = name;
		this.parameters = List.copyOf(parameters);
		this.body = body;
		this.nativeBody = new NativeBody(nativeBody);
	}

	public Set<MethodModifier> modifiers() {
		return modifiers;
	}

	public boolean hasModifier(MethodModifier modifier) {
		return modifiers.contains(modifier);
	}

	public boolean isStatic() {
		return hasModifier(MethodModifier.STATIC);
	}

	public boolean isNative() {
		return hasModifier(MethodModifier.NATIVE);
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

	public NativeBody nativeBody() {
		return nativeBody;
	}

	public MethodSymbol resolvedMethodSymbol() {
		return resolvedMethodSymbol;
	}

	public void setResolvedMethodSymbol(MethodSymbol resolvedMethodSymbol) {
		this.resolvedMethodSymbol = resolvedMethodSymbol;
	}

	@Override
	public String getString() {
		String prefix = modifiers.isEmpty() ? "" : modifiers + " ";
		return "Method(" + prefix + name() + ")";
	}

	public MCFunction getFunction(){
		if (isNative()){
			return nativeBody.associatedFunction;
		}else {
			return body.getAssociatedFunction();
		}
	}
}
