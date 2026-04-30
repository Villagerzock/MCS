package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.semantic.SemanticType;

public final class NullLiteralExpression extends AstNode implements Expression {
    private SemanticType resolvedType;

    public NullLiteralExpression() {
        this(SourceRange.UNKNOWN);
    }

    public NullLiteralExpression(SourceRange sourceRange) {
        super(sourceRange);
    }


    public SemanticType resolvedType() {
        return resolvedType;
    }

    public void setResolvedType(SemanticType resolvedType) {
        this.resolvedType = resolvedType;
    }

    @Override
    public String getString() {
        return "NULL";
    }
}
