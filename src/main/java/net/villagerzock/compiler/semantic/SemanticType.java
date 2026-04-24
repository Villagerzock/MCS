package net.villagerzock.compiler.semantic;

import net.villagerzock.compiler.ast.type.TypeNode;

import java.util.Locale;
import java.util.Objects;

public final class SemanticType {
	public static final SemanticType INT = new SemanticType("int");
	public static final SemanticType STRING = new SemanticType("string");
	public static final SemanticType BOOLEAN = new SemanticType("boolean");
	public static final SemanticType VOID = new SemanticType("function");
	public static final SemanticType UNKNOWN = new SemanticType("<unknown>");

	private final String name;

	private SemanticType(String name) {
		this.name = name;
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
			default -> new SemanticType(rawName.trim());
		};
	}

	public String name() {
		return name;
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
