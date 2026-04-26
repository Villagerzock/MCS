package net.villagerzock.compiler.gen;

import net.villagerzock.compiler.ast.decl.ClassDeclaration;
import net.villagerzock.compiler.ast.decl.Declaration;
import net.villagerzock.compiler.ast.decl.MethodDeclaration;
import net.villagerzock.compiler.ast.decl.ProgramNode;
import net.villagerzock.compiler.ast.expr.*;
import net.villagerzock.compiler.ast.stmt.*;
import net.villagerzock.compiler.semantic.MethodSymbol;
import net.villagerzock.mcfunction.ICommandPart;
import net.villagerzock.mcfunction.MCFunction;
import net.villagerzock.mcfunction.MCFunctionUnit;
import net.villagerzock.mcfunction.commandParts.*;
import net.villagerzock.snbt.SnbtCompound;

import java.util.List;

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

                for (int i = 0; i < callExpression.arguments().size(); i++){
                    Expression param = callExpression.arguments().get(i);
                    String name = method.declaration().parameters().get(i).name();

                    if (param instanceof StringLiteralExpression stringLiteralExpression){
                        locals.putString(name, stringLiteralExpression.rawValue());
                    }
                }

                SnbtCompound compound = compound()
                        .put("locals", locals)
                        .put("macro", compound());

                function.addCommand(new TmpWrite(compound.toSnbt()));
                function.addCommand(new CreateStackFrame());
                function.addCommand(new FunctionCall(method.declaration().getFunction()));
                function.addCommand(new PopStackFrame());

                return;
            }

            if (exp instanceof AssignmentExpression assignmentExpression) {
                Expression target = assignmentExpression.target();
                Expression value = assignmentExpression.value();

                if (!(target instanceof IdentifierExpression identifier)) {
                    throw new IllegalStateException("Unsupported assignment target: " + target.getClass().getSimpleName());
                }

                String varName = identifier.name();
                String tmpName = baseName + "_assign_" + varName;

                function.addCommand(generateNumberExpression(
                        value,
                        unit,
                        function,
                        pathStack,
                        tmpName
                ));

                function.addCommand(new TmpScoreToLocal(tmpName, varName));

                return;
            }

            if (exp instanceof UpdateExpression updateExpression) {
                Expression target = updateExpression.target(); // ggf. Getter-Name anpassen

                if (!(target instanceof IdentifierExpression identifier)) {
                    throw new IllegalStateException("Unsupported update target: " + target.getClass().getSimpleName());
                }

                String varName = identifier.name();
                String tmpName = baseName + "_update_" + varName;

                function.addCommand(new LocalToTmpScore(varName, tmpName));

                if (updateExpression.operator() == UpdateOperator.INCREMENT) {
                    function.addCommand(new ScoreAddValue(tmpName, 1));
                } else if (updateExpression.operator() == UpdateOperator.DECREMENT) {
                    function.addCommand(new ScoreRemoveValue(tmpName, 1));
                } else {
                    throw new IllegalStateException("Unsupported update operator: " + updateExpression.operator());
                }

                function.addCommand(new TmpScoreToLocal(tmpName, varName));

                return;
            }

            throw new IllegalStateException("Unsupported expression statement: " + exp.getClass().getSimpleName());
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
        if (statement instanceof WhileStatement whileStatement){
            Statement body = whileStatement.body();
            if (body instanceof BlockStatement blockStatement){
                MCFunction f = unit.create(function.getNamespace(), function.getPath(), baseName + "_while");
                blockStatement.setAssociatedFunction(f);
                generateBlock(blockStatement,unit,f,pathStack,baseName);



                String condName = baseName + "_while_cond";

                ICommandPart conditionExpr = generateExpression(
                        whileStatement.condition(),
                        unit,
                        function,
                        pathStack,
                        condName
                );

                ICommandPart repeatConditionExpr = generateExpression(
                        whileStatement.condition(),
                        unit,
                        f,
                        pathStack,
                        condName
                );

                function.addCommand(conditionExpr);
                f.addCommand(repeatConditionExpr);

                f.addCommand(
                        new ExecuteCall(new FunctionCall(f, true))
                                .addCondition(new ScoreMatchesCondition(condName, "1"))
                );

                function.addCommand(
                        new ExecuteCall(new FunctionCall(f, true))
                                .addCondition(new ScoreMatchesCondition(condName, "1"))
                );
            }
        }
        if (statement instanceof ForStatement forStatement) {
            Statement body = forStatement.body();

            // 1. init nur EINMAL im aktuellen function-context ausführen
            if (forStatement.initializer() != null) {
                generateStatement(forStatement.initializer(), unit, function, pathStack, baseName + "_for_init");
            }

            if (body instanceof BlockStatement blockStatement) {
                MCFunction f = unit.create(function.getNamespace(), function.getPath(), baseName + "_for");
                blockStatement.setAssociatedFunction(f);

                String condName = baseName + "_for_cond";

                // 2. condition vor dem ersten call im caller berechnen
                ICommandPart conditionExpr;

                if (forStatement.condition() != null) {
                    conditionExpr = generateExpression(
                            forStatement.condition(),
                            unit,
                            function,
                            pathStack,
                            condName
                    );
                } else {
                    conditionExpr = new ScoreSet(condName, 1); // falls for(;;)
                }

                function.addCommand(conditionExpr);

                // 3. body in der for-function generieren
                generateBlock(blockStatement, unit, f, pathStack, baseName);

                // 4. update NACH body ausführen
                if (forStatement.update() != null) {
                    generateStatement(new ExpressionStatement(forStatement.update()), unit, f, pathStack, baseName + "_for_update");
                }

                // 5. condition erneut am Ende der for-function berechnen
                ICommandPart repeatConditionExpr;

                if (forStatement.condition() != null) {
                    repeatConditionExpr = generateExpression(
                            forStatement.condition(),
                            unit,
                            f,
                            pathStack,
                            condName
                    );
                } else {
                    repeatConditionExpr = new ScoreSet(condName, 1);
                }

                f.addCommand(repeatConditionExpr);

                // 6. solange condition true ist, ruft sich die for-function wieder auf
                f.addCommand(
                        new ExecuteCall(new FunctionCall(f, true))
                                .addCondition(new ScoreMatchesCondition(condName, "1"))
                );

                // 7. erster Einstieg
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

        if (expression instanceof IdentifierExpression identifier) {
            return new LocalToTmpScore(identifier.name(), baseName);
        }

        if (expression instanceof BinaryExpression binary) {
            Integer folded = tryFoldInt(binary);

            if (folded != null) {
                return new TmpScoreWrite(baseName, String.valueOf(folded));
            }

            String leftName = baseName + "_left";
            String rightName = baseName + "_right";

            ICommandPart left = generateNumberExpression(binary.left(), unit, function, pathStack, leftName);
            ICommandPart right = generateNumberExpression(binary.right(), unit, function, pathStack, rightName);

            String op = switch (binary.operator()) {
                case ADD -> "+=";
                case SUBTRACT -> "-=";
                case MULTIPLY -> "*=";
                case DIVIDE -> "/=";
                case MODULO -> "%=";
                default -> throw new IllegalStateException("Unsupported number binary operator: " + binary.operator());
            };

            return new MultiPart(List.of(
                    left,
                    right,
                    new ScoreCopy(baseName, leftName),
                    new ScoreOperation(baseName, op, rightName)
            ));
        }

        throw new IllegalStateException("Unsupported number expression: " + expression.getClass().getSimpleName());
    }

    public record LocalToTmpScore(String localName, String scoreName) implements ICommandPart {
        @Override
        public String apply() {
            return "execute store result score #" + scoreName + " mcs_tmp run data get storage mcs:memory stack[0].locals." + localName;
        }
    }

    public record MultiPart(List<ICommandPart> parts) implements ICommandPart {
        @Override
        public String apply() {
            StringBuilder builder = new StringBuilder();
            for (ICommandPart part : parts) {
                builder.append(part.apply()).append("\n");
            }
            return builder.toString().stripTrailing();
        }
    }

    public record TmpScoreToLocal(String scoreName, String localName) implements ICommandPart {
        @Override
        public String apply() {
            return "execute store result storage mcs:memory stack[0].locals." + localName + " int 1 run scoreboard players get #" + scoreName + " mcs_tmp";
        }
    }

    public record ScoreOperation(String target, String op, String source) implements ICommandPart {
        @Override
        public String apply() {
            return "scoreboard players operation #" + target + " mcs_tmp " + op + " #" + source + " mcs_tmp";
        }
    }

    public record ScoreCopy(String target, String source) implements ICommandPart {
        @Override
        public String apply() {
            return "scoreboard players operation #" + target + " mcs_tmp = #" + source + " mcs_tmp";
        }
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
    public record ScoreAddValue(String scoreName, int value) implements ICommandPart {
        @Override
        public String apply() {
            return "scoreboard players add #" + scoreName + " mcs_tmp " + value;
        }
    }

    public record ScoreRemoveValue(String scoreName, int value) implements ICommandPart {
        @Override
        public String apply() {
            return "scoreboard players remove #" + scoreName + " mcs_tmp " + value;
        }
    }
}
