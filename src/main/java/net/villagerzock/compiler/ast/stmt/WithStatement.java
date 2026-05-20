package net.villagerzock.compiler.ast.stmt;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.ast.expr.Expression;

import java.util.List;

public final class WithStatement extends AstNode implements Statement {
    private final List<Part> parts;
    private final Statement body;

    public WithStatement(List<Part> parts, Statement body) {
        this(parts, body, SourceRange.UNKNOWN);
    }

    public WithStatement(List<Part> parts, Statement body, SourceRange sourceRange) {
        super(sourceRange);
        this.parts = List.copyOf(parts);
        this.body = body;
    }

    public List<Part> parts() {
        return parts;
    }

    public Statement body() {
        return body;
    }

    @Override
    public String getString() {
        return "With";
    }

    public record Part(String name, Value value) {
    }

    public sealed interface Value permits SelectorValue, CallValue, CoordinateValue {
    }

    public record SelectorValue(String selector) implements Value {
    }

    public record CallValue(String name, List<Expression> arguments) implements Value {
    }

    public record CoordinateValue(List<Expression> coordinates) implements Value {
    }
}
