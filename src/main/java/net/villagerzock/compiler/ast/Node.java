package mylang.ast;

public sealed interface Node permits mylang.ast.decl.Declaration, mylang.ast.expr.Expression, mylang.ast.stmt.Statement {
}
