package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.semantic.SemanticType;

import java.util.List;

public final class ArrayLiteralExpression extends AstNode implements Expression {
    private SemanticType resolvedType;
    private final List<Expression> values;

    public ArrayLiteralExpression(List<Expression> values) {
        this(values, SourceRange.UNKNOWN);
    }

    public ArrayLiteralExpression(List<Expression> values, SourceRange sourceRange) {
        super(sourceRange);
        this.values = List.copyOf(values);
    }

    public List<Expression> values() {
        return values;
    }

    @Override
    public SemanticType resolvedType() {
        return resolvedType;
    }

    @Override
    public void setResolvedType(SemanticType resolvedType) {
        this.resolvedType = resolvedType;
    }

    @Override
    public String getString() {
        return "ArrayLiteral";
    }
}
