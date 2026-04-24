package net.villagerzock.snbt;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public final class SnbtList implements SnbtElement {
	private final List<SnbtElement> values = new ArrayList<>();

	public SnbtList add(SnbtElement value) {
		values.add(value == null ? Snbt.raw("null") : value);
		return this;
	}

	public SnbtList addString(String value) {
		return add(Snbt.string(value));
	}

	public SnbtList addInt(int value) {
		return add(Snbt.integer(value));
	}

	public SnbtList addBoolean(boolean value) {
		return add(Snbt.bool(value));
	}

	@Override
	public String toSnbt() {
		StringJoiner joiner = new StringJoiner(",", "[", "]");

		for (SnbtElement value : values) {
			joiner.add(value.toSnbt());
		}

		return joiner.toString();
	}
}
