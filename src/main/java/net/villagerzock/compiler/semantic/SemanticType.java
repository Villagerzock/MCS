package net.villagerzock.compiler.semantic;

import net.villagerzock.compiler.ast.expr.*;
import net.villagerzock.compiler.ast.type.TypeNode;

import java.util.Locale;
import java.util.Objects;

public final class SemanticType {
	public static final SemanticType INT = new SemanticType("int",new NumberLiteralExpression("0"));
	public static final SemanticType STRING = new SemanticType("string",new StringLiteralExpression(""));
	public static final SemanticType BOOLEAN = new SemanticType("boolean", new BooleanLiteralExpression(false));
	public static final SemanticType VOID = new SemanticType("function", new NullLiteralExpression());
	public static final SemanticType UNKNOWN = new SemanticType("<unknown>", new NullLiteralExpression());

	private final String name;
    private String namespace;
    private String path;
    private final boolean isBuiltin;
	private final Expression defaultExpression;

	private SemanticType(String name, Expression defaultExpression) {
		this.name = name;
        this.defaultExpression = defaultExpression;
        this.namespace = null;
        this.path = null;
        this.isBuiltin = true;
	}
    private SemanticType(String name, String namespace, String path, Expression defaultExpression) {
        this.name = name;
        this.namespace = namespace;
        this.path = path;
        this.defaultExpression = defaultExpression;
        this.isBuiltin = false;
    }

	public static SemanticType from(TypeNode node) {
		if (node == null) {
			return UNKNOWN;
		}
		return fromName(node.name());
	}

	public static SemanticType fromName(String rawName) {
		if (rawName == null || rawName.isBlank()) {
			return UNKNOWN;
		}

		String normalized = rawName.trim().toLowerCase(Locale.ROOT);
		return switch (normalized) {
			case "int", "integer", "number", "long" -> INT;
			case "string" -> STRING;
			case "bool", "boolean" -> BOOLEAN;
			case "function", "void" -> VOID;
			default -> new SemanticType(rawName.trim(),null,null,new NullLiteralExpression());
		};
	}

	public Expression getDefaultExpression(){
		return defaultExpression;
	}

	public String name() {
		return name;
	}

    public String getCanonnicalName() {
        if (namespace == null || path == null) {
            return name;
        }
        return "%s:%s%s".formatted(namespace, path, name);
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isBuiltin() {
        return isBuiltin;
    }

    public boolean isUnknown() {
		return this.equals(UNKNOWN);
	}

	public boolean isVoid() {
		return this.equals(VOID);
	}

	public boolean isAssignableFrom(SemanticType other) {
		if (this.isUnknown() || other == null || other.isUnknown()) {
			return true;
		}
		return equals(other);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof SemanticType that)) return false;
		return name.equals(that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public String toString() {
		return name;
	}
}
