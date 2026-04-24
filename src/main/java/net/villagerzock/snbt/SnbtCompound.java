package net.villagerzock.snbt;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public final class SnbtCompound implements SnbtElement {
	private final Map<String, SnbtElement> values = new LinkedHashMap<>();

	public SnbtCompound put(String key, SnbtElement value) {
		if (key == null || key.isBlank()) {
			throw new IllegalArgumentException("SNBT key may not be null or blank");
		}

		values.put(key, value == null ? Snbt.raw("null") : value);
		return this;
	}

	public SnbtCompound putString(String key, String value) {
		return put(key, Snbt.string(value));
	}

	public SnbtCompound putInt(String key, int value) {
		return put(key, Snbt.integer(value));
	}

	public SnbtCompound putBoolean(String key, boolean value) {
		return put(key, Snbt.bool(value));
	}

	@Override
	public String toSnbt() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");

		for (Map.Entry<String, SnbtElement> entry : values.entrySet()) {
			joiner.add(formatKey(entry.getKey()) + ":" + entry.getValue().toSnbt());
		}

		return joiner.toString();
	}

	private static String formatKey(String key) {
		if (key.matches("[A-Za-z0-9_+.-]+")) {
			return key;
		}

		return SnbtString.quote(key);
	}
}
