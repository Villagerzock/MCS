package net.villagerzock.snbt;

public final class SnbtNumber implements SnbtElement {
	private final String value;

	private SnbtNumber(String value) {
		this.value = value;
	}

	public static SnbtNumber integer(int value) {
		return new SnbtNumber(Integer.toString(value));
	}

	public static SnbtNumber longNumber(long value) {
		return new SnbtNumber(value + "L");
	}

	public static SnbtNumber floatNumber(float value) {
		return new SnbtNumber(Float.toString(value) + "f");
	}

	public static SnbtNumber doubleNumber(double value) {
		return new SnbtNumber(Double.toString(value) + "d");
	}

	public static SnbtNumber byteNumber(byte value) {
		return new SnbtNumber(value + "b");
	}

	public static SnbtNumber shortNumber(short value) {
		return new SnbtNumber(value + "s");
	}

	@Override
	public String toSnbt() {
		return value;
	}
}
