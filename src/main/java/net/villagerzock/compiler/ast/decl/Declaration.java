package mylang.ast.decl;

import mylang.ast.Node;

public sealed interface Declaration extends Node permits ProgramNode, ClassDeclaration, FieldDeclaration, MethodDeclaration, ParameterDeclaration {
}
