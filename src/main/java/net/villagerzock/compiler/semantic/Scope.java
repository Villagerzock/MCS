package net.villagerzock.compiler.semantic;

import java.util.LinkedHashMap;
import java.util.Map;

final class Scope {
	private final Scope parent;
	private final Map<String, Symbol> symbols = new LinkedHashMap<>();

	Scope(Scope parent) {
		this.parent = parent;
	}

	boolean define(Symbol symbol) {
		if (symbols.containsKey(symbol.name())) {
			return false;
		}
		symbols.put(symbol.name(), symbol);
		return true;
	}

	Symbol resolve(String name) {
		Symbol symbol = symbols.get(name);
		if (symbol != null) {
			return symbol;
		}
		return parent == null ? null : parent.resolve(name);
	}
}
