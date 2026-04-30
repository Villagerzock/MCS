package net.villagerzock.compiler.semantic;

import net.villagerzock.compiler.ast.decl.ConstructorDeclaration;
import net.villagerzock.compiler.ast.decl.MethodDeclaration;

import java.util.List;

public record ConstructorSymbol(List<Symbol> parameters, ConstructorDeclaration declaration) {
}
