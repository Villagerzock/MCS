package mylang.ast.expr;

import mylang.ast.Node;

public sealed interface Expression extends Node permits AssignmentExpression, BinaryExpression, UnaryExpression, IdentifierExpression, NumberLiteralExpression, StringLiteralExpression, BooleanLiteralExpression, GroupExpression, CallExpression, MemberAccessExpression {
}
