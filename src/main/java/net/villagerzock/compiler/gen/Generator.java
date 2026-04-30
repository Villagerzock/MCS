package net.villagerzock.compiler.gen;

import net.villagerzock.compiler.ast.AstBuilder;
import net.villagerzock.compiler.ast.decl.*;
import net.villagerzock.compiler.ast.expr.*;
import net.villagerzock.compiler.ast.stmt.*;
import net.villagerzock.compiler.parser.MCSLexer;
import net.villagerzock.compiler.parser.MCSParser;
import net.villagerzock.compiler.semantic.MethodSymbol;
import net.villagerzock.mcfunction.ICommandPart;
import net.villagerzock.mcfunction.LightMCFunction;
import net.villagerzock.mcfunction.MCFunction;
import net.villagerzock.mcfunction.MCFunctionUnit;
import net.villagerzock.mcfunction.commandParts.*;
import net.villagerzock.mcfunction.valueTargeting.AbstractValueTarget;
import net.villagerzock.mcfunction.valueTargeting.DataValueTarget;
import net.villagerzock.mcfunction.valueTargeting.ScoreboardValueTarget;
import net.villagerzock.snbt.SnbtCompound;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.villagerzock.snbt.Snbt.*;

public class Generator {

    public MCFunctionUnit generate(List<ProgramNode> nodes) {
        PathStack pathStack = new PathStack();
        MCFunctionUnit unit = new MCFunctionUnit();

        for (ProgramNode node : nodes) {
            if (node.isLib()) continue;
            pathStack.push(node.packagePath().path());
            generateClasses(node, unit, pathStack);
            pathStack.pop();
        }

        for (ProgramNode node : nodes) {
            if (node.isLib()) continue;
            pathStack.push(node.packagePath().path());

            for (ClassDeclaration decl : node.classes()) {
                pathStack.push(decl.name());
                updateMembers(decl, unit, pathStack, node.packagePath().namespace());
                pathStack.pop();
            }

            pathStack.pop();
        }

        unit.analyze();
        return unit;
    }

    private void generateClasses(ProgramNode node, MCFunctionUnit unit, PathStack pathStack) {
        for (ClassDeclaration decl : node.classes()) {
            generateMember(decl, unit, node.packagePath().namespace(), pathStack);
        }
    }

    private void generateMembers(ClassDeclaration decl, MCFunctionUnit unit, String namespace, PathStack pathStack) {
        for (Declaration d : decl.members()) {
            generateMember(d, unit, namespace, pathStack);
        }
    }

    private void generateMember(Declaration decl, MCFunctionUnit unit, String namespace, PathStack pathStack) {
        if (decl instanceof ClassDeclaration classDeclaration) {
            pathStack.push(classDeclaration.name());
            generateMembers(classDeclaration, unit, namespace, pathStack);
            pathStack.pop();
        }

        if (decl instanceof MethodDeclaration methodDeclaration) {
            if (methodDeclaration.isNative()) {
                MCFunction function = unit.create(namespace, pathStack.getPath(), methodDeclaration.name() + "_native");
                methodDeclaration.nativeBody().setAssociatedFunction(function);
            } else {
                MCFunction function = unit.create(namespace, pathStack.getPath(), methodDeclaration.name() + "_entry");
                methodDeclaration.body().setAssociatedFunction(function);
            }
        }
        if (decl instanceof ConstructorDeclaration constructorDeclaration){
            MCFunction function = unit.create(namespace, pathStack.getPath(),  "init");
            constructorDeclaration.body().setAssociatedFunction(function);
        }
    }

