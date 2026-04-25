package net.villagerzock.compiler.gen;

import net.villagerzock.compiler.ast.decl.ClassDeclaration;
import net.villagerzock.compiler.ast.decl.Declaration;
import net.villagerzock.compiler.ast.decl.MethodDeclaration;
import net.villagerzock.compiler.ast.decl.ProgramNode;
import net.villagerzock.compiler.ast.expr.*;
import net.villagerzock.compiler.ast.stmt.BlockStatement;
import net.villagerzock.compiler.ast.stmt.ExpressionStatement;
import net.villagerzock.compiler.ast.stmt.IfStatement;
import net.villagerzock.compiler.ast.stmt.Statement;
import net.villagerzock.compiler.semantic.MethodSymbol;
import net.villagerzock.mcfunction.ICommandPart;
import net.villagerzock.mcfunction.MCFunction;
import net.villagerzock.mcfunction.MCFunctionUnit;
import net.villagerzock.mcfunction.commandParts.*;
import net.villagerzock.snbt.Example;
import net.villagerzock.snbt.SnbtCompound;

import java.awt.geom.Line2D;
import java.sql.Struct;
import java.util.List;
import java.util.Stack;

import static net.villagerzock.compiler.ast.expr.BinaryOperator.*;
import static net.villagerzock.snbt.Snbt.*;

public class Generator {
    public MCFunctionUnit generate(List<ProgramNode> nodes){
        PathStack pathStack = new PathStack();
        MCFunctionUnit unit = new MCFunctionUnit();
        for (ProgramNode node : nodes){
            pathStack.push(node.packagePath().path());
            generateClasses(node, unit, pathStack);
            pathStack.pop();
        }
        for (ProgramNode node : nodes){
            pathStack.push(node.packagePath().getString());
            for (ClassDeclaration decl : node.classes()){
                pathStack.push(decl.name());
                updateMembers(decl, unit, pathStack);
                pathStack.pop();
            }
            pathStack.pop();
        }

        unit.analyze();
        return unit;
    }
    private void generateClasses(ProgramNode node, MCFunctionUnit unit, PathStack pathStack){
        for (ClassDeclaration decl : node.classes()){
            generateMember(decl, unit, node.packagePath().namespace(), pathStack);
        }
    }
    private void generateMembers(ClassDeclaration decl, MCFunctionUnit unit, String namespace, PathStack pathStack) {
        for (Declaration d : decl.members()){
            generateMember(d, unit, namespace, pathStack);
        }
    }

    private void generateMember(Declaration decl, MCFunctionUnit unit, String namespace, PathStack pathStack){
        if (decl instanceof ClassDeclaration classDeclaration){
            pathStack.push(classDeclaration.name());
            generateMembers(classDeclaration, unit, namespace, pathStack);
            pathStack.pop();
        }
        if (decl instanceof MethodDeclaration methodDeclaration){

            if (methodDeclaration.isNative()){
                MCFunction function = unit.create(namespace,pathStack.getPath(),methodDeclaration.name()+"_native");
                methodDeclaration.nativeBody().setAssociatedFunction(function);
            }else {
                MCFunction function = unit.create(namespace,pathStack.getPath(),methodDeclaration.name()+"_entry");
                BlockStatement stmt = methodDeclaration.body();
                stmt.setAssociatedFunction(function);
            }
        }
    }

    private void updateMembers(ClassDeclaration classDeclaration, MCFunctionUnit unit, PathStack pathStack) {
        for (Declaration decl : classDeclaration.members()){
            if (decl instanceof ClassDeclaration declaration){
                updateMembers(declaration,unit,pathStack);
            }
            if (decl instanceof MethodDeclaration methodDeclaration){
                if (methodDeclaration.isNative()){
                    methodDeclaration.getFunction().addCommand(new NativePart(methodDeclaration.nativeBody().getCode()));
                }else {
                    generateBlock(methodDeclaration.body(),unit,methodDeclaration.getFunction(),pathStack,methodDeclaration.name());
                }
            }
        }
    }

    private void generateBlock(BlockStatement stmt, MCFunctionUnit unit, MCFunction function, PathStack pathStack, String baseName){
        for (Statement statement : stmt.statements()){
            generateStatement(statement,unit,function,pathStack,baseName);
        }
    }

