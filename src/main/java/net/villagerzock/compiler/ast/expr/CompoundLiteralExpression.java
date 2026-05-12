package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.semantic.SemanticType;

import java.util.List;

public final class CompoundLiteralExpression extends AstNode implements Expression {
    private SemanticType resolvedType;
    private final List<Entry> entries;

    public record Entry(String key, Expression value) {
    }

    public CompoundLiteralExpression(List<Entry> entries) {
        this(entries, SourceRange.UNKNOWN);
    }

    public CompoundLiteralExpression(List<Entry> entries, SourceRange sourceRange) {
        super(sourceRange);
        this.entries = List.copyOf(entries);
    }

    public List<Entry> entries() {
        return entries;
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
        return "CompoundLiteral";
    }
}