    private void updateMembers(ClassDeclaration classDeclaration, MCFunctionUnit unit, PathStack pathStack, String namespace) {
        for (Declaration decl : classDeclaration.members()) {
            if (decl instanceof ClassDeclaration declaration) {
                pathStack.push(declaration.name());
                updateMembers(declaration, unit, pathStack, namespace);
                pathStack.pop();
            }

            if (decl instanceof MethodDeclaration methodDeclaration) {
                if (methodDeclaration.isNative()) {
                    generateNative(
                            methodDeclaration.nativeBody().getCode(),
                            unit,
                            methodDeclaration.getFunction(),
                            pathStack,
                            methodDeclaration.name(),
                            namespace
                    );
                } else {
                    generateBlock(
                            methodDeclaration.body(),
                            unit,
                            methodDeclaration.getFunction(),
                            pathStack,
                            methodDeclaration.name()
                    );
                }
            }
            if (decl instanceof ConstructorDeclaration constructorDeclaration){
                generateBlock(
                        constructorDeclaration.body(),
                        unit,
                        constructorDeclaration.getFunction(),
                        pathStack,
                        "init"
                );
            }
        }
    }

    private void generateNative(
            String nativeCode,
            MCFunctionUnit unit,
            MCFunction function,
            PathStack pathStack,
            String baseName,
            String namespace
    ) {
        String[] lines = nativeCode.split("\n");
        Pattern pattern = Pattern.compile("\\$\\{([^}]*)\\}");
        AstBuilder astBuilder = new AstBuilder();

        boolean useNewFunction = false;
        int macroCount = 0;

        Map<String, String> expressionToMacroMap = new HashMap<>();

        for (int i = 0; i < lines.length; i++) {
            Matcher matcher = pattern.matcher(lines[i]);

            while (matcher.find()) {
                String expression = matcher.group(1);

                if (expressionToMacroMap.containsKey(expression)) {
                    lines[i] = lines[i].replace(
                            "${%s}".formatted(expression),
                            "$(%s)".formatted(expressionToMacroMap.get(expression))
                    );

                    if (!lines[i].startsWith("$")) {
                        lines[i] = "$%s".formatted(lines[i]);
                    }

                    continue;
                }

                useNewFunction = true;

                MCSLexer lexer = new MCSLexer(CharStreams.fromString(expression));
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                MCSParser parser = new MCSParser(tokens);

                Expression expr = (Expression) astBuilder.visit(parser.inlineExpression());

                String macroName = "m_%s".formatted(macroCount++);

                function.addCommand(generateExpression(
                        expr,
                        unit,
                        function,
                        pathStack,
                        new DataValueTarget("storage mcs:memory stack[0].macro." + macroName),
                        baseName + "_macro_" + macroName
                ));

                lines[i] = lines[i].replace(
                        "${%s}".formatted(expression),
                        "$(%s)".formatted(macroName)
                );

                expressionToMacroMap.put(expression, macroName);

                if (!lines[i].startsWith("$")) {
                    lines[i] = "$%s".formatted(lines[i]);
                }
            }
        }

        MCFunction functionWrite = function;

        if (useNewFunction) {
            function.setUsesMacros(true);
            functionWrite = unit.create(namespace, pathStack.getPath(), baseName + "_native");
            function.addCommand(new FunctionCall(functionWrite));
        }

        functionWrite.addCommand(new NativePart(String.join("\n", lines)));
    }

    private void generateBlock(BlockStatement stmt, MCFunctionUnit unit, MCFunction function, PathStack pathStack, String baseName) {
        for (Statement statement : stmt.statements()) {
            generateStatement(statement, unit, function, pathStack, baseName);
        }
    }

