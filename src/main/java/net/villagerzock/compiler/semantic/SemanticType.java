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
	public static final SemanticType REFERENCE = new SemanticType("pointer", new NumberLiteralExpression("0"));
	public static final SemanticType TARGET = new SemanticType("Target", new SelectorExpression("@s"));
	public static final SemanticType PLAYER_TARGET = new SemanticType("PlayerTarget", new SelectorExpression("@s"));
	public static final SemanticType LOCATION = new SemanticType("Location", new StringLiteralExpression("~ ~ ~"));
	public static final SemanticType ANY = new SemanticType("any", new NullLiteralExpression());
	public static final SemanticType UNKNOWN = new SemanticType("<unknown>", new NullLiteralExpression());

	private final String name;
    private String namespace;
    private String path;
    private final boolean isBuiltin;
	private final Expression defaultExpression;
	private final Kind kind;
	private final SemanticType elementType;

	public enum Kind {
		SIMPLE,
		ARRAY,
		DICTIONARY
	}

	private SemanticType(String name, Expression defaultExpression) {
		this.name = name;
        this.defaultExpression = defaultExpression;
        this.namespace = null;
        this.path = null;
        this.isBuiltin = true;
		this.kind = Kind.SIMPLE;
		this.elementType = null;
	}
    private SemanticType(String name, String namespace, String path, Expression defaultExpression) {
        this.name = name;
        this.namespace = namespace;
        this.path = path;
        this.defaultExpression = defaultExpression;
        this.isBuiltin = false;
		this.kind = Kind.SIMPLE;
		this.elementType = null;
    }

	private SemanticType(Kind kind, SemanticType elementType) {
		this.kind = kind;
		this.elementType = elementType;
		this.name = kind == Kind.ARRAY
				? elementType.name() + "[]"
				: "dict[" + elementType.name() + "]";
		this.namespace = null;
		this.path = null;
		this.isBuiltin = true;
		this.defaultExpression = kind == Kind.ARRAY
				? new ArrayLiteralExpression(java.util.List.of())
				: new CompoundLiteralExpression(java.util.List.of());
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

		String trimmed = rawName.trim();
		if (trimmed.toLowerCase(Locale.ROOT).startsWith("dict[") && trimmed.endsWith("]")) {
			return dictionaryOf(fromName(trimmed.substring(5, trimmed.length() - 1)));
		}
		if (trimmed.endsWith("[]") && trimmed.length() > 2) {
			return arrayOf(fromName(trimmed.substring(0, trimmed.length() - 2)));
		}

		String normalized = trimmed.toLowerCase(Locale.ROOT);
		return switch (normalized) {
			case "int", "integer", "number", "long" -> INT;
			case "string" -> STRING;
			case "bool", "boolean" -> BOOLEAN;
			case "function", "void" -> VOID;
			case "pointer" -> REFERENCE;
			case "target" -> TARGET;
			case "playertarget" -> PLAYER_TARGET;
			case "location" -> LOCATION;
			case "any" -> ANY;
			default -> new SemanticType(trimmed,null,null,new NullLiteralExpression());
		};
	}

	public static SemanticType arrayOf(SemanticType elementType) {
		return new SemanticType(Kind.ARRAY, elementType == null ? UNKNOWN : elementType);
	}

	public static SemanticType dictionaryOf(SemanticType elementType) {
		return new SemanticType(Kind.DICTIONARY, elementType == null ? UNKNOWN : elementType);
	}

	public Expression getDefaultExpression(){
		return defaultExpression;
	}

	public String name() {
		return name;
	}

	public Kind kind() {
		return kind;
	}

	public SemanticType elementType() {
		return elementType;
	}

    public String getCanonnicalName() {
		if (kind == Kind.ARRAY) {
			return elementType.getCanonnicalName() + "[]";
		}
		if (kind == Kind.DICTIONARY) {
			return "dict[" + elementType.getCanonnicalName() + "]";
		}
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

	public boolean isAny() {
		return this.equals(ANY);
	}

    public boolean isUnknown() {
		return this.equals(UNKNOWN);
	}

	public boolean isVoid() {
		return this.equals(VOID);
	}

	public boolean isAssignableFrom(SemanticType other) {
		if (this.isUnknown() || other == null || other.isUnknown() || this.isAny() || other.isAny()) {
			return true;
		}
		if (kind == Kind.ARRAY || kind == Kind.DICTIONARY) {
			if (kind == Kind.ARRAY && isTargetLike(elementType) && isTargetLike(other)) {
				return elementType.isAssignableFrom(other);
			}
			return kind == other.kind
					&& elementType.isAssignableFrom(other.elementType);
		}
		if (this.equals(TARGET) && other.equals(PLAYER_TARGET)) {
			return true;
		}
		return equals(other);
	}

	private static boolean isTargetLike(SemanticType type) {
		return TARGET.equals(type) || PLAYER_TARGET.equals(type);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof SemanticType that)) return false;
		return kind == that.kind
				&& name.equals(that.name)
				&& Objects.equals(elementType, that.elementType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, kind, elementType);
	}

	@Override
	public String toString() {
		return name;
	}
}
