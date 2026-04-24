package net.villagerzock.snbt;

import static net.villagerzock.snbt.Snbt.*;

public final class Example {
	public static void main(String[] args) {
		String snbt = compound()
			.put("locals", compound()
				.put("name", string("Steve"))
				.put("level", integer(10))
				.put("admin", bool(false)))
			.put("items", list()
				.add(string("stone"))
				.add(string("diamond")))
			.toSnbt();

		System.out.println(snbt);
	}
}