    private void generateStatement(
            Statement statement,
            MCFunctionUnit unit,
            MCFunction function,
            PathStack pathStack,
            String baseName
    ) {
        if (statement instanceof VariableDeclarationStatement varDecl){
            String varName = varDecl.name();
            Expression init;
            if (varDecl.initializer() != null){
                init = varDecl.initializer();
            }else {
                init = varDecl.type().resolvedType().getDefaultExpression();
            }
            function.addCommand(generateExpression(
                    init,
                    unit,
                    function,
                    pathStack,
                    new DataValueTarget(localPath(varName)),
                    baseName+"_var_"+varName
            ));
        }
        if (statement instanceof ExpressionStatement expressionStatement) {
            Expression exp = expressionStatement.expression();

            if (exp instanceof CallExpression callExpression) {
                function.addCommand(generateCallStatement(
                        callExpression,
                        unit,
                        function,
                        pathStack,
                        baseName + "_call"
                ));
                return;
            }

            if (exp instanceof AssignmentExpression assignmentExpression) {
                Expression target = assignmentExpression.target();
                Expression value = assignmentExpression.value();

                if (!(target instanceof IdentifierExpression identifier)) {
                    throw new IllegalStateException("Unsupported assignment target: " + target.getClass().getSimpleName());
                }

                String varName = identifier.name();

                function.addCommand(generateExpression(
                        value,
                        unit,
                        function,
                        pathStack,
                        new DataValueTarget(localPath(varName)),
                        baseName + "_assign_" + varName
                ));

                return;
            }

            if (exp instanceof UpdateExpression updateExpression) {
                Expression target = updateExpression.target();

                if (!(target instanceof IdentifierExpression identifier)) {
                    throw new IllegalStateException("Unsupported update target: " + target.getClass().getSimpleName());
                }

                String varName = identifier.name();
                String tmpName = baseName + "_update_" + varName;

                function.addCommand(new ScoreboardValueTarget(tmpName).storeFrom(new DataValueTarget(localPath(varName))));

                if (updateExpression.operator() == UpdateOperator.INCREMENT) {
                    function.addCommand(new ScoreAddValue(tmpName, 1));
                } else if (updateExpression.operator() == UpdateOperator.DECREMENT) {
                    function.addCommand(new ScoreRemoveValue(tmpName, 1));
                } else {
                    throw new IllegalStateException("Unsupported update operator: " + updateExpression.operator());
                }

                function.addCommand(new DataValueTarget(localPath(varName)).storeFrom(new ScoreboardValueTarget(tmpName)));

                return;
            }

            throw new IllegalStateException("Unsupported expression statement: " + exp.getClass().getSimpleName());
        }

        if (statement instanceof IfStatement ifStatement) {
            String condName = baseName + "_if_cond";

            function.addCommand(generateExpression(
                    ifStatement.condition(),
                    unit,
                    function,
                    pathStack,
                    new ScoreboardValueTarget(condName),
                    condName
            ));

            if (ifStatement.thenBranch() instanceof BlockStatement thenBlock) {
                MCFunction thenFunction = unit.create(function.getNamespace(), function.getPath(), baseName + "_if");
                thenBlock.setAssociatedFunction(thenFunction);
                generateBlock(thenBlock, unit, thenFunction, pathStack, baseName);

                function.addCommand(
                        new ExecuteCall(new FunctionCall(thenFunction, 1))
                                .addCondition(new ScoreMatchesCondition(condName, "1"))
                );
            }

            if (ifStatement.elseBranch() instanceof BlockStatement elseBlock) {
                MCFunction elseFunction = unit.create(function.getNamespace(), function.getPath(), baseName + "_else");
                elseBlock.setAssociatedFunction(elseFunction);
                generateBlock(elseBlock, unit, elseFunction, pathStack, baseName);

                function.addCommand(
                        new ExecuteCall(new FunctionCall(elseFunction, 1))
                                .addCondition(new ScoreMatchesCondition(condName, "0"))
                );
            }

            return;
        }

        if (statement instanceof WhileStatement whileStatement) {
            Statement body = whileStatement.body();

            if (body instanceof BlockStatement blockStatement) {
                MCFunction f = unit.create(function.getNamespace(), function.getPath(), baseName + "_while");
                blockStatement.setAssociatedFunction(f);
                generateBlock(blockStatement, unit, f, pathStack, baseName);

                String condName = baseName + "_while_cond";

                function.addCommand(generateExpression(
                        whileStatement.condition(),
                        unit,
                        function,
                        pathStack,
                        new ScoreboardValueTarget(condName),
                        condName
                ));

                f.addCommand(generateExpression(
                        whileStatement.condition(),
                        unit,
                        f,
                        pathStack,
                        new ScoreboardValueTarget(condName),
                        condName
                ));

                f.addCommand(
                        new ExecuteCall(new FunctionCall(f, 1))
                                .addCondition(new ScoreMatchesCondition(condName, "1"))
                );

                function.addCommand(
                        new ExecuteCall(new FunctionCall(f, 1))
                                .addCondition(new ScoreMatchesCondition(condName, "1"))
                );
            }
        }

        if (statement instanceof ForStatement forStatement) {
            Statement body = forStatement.body();

            if (forStatement.initializer() != null) {
                generateStatement(forStatement.initializer(), unit, function, pathStack, baseName + "_for_init");
            }

            if (body instanceof BlockStatement blockStatement) {
                MCFunction f = unit.create(function.getNamespace(), function.getPath(), baseName + "_for");
                blockStatement.setAssociatedFunction(f);

                String condName = baseName + "_for_cond";

                if (forStatement.condition() != null) {
                    function.addCommand(generateExpression(
                            forStatement.condition(),
                            unit,
                            function,
                            pathStack,
                            new ScoreboardValueTarget(condName),
                            condName
                    ));
                } else {
                    function.addCommand(new ScoreSet(condName, 1));
                }

                generateBlock(blockStatement, unit, f, pathStack, baseName);

                if (forStatement.update() != null) {
                    generateStatement(
                            new ExpressionStatement(forStatement.update()),
                            unit,
                            f,
                            pathStack,
                            baseName + "_for_update"
                    );
                }

                if (forStatement.condition() != null) {
                    f.addCommand(generateExpression(
                            forStatement.condition(),
                            unit,
                            f,
                            pathStack,
                            new ScoreboardValueTarget(condName),
                            condName
                    ));
                } else {
                    f.addCommand(new ScoreSet(condName, 1));
                }

                f.addCommand(
                        new ExecuteCall(new FunctionCall(f, 1))
                                .addCondition(new ScoreMatchesCondition(condName, "1"))
                );

                function.addCommand(
                        new ExecuteCall(new FunctionCall(f, 1))
                                .addCondition(new ScoreMatchesCondition(condName, "1"))
                );
            }
        }
    }

