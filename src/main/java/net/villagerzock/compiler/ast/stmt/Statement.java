package mylang.ast.stmt;

import mylang.ast.Node;

public sealed interface Statement extends Node permits BlockStatement, IfStatement, ReturnStatement, VariableDeclarationStatement, ExpressionStatement {
}
