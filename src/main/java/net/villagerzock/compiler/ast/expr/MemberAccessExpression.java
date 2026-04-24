package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.semantic.SemanticType;
import net.villagerzock.compiler.semantic.Symbol;
import net.villagerzock.compiler.semantic.MethodSymbol;
import net.villagerzock.compiler.ast.decl.ClassDeclaration;
import net.villagerzock.compiler.ast.decl.MethodDeclaration;

public final class MemberAccessExpression extends AstNode implements Expression {
	private SemanticType resolvedType;
	private SemanticType resolvedTargetType;
	private ClassDeclaration resolvedTargetClass;
	private Symbol resolvedField;
	private MethodSymbol resolvedMethod;
	private final Expression target;
	private final String memberName;

	public MemberAccessExpression(Expression target, String memberName) {
		this(target, memberName, SourceRange.UNKNOWN);
	}

	public MemberAccessExpression(Expression target, String memberName, SourceRange sourceRange) {
		super(sourceRange);
		this.target = target;
		this.memberName = memberName;
	}

	public Expression target() {
		return target;
	}

	public String memberName() {
		return memberName;
	}


	public SemanticType resolvedTargetType() {
		return resolvedTargetType;
	}

	public void setResolvedTargetType(SemanticType resolvedTargetType) {
		this.resolvedTargetType = resolvedTargetType;
	}

	public ClassDeclaration resolvedTargetClass() {
		return resolvedTargetClass;
	}

	public void setResolvedTargetClass(ClassDeclaration resolvedTargetClass) {
		this.resolvedTargetClass = resolvedTargetClass;
	}

	public Symbol resolvedField() {
		return resolvedField;
	}

	public void setResolvedField(Symbol resolvedField) {
		this.resolvedField = resolvedField;
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
		return "MemberAccess(" + memberName() + ")";
	}
}