    private ICommandPart generateCallStatement(
            CallExpression callExpression,
            MCFunctionUnit unit,
            MCFunction function,
            PathStack pathStack,
            String baseName
    ) {
        MethodSymbol method = callExpression.resolvedMethod();
        SnbtCompound locals = compound();

        List<DelayedParam> delayedParams = new ArrayList<>();

        for (int i = 0; i < callExpression.arguments().size(); i++) {
            Expression param = callExpression.arguments().get(i);
            String name = method.declaration().parameters().get(i).name();
            if (param instanceof NullLiteralExpression){
                continue;
            }
            if (param instanceof StringLiteralExpression stringLiteralExpression) {
                locals.putString(name, stringLiteralExpression.rawValue());
            } else if (param instanceof NumberLiteralExpression number) {
                String raw = number.rawValue();

                if (raw.endsWith("f") || raw.endsWith("F")) {
                    locals.put(name, floatNumber(Float.parseFloat(raw.substring(0, raw.length() - 1))));
                } else if (raw.contains(".")) {
                    locals.put(name, doubleNumber(Double.parseDouble(raw)));
                } else {
                    locals.put(name, integer(Integer.parseInt(raw)));
                }
            } else if (param instanceof BooleanLiteralExpression bool) {
                locals.put(name, bool(bool.value()));
            } else {
                delayedParams.add(new DelayedParam(name, param));
            }
        }

        SnbtCompound frame = compound()
                .put("locals", locals)
                .put("macro", compound());

        List<ICommandPart> commands = new ArrayList<>();

        commands.add(new TmpWrite(frame.toSnbt()));

        for (DelayedParam delayed : delayedParams) {
            commands.add(generateExpression(
                    delayed.expression(),
                    unit,
                    function,
                    pathStack,
                    new DataValueTarget("tmp.locals." + delayed.name()),
                    baseName + "_param_" + delayed.name()
            ));
        }

        commands.add(new CreateStackFrame());
        commands.add(new FunctionCall(method.declaration().getFunction()));
        commands.add(new PopStackFrame());

        return new MultiPart(commands);
    }

