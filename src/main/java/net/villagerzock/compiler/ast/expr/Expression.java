package net.villagerzock.compiler.ast.expr;

import net.villagerzock.compiler.ast.Node;
import net.villagerzock.compiler.semantic.SemanticType;

public interface Expression extends Node {
	SemanticType resolvedType();
	void setResolvedType(SemanticType resolvedType);
}
