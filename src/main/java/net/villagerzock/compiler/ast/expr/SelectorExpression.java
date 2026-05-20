package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.semantic.SemanticType;

public final class SelectorExpression extends AstNode implements Expression {
    private final String selector;
    private SemanticType resolvedType;

    public SelectorExpression(String selector) {
        this(selector, SourceRange.UNKNOWN);
    }

    public SelectorExpression(String selector, SourceRange sourceRange) {
        super(sourceRange);
        this.selector = selector;
    }

    public String selector() {
        return selector;
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
        return "Selector(" + selector + ")";
    }
}