    private void generateStatement(Statement statement, MCFunctionUnit unit, MCFunction function, PathStack pathStack, String baseName){
        if (statement instanceof ExpressionStatement expressionStatement){
            Expression exp = expressionStatement.expression();
            if (exp instanceof CallExpression callExpression){
                MethodSymbol method = callExpression.resolvedMethod();
                SnbtCompound locals = compound();
                for (int i = 0; i<callExpression.arguments().size(); i++){
                    Expression param = callExpression.arguments().get(i);
                    String name = method.declaration().parameters().get(i).name();
                    if (param instanceof StringLiteralExpression stringLiteralExpression){
                        locals.putString(name,stringLiteralExpression.rawValue());
                    }
                }
                SnbtCompound compound = compound()
                        .put("locals",locals)
                        .put("macro",compound());
                function.addCommand(new TmpWrite(compound.toSnbt()));
                function.addCommand(new CreateStackFrame());
                function.addCommand(new FunctionCall(method.declaration().getFunction()));
                function.addCommand(new PopStackFrame());
            }
        }

        if (statement instanceof IfStatement ifStatement){
            Statement then = ifStatement.thenBranch();
            if (then instanceof BlockStatement blockStatement){
                MCFunction f = unit.create(function.getNamespace(), function.getPath(), baseName + "_if");
                blockStatement.setAssociatedFunction(f);
                generateBlock(blockStatement,unit,f,pathStack,baseName);


                String condName = baseName + "_if_cond";

                ICommandPart conditionExpr = generateExpression(
                        ifStatement.condition(),
                        unit,
                        function,
                        pathStack,
                        condName
                );

                function.addCommand(conditionExpr);

                function.addCommand(
                        new ExecuteCall(new FunctionCall(f, true))
                                .addCondition(new ScoreMatchesCondition(condName, "1"))
                );
            }
        }
    }


    private ICommandPart generateExpression(
            Expression expression,
            MCFunctionUnit unit,
            MCFunction function,
            PathStack pathStack,
            String baseName
    ) {
        if (expression instanceof BooleanLiteralExpression bool) {
            return new TmpScoreWrite(baseName, bool.value() ? "1" : "0");
        }

        if (expression instanceof BinaryExpression binary) {
            return switch (binary.operator()) {
                case GREATER, LESS, GREATER_EQUAL, LESS_EQUAL, EQUAL, NOT_EQUAL ->
                        generateCompareExpression(binary, unit, function, pathStack, baseName);

                default -> throw new IllegalStateException("Unsupported boolean binary operator: " + binary.operator());
            };
        }

        throw new IllegalStateException("Unsupported boolean expression: " + expression.getClass().getSimpleName());
    }

    private ICommandPart generateCompareExpression(
            BinaryExpression expression,
            MCFunctionUnit unit,
            MCFunction function,
            PathStack pathStack,
            String baseName
    ) {
        String leftName = baseName + "_left";
        String rightName = baseName + "_right";

        ICommandPart left = generateNumberExpression(expression.left(), unit, function, pathStack, leftName);
        ICommandPart right = generateNumberExpression(expression.right(), unit, function, pathStack, rightName);

        String op = switch (expression.operator()) {
            case GREATER -> ">";
            case LESS -> "<";
            case GREATER_EQUAL -> ">=";
            case LESS_EQUAL -> "<=";
            case EQUAL -> "=";
            case NOT_EQUAL -> "=";
            default -> throw new IllegalStateException("Unsupported compare op: " + expression.operator());
        };

        ICommandPart setFalse = new TmpScoreWrite(baseName, "0");
        ICommandPart setTrue = new ExecuteCall(new TmpScoreWrite(baseName, "1"))
                .addCondition(new ScoreCompareCondition(leftName, op, rightName));

        BooleanExpressionPart part = new BooleanExpressionPart(setTrue)
                .addSetup(left)
                .addSetup(right)
                .addSetup(setFalse);

        if (expression.operator() == BinaryOperator.NOT_EQUAL) {
            ICommandPart setTrueWhenNotEqual = new ExecuteCall(new TmpScoreWrite(baseName, "1"))
                    .addCondition(new ScoreNotEqualCondition(leftName, rightName));

            return new BooleanExpressionPart(setTrueWhenNotEqual)
                    .addSetup(left)
                    .addSetup(right)
                    .addSetup(setFalse);
        }

        return part;
    }


    private ICommandPart generateNumberExpression(
            Expression expression,
            MCFunctionUnit unit,
            MCFunction function,
            PathStack pathStack,
            String baseName
    ) {
        if (expression instanceof NumberLiteralExpression number) {
            return new TmpScoreWrite(baseName, number.rawValue());
        }

        if (expression instanceof BinaryExpression binary) {
            Integer folded = tryFoldInt(binary);

            if (folded != null) {
                return new TmpScoreWrite(baseName, String.valueOf(folded));
            }
        }

        throw new IllegalStateException("Unsupported number expression: " + expression.getClass().getSimpleName());
    }

    private Integer tryFoldInt(Expression expression) {
        if (expression instanceof NumberLiteralExpression number) {
            return Integer.parseInt(number.rawValue());
        }

        if (!(expression instanceof BinaryExpression binary)) {
            return null;
        }

        Integer left = tryFoldInt(binary.left());
        Integer right = tryFoldInt(binary.right());

        if (left == null || right == null) {
            return null;
        }

        return switch (binary.operator()) {
            case ADD -> left + right;
            case SUBTRACT -> left - right;
            case MULTIPLY -> left * right;
            case DIVIDE -> left / right;
            case MODULO -> left % right;
            default -> null;
        };
    }
}
