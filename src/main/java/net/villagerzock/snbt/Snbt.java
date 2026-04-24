package net.villagerzock.snbt;

public final class Snbt {
	private Snbt() {
	}

	public static SnbtCompound compound() {
		return new SnbtCompound();
	}

	public static SnbtList list() {
		return new SnbtList();
	}

	public static SnbtString string(String value) {
		return new SnbtString(value);
	}

	public static SnbtBoolean bool(boolean value) {
		return SnbtBoolean.of(value);
	}

	public static SnbtNumber integer(int value) {
		return SnbtNumber.integer(value);
	}

	public static SnbtNumber longNumber(long value) {
		return SnbtNumber.longNumber(value);
	}

	public static SnbtNumber floatNumber(float value) {
		return SnbtNumber.floatNumber(value);
	}

	public static SnbtNumber doubleNumber(double value) {
		return SnbtNumber.doubleNumber(value);
	}

	public static SnbtNumber byteNumber(byte value) {
		return SnbtNumber.byteNumber(value);
	}

	public static SnbtNumber shortNumber(short value) {
		return SnbtNumber.shortNumber(value);
	}

	public static SnbtRaw raw(String value) {
		return new SnbtRaw(value);
	}
}