    private record DelayedParam(String name, Expression expression) {
    }

    private record InlineMacro(String name, Expression expression) {
    }

    private ICommandPart generateExpression(
            Expression expression,
            MCFunctionUnit unit,
            MCFunction function,
            PathStack pathStack,
            AbstractValueTarget target,
            String baseName
    ) {
        if (expression instanceof NumberLiteralExpression number) {
            ScoreboardValueTarget tmp = new ScoreboardValueTarget(baseName);

            return new MultiPart(List.of(
                    new TmpScoreWrite(baseName, number.rawValue()),
                    target.storeFrom(tmp)
            ));
        }

        if (expression instanceof BooleanLiteralExpression bool) {
            ScoreboardValueTarget tmp = new ScoreboardValueTarget(baseName);

            return new MultiPart(List.of(
                    new TmpScoreWrite(baseName, bool.value() ? "1" : "0"),
                    target.storeFrom(tmp)
            ));
        }
        if (expression instanceof TStringLiteralExpression string) {
            SnbtCompound compound = compound();

            String raw = string.rawValue();
            StringBuilder resultString = new StringBuilder();

            List<InlineMacro> macros = new ArrayList<>();

            int macroIndex = 0;
            int i = 0;

            while (i < raw.length()) {
                int start = raw.indexOf("${", i);

                if (start == -1) {
                    resultString.append(raw.substring(i));
                    break;
                }

                resultString.append(raw, i, start);

                int end = raw.indexOf("}", start + 2);
                if (end == -1) {
                    throw new IllegalStateException("Unclosed ${ in tString: " + raw);
                }

                String inlineSource = raw.substring(start + 2, end);
                String macroName = "m_" + macroIndex++;

                Expression inlineExpression = parseInlineExpression(inlineSource);
                macros.add(new InlineMacro(macroName, inlineExpression));

                resultString.append("$(").append(macroName).append(")");

                i = end + 1;
            }

            compound.put("text", string(resultString.toString()));

            // tmp = { text: "... $(m_0) ...", ... }
            function.addCommand(new TmpMacroWrite(compound.toSnbt()));

            // tmp.m_X = result of ${...}
            for (InlineMacro macro : macros) {
                function.addCommand(generateExpression(
                        macro.expression(),
                        unit,
                        function,
                        pathStack,
                        new DataValueTarget("storage mcs:memory tmp_macro." + macro.name()),
                        baseName + "_tstr_" + macro.name()
                ));
            }

            MCFunction macroFunction = unit.create(
                    function.getNamespace(),
                    function.getPath(),
                    baseName + "_tstr"
            );

            ICommandPart writeResult = target.storeFrom(
                    new StringValueTarget(resultString.toString())
            );

            macroFunction.addCommand(new MacroCommandPart(writeResult));

            return new FunctionCall(macroFunction, 2);
        }
        if (expression instanceof StringLiteralExpression string) {
            return target.storeFrom(new StringValueTarget(string.rawValue()));
        }

        if (expression instanceof IdentifierExpression identifier) {
            return target.storeFrom(new DataValueTarget(localPath(identifier.name())));
        }

        if (expression instanceof CallExpression callExpression) {
            return generateCallExpression(callExpression, unit, function, pathStack, target, baseName);
        }

        if (expression instanceof UnaryExpression unaryExpression) {
            return generateUnaryExpression(unaryExpression, unit, function, pathStack, target, baseName);
        }

        if (expression instanceof GroupExpression groupExpression) {
            return generateExpression(groupExpression.expression(), unit, function, pathStack, target, baseName);
        }

        if (expression instanceof BinaryExpression binary) {
            return switch (binary.operator()) {
                case ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO ->
                        generateArithmeticExpression(binary, unit, function, pathStack, target, baseName);

                case GREATER, LESS, GREATER_EQUAL, LESS_EQUAL, EQUAL, NOT_EQUAL ->
                        generateCompareExpression(binary, unit, function, pathStack, target, baseName);

                case LOGICAL_AND ->
                        generateAndExpression(binary, unit, function, pathStack, target, baseName);

                case LOGICAL_OR ->
                        generateOrExpression(binary, unit, function, pathStack, target, baseName);

                default -> throw new IllegalStateException("Unsupported binary operator: " + binary.operator());
            };
        }

        if (expression instanceof NewExpression newExpression) {
            ConstructorDeclaration constructorDeclaration = newExpression.getResolvedConstructor().declaration();

            String allocScore = baseName + "_alloc";

            SnbtCompound locals = compound()
                    .put("macro", compound());

            List<ICommandPart> commands = new ArrayList<>();

            // 1. allocate() ausführen und neuen Heap-Index holen
            commands.add(new ExecuteStoreResultScore(
                    allocScore,
                    new FunctionCall(new LightMCFunction("std:std/std/allocate_native"))
            ));

            // 2. Ergebnis von new-expression speichern
            commands.add(target.storeFrom(new ScoreboardValueTarget(allocScore)));

            // 3. this in Constructor-Frame schreiben
            commands.add(new TmpWrite(compound()
                    .put("locals", compound())
                    .put("macro", compound())
                    .toSnbt()
            ));

            commands.add(new ScoreToData(
                    allocScore,
                    "storage mcs:memory tmp.locals.this"
            ));

            // 4. Constructor-Argumente schreiben
            for (int i = 0; i < newExpression.arguments().size(); i++) {
                Expression arg = newExpression.arguments().get(i);
                String paramName = constructorDeclaration.parameters().get(i).name();

                commands.add(generateExpression(
                        arg,
                        unit,
                        function,
                        pathStack,
                        new DataValueTarget("storage mcs:memory tmp.locals." + paramName),
                        baseName + "_ctor_param_" + paramName
                ));
            }

            // 5. Constructor ausführen
            commands.add(new CreateStackFrame());
            commands.add(new FunctionCall(constructorDeclaration.getFunction()));
            commands.add(new PopStackFrame());

            return new MultiPart(commands);
        }

        throw new IllegalStateException("Unsupported expression: " + expression.getClass().getSimpleName());
    }

