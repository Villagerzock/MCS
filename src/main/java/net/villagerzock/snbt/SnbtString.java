package net.villagerzock.snbt;

public final class SnbtString implements SnbtElement {
	private final String value;

	public SnbtString(String value) {
		this.value = value == null ? "" : value;
	}

	public String value() {
		return value;
	}

	@Override
	public String toSnbt() {
		return quote(value);
	}

	public static String quote(String value) {
		StringBuilder builder = new StringBuilder();
		builder.append('"');

		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
				case '\\' -> builder.append("\\\\");
				case '"' -> builder.append("\\\"");
				case '\n' -> builder.append("\\n");
				case '\r' -> builder.append("\\r");
				case '\t' -> builder.append("\\t");
				default -> builder.append(c);
			}
		}

		builder.append('"');
		return builder.toString();
	}
}
