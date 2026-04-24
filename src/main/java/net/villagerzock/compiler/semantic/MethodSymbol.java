package net.villagerzock.compiler.semantic;

import net.villagerzock.compiler.ast.decl.MethodDeclaration;

import java.util.List;

public record MethodSymbol(String name, SemanticType returnType, List<Symbol> parameters, MethodDeclaration declaration) {
}