    private Expression parseInlineExpression(String source) {
        CharStream input = CharStreams.fromString(source);

        MCSLexer lexer = new MCSLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MCSParser parser = new MCSParser(tokens);

        MCSParser.InlineExpressionContext ctx = parser.inlineExpression();

        if (parser.getNumberOfSyntaxErrors() > 0) {
            throw new IllegalStateException("Invalid inline expression in tString: " + source);
        }

        AstBuilder builder = new AstBuilder();
        return builder.visitInlineExpression(ctx);
    }

    private ICommandPart generateCallExpression(
            CallExpression callExpression,
            MCFunctionUnit unit,
            MCFunction function,
            PathStack pathStack,
            AbstractValueTarget target,
            String baseName
    ) {
        MethodSymbol method = callExpression.resolvedMethod();
        SnbtCompound locals = compound();

        for (int i = 0; i < callExpression.arguments().size(); i++) {
            Expression param = callExpression.arguments().get(i);
            String name = method.declaration().parameters().get(i).name();

            if (param instanceof StringLiteralExpression stringLiteralExpression) {
                locals.putString(name, stringLiteralExpression.rawValue());
                continue;
            }

            throw new IllegalStateException("Only string literal call arguments are supported right now");
        }

        SnbtCompound frame = compound()
                .put("locals", locals)
                .put("macro", compound());

        ScoreboardValueTarget tmp = new ScoreboardValueTarget(baseName);

        return new MultiPart(List.of(
                new TmpWrite(frame.toSnbt()),
                new CreateStackFrame(),
                new ExecuteStoreResultScore(baseName, new FunctionCall(method.declaration().getFunction())),
                new PopStackFrame(),
                target.storeFrom(tmp)
        ));
    }

