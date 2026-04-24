package net.villagerzock.compiler.semantic;

import net.villagerzock.compiler.ast.Node;

public record Symbol(String name, SemanticType type, SymbolKind kind, Node declaration) {
}
