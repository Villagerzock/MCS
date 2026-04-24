package net.villagerzock.snbt;

public final class SnbtBoolean implements SnbtElement {
	public static final SnbtBoolean TRUE = new SnbtBoolean(true);
	public static final SnbtBoolean FALSE = new SnbtBoolean(false);

	private final boolean value;

	private SnbtBoolean(boolean value) {
		this.value = value;
	}

	public static SnbtBoolean of(boolean value) {
		return value ? TRUE : FALSE;
	}

	@Override
	public String toSnbt() {
		return Boolean.toString(value);
	}
}
