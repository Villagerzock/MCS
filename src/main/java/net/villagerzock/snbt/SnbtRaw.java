package net.villagerzock.snbt;

public final class SnbtRaw implements SnbtElement {
	private final String value;

	public SnbtRaw(String value) {
		this.value = value == null ? "" : value;
	}

	@Override
	public String toSnbt() {
		return value;
	}
}