    private ICommandPart generateUnaryExpression(
            UnaryExpression expression,
            MCFunctionUnit unit,
            MCFunction function,
            PathStack pathStack,
            AbstractValueTarget target,
            String baseName
    ) {
        String valueName = baseName + "_value";
        ScoreboardValueTarget result = new ScoreboardValueTarget(baseName);

        return switch (expression.operator()) {
            case NEGATE -> new MultiPart(List.of(
                    generateExpression(expression.operand(), unit, function, pathStack, new ScoreboardValueTarget(valueName), valueName),
                    new TmpScoreWrite(baseName, "0"),
                    new ScoreOperation(baseName, "-=", valueName),
                    target.storeFrom(result)
            ));

            case NOT -> new MultiPart(List.of(
                    generateExpression(expression.operand(), unit, function, pathStack, new ScoreboardValueTarget(valueName), valueName),
                    new TmpScoreWrite(baseName, "1"),
                    new ScoreOperation(baseName, "-=", valueName),
                    target.storeFrom(result)
            ));
        };
    }

    private ICommandPart generateArithmeticExpression(
            BinaryExpression expression,
            MCFunctionUnit unit,
            MCFunction function,
            PathStack pathStack,
            AbstractValueTarget target,
            String baseName
    ) {
        String leftName = baseName + "_left";
        String rightName = baseName + "_right";

        String op = switch (expression.operator()) {
            case ADD -> "+=";
            case SUBTRACT -> "-=";
            case MULTIPLY -> "*=";
            case DIVIDE -> "/=";
            case MODULO -> "%=";
            default -> throw new IllegalStateException("Unsupported arithmetic operator: " + expression.operator());
        };

        return new MultiPart(List.of(
                generateExpression(expression.left(), unit, function, pathStack, new ScoreboardValueTarget(leftName), leftName),
                generateExpression(expression.right(), unit, function, pathStack, new ScoreboardValueTarget(rightName), rightName),
                new ScoreCopy(baseName, leftName),
                new ScoreOperation(baseName, op, rightName),
                target.storeFrom(new ScoreboardValueTarget(baseName))
        ));
    }

    private ICommandPart generateCompareExpression(
            BinaryExpression expression,
            MCFunctionUnit unit,
            MCFunction function,
            PathStack pathStack,
            AbstractValueTarget target,
            String baseName
    ) {
        String leftName = baseName + "_left";
        String rightName = baseName + "_right";

        ICommandPart setFalse = new TmpScoreWrite(baseName, "0");

        ICommandPart setTrue;

        if (expression.operator() == BinaryOperator.NOT_EQUAL) {
            setTrue = new ExecuteCall(new TmpScoreWrite(baseName, "1"))
                    .addCondition(new ScoreNotEqualCondition(leftName, rightName));
        } else {
            setTrue = new ExecuteCall(new TmpScoreWrite(baseName, "1"))
                    .addCondition(new ScoreCompareCondition(leftName, mapCompareOperator(expression.operator()), rightName));
        }

        return new MultiPart(List.of(
                generateExpression(expression.left(), unit, function, pathStack, new ScoreboardValueTarget(leftName), leftName),
                generateExpression(expression.right(), unit, function, pathStack, new ScoreboardValueTarget(rightName), rightName),
                setFalse,
                setTrue,
                target.storeFrom(new ScoreboardValueTarget(baseName))
        ));
    }

