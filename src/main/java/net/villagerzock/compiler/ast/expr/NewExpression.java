package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.semantic.ConstructorSymbol;
import net.villagerzock.compiler.semantic.MethodSymbol;
import net.villagerzock.compiler.semantic.SemanticType;

import java.util.List;

public final class NewExpression extends AstNode implements Expression {
    private SemanticType resolvedType;
    private ConstructorSymbol resolvedConstructor;
    private final String typeName;
    private final List<Expression> arguments;

    public NewExpression(String typeName, List<Expression> arguments) {
        this(typeName, arguments, SourceRange.UNKNOWN);
    }

    public NewExpression(String typeName, List<Expression> arguments, SourceRange sourceRange) {
        super(sourceRange);
        this.typeName = typeName;
        this.arguments = List.copyOf(arguments);
    }

    public String typeName() {
        return typeName;
    }

    public List<Expression> arguments() {
        return arguments;
    }

    public SemanticType resolvedType() {
        return resolvedType;
    }

    public void setResolvedType(SemanticType resolvedType) {
        this.resolvedType = resolvedType;
    }

    @Override
    public String getString() {
        return "New(" + typeName + ")";
    }

    public void setResolvedConstructorSymbol(ConstructorSymbol constructor) {
        this.resolvedConstructor = constructor;
    }

    public ConstructorSymbol getResolvedConstructor() {
        return resolvedConstructor;
    }
}