    private String mapCompareOperator(BinaryOperator operator) {
        return switch (operator) {
            case GREATER -> ">";
            case LESS -> "<";
            case GREATER_EQUAL -> ">=";
            case LESS_EQUAL -> "<=";
            case EQUAL -> "=";
            default -> throw new IllegalStateException("Unsupported compare operator: " + operator);
        };
    }

    private ICommandPart generateAndExpression(
            BinaryExpression expression,
            MCFunctionUnit unit,
            MCFunction function,
            PathStack pathStack,
            AbstractValueTarget target,
            String baseName
    ) {
        String leftName = baseName + "_left";
        String rightName = baseName + "_right";

        return new MultiPart(List.of(
                generateExpression(expression.left(), unit, function, pathStack, new ScoreboardValueTarget(leftName), leftName),
                generateExpression(expression.right(), unit, function, pathStack, new ScoreboardValueTarget(rightName), rightName),
                new ScoreCopy(baseName, leftName),
                new ScoreOperation(baseName, "*=", rightName),
                target.storeFrom(new ScoreboardValueTarget(baseName))
        ));
    }

    private ICommandPart generateOrExpression(
            BinaryExpression expression,
            MCFunctionUnit unit,
            MCFunction function,
            PathStack pathStack,
            AbstractValueTarget target,
            String baseName
    ) {
        String leftName = baseName + "_left";
        String rightName = baseName + "_right";

        return new MultiPart(List.of(
                generateExpression(expression.left(), unit, function, pathStack, new ScoreboardValueTarget(leftName), leftName),
                generateExpression(expression.right(), unit, function, pathStack, new ScoreboardValueTarget(rightName), rightName),
                new ScoreCopy(baseName, leftName),
                new ScoreOperation(baseName, "+=", rightName),
                new ExecuteCall(new TmpScoreWrite(baseName, "1"))
                        .addCondition(new ScoreMatchesCondition(baseName, "2..")),
                target.storeFrom(new ScoreboardValueTarget(baseName))
        ));
    }

    private String localPath(String localName) {
        return "storage mcs:memory stack[0].locals." + localName;
    }

    private String macroPath(String macroName) {
        return "storage mcs:memory stack[0].macro." + macroName;
    }

    private String quoteString(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"") + "\"";
    }

    public class StringValueTarget extends AbstractValueTarget {
        private final String value;

        public StringValueTarget(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        @Override
        protected ICommandPart iStoreFrom(AbstractValueTarget source) {
            return null;
        }

        @Override
        protected ICommandPart iStoreTo(AbstractValueTarget target) {
            if (target instanceof DataValueTarget dataTarget) {
                return new DataSetValue(dataTarget.path(), quoteString(value));
            }

            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof StringValueTarget dataValueTarget){
                return Objects.equals(dataValueTarget.value, this.value);
            }
            return false;
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

    public record ExecuteStoreResultScore(String scoreName, ICommandPart command) implements ICommandPart {
        @Override
        public String apply() {
            return "execute store result score #" + scoreName + " mcs_tmp run " + command.apply();
        }
    }

    public record ScoreToData(String scoreName, String dataPath) implements ICommandPart {
        @Override
        public String apply() {
            return "execute store result " + dataPath + " int 1 run scoreboard players get #" + scoreName + " mcs_tmp";
        }
    }

    public record DataToScore(String dataPath, String scoreName) implements ICommandPart {
        @Override
        public String apply() {
            return "execute store result score #" + scoreName + " mcs_tmp run data get " + dataPath;
        }
    }

    public record DataToData(String sourcePath, String targetPath) implements ICommandPart {
        @Override
        public String apply() {
            return "data modify " + targetPath + " set from " + sourcePath;
        }
    }

    public record DataSetValue(String targetPath, String value) implements ICommandPart {
        @Override
        public String apply() {
            return "data modify " + targetPath + " set value " + value;
        }
    }
}