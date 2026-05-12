package net.villagerzock.compiler.optimization;

import net.villagerzock.compiler.ast.CompilationUnit;
import net.villagerzock.compiler.ast.decl.*;
import net.villagerzock.compiler.ast.expr.*;
import net.villagerzock.compiler.ast.stmt.*;

import java.util.*;

public class Optimizer {
    private static final int MAX_FOR_UNROLL_ITERATIONS = 100;
    private static final int MAX_FOR_PRECOMPUTE_ITERATIONS = 10_000;

    public void optimize(CompilationUnit compilationUnit) {
        preCalcExpressions(compilationUnit);
        optimizeForLoopsAway(compilationUnit);
        optimizeForLoops(compilationUnit);
        cleanUpUnused(compilationUnit);
    }

    private void optimizeForLoops(CompilationUnit compilationUnit) {
        rewriteCompilationUnit(compilationUnit, RewriteMode.UNROLL_FOR_LOOPS);
    }

    private void optimizeForLoopsAway(CompilationUnit compilationUnit) {
        rewriteCompilationUnit(compilationUnit, RewriteMode.PRECOMPUTE_FOR_LOOPS);
    }

    private void cleanUpUnused(CompilationUnit compilationUnit) {
        rewriteCompilationUnit(compilationUnit, RewriteMode.CLEANUP);
    }

    private void preCalcExpressions(CompilationUnit compilationUnit) {
        rewriteCompilationUnit(compilationUnit, RewriteMode.FOLD_EXPRESSIONS);
    }

    private void rewriteCompilationUnit(CompilationUnit compilationUnit, RewriteMode mode) {
        List<ProgramNode> optimizedPrograms = new ArrayList<>();

        for (ProgramNode program : compilationUnit.programs()) {
            optimizedPrograms.add(rewriteProgram(program, mode));
        }

        compilationUnit.setPrograms(optimizedPrograms);
    }

    private ProgramNode rewriteProgram(ProgramNode program, RewriteMode mode) {
        if (program.isLib()) {
            return program;
        }

        List<ClassDeclaration> classes = new ArrayList<>();
        for (ClassDeclaration classDeclaration : program.classes()) {
            classes.add(rewriteClass(classDeclaration, mode));
        }

        return new ProgramNode(
                program.packagePath(),
                program.imports(),
                classes,
                program.sourceRange(),
                program.staticImports()
        );
    }

    private ClassDeclaration rewriteClass(ClassDeclaration classDeclaration, RewriteMode mode) {
        List<Declaration> members = new ArrayList<>();
        for (Declaration member : classDeclaration.members()) {
            members.add(rewriteDeclaration(member, mode));
        }

        if (classDeclaration instanceof RecordDeclaration recordDeclaration) {
            return new RecordDeclaration(
                    recordDeclaration.name(),
                    recordDeclaration.components(),
                    members,
                    recordDeclaration.sourceRange()
            );
        }

        return new ClassDeclaration(classDeclaration.name(), members, classDeclaration.sourceRange());
    }

    private Declaration rewriteDeclaration(Declaration declaration, RewriteMode mode) {
        if (declaration instanceof ClassDeclaration classDeclaration) {
            return rewriteClass(classDeclaration, mode);
        }

        if (declaration instanceof FieldDeclaration field) {
            return new FieldDeclaration(
                    field.type(),
                    field.name(),
                    rewriteExpression(field.initializer(), mode),
                    field.sourceRange()
            );
        }

        if (declaration instanceof ConstructorDeclaration constructor) {
            ConstructorDeclaration result = new ConstructorDeclaration(
                    constructor.parameters(),
                    rewriteBlock(constructor.body(), mode),
                    constructor.sourceRange()
            );
            result.setImplicitRecordConstructor(constructor.isImplicitRecordConstructor());
            return result;
        }

        if (declaration instanceof MethodDeclaration method) {
            String nativeBody = method.nativeBody() == null ? null : method.nativeBody().getCode();
            return new MethodDeclaration(
                    method.modifiers(),
                    method.returnType(),
                    method.name(),
                    method.parameters(),
                    rewriteBlock(method.body(), mode),
                    nativeBody,
                    method.sourceRange()
            );
        }

        return declaration;
    }

    private BlockStatement rewriteBlock(BlockStatement block, RewriteMode mode) {
        if (block == null) {
            return null;
        }

        if (mode == RewriteMode.PRECOMPUTE_FOR_LOOPS) {
            return rewriteBlockWithLoopPrecomputation(block);
        }

        List<Statement> statements = new ArrayList<>();
        for (Statement statement : block.statements()) {
            Statement rewritten = rewriteStatement(statement, mode);
            if (rewritten instanceof BlockStatement nestedBlock && shouldFlatten(nestedBlock, mode)) {
                statements.addAll(nestedBlock.statements());
            } else if (rewritten != null) {
                statements.add(rewritten);
            }
        }

        if (mode == RewriteMode.CLEANUP) {
            statements = removeDeadAndUnusedStatements(statements);
        }

        return new BlockStatement(statements, block.sourceRange());
    }

    private Statement rewriteStatement(Statement statement, RewriteMode mode) {
        if (statement instanceof BlockStatement block) {
            return rewriteBlock(block, mode);
        }

        if (statement instanceof VariableDeclarationStatement variable) {
            VariableDeclarationStatement result = new VariableDeclarationStatement(
                    variable.type(),
                    variable.name(),
                    rewriteExpression(variable.initializer(), mode),
                    variable.sourceRange()
            );
            result.setConstant(variable.isConstant());
            return result;
        }

        if (statement instanceof ExpressionStatement expressionStatement) {
            Expression expression = rewriteExpression(expressionStatement.expression(), mode);
            if (mode == RewriteMode.CLEANUP && isPureExpression(expression)) {
                return null;
            }
            return new ExpressionStatement(expression, expressionStatement.sourceRange());
        }

        if (statement instanceof ReturnStatement returnStatement) {
            return new ReturnStatement(rewriteExpression(returnStatement.value(), mode), returnStatement.sourceRange());
        }

        if (statement instanceof IfStatement ifStatement) {
            Expression condition = rewriteExpression(ifStatement.condition(), mode);
            Statement thenBranch = rewriteStatement(ifStatement.thenBranch(), mode);
            Statement elseBranch = rewriteStatement(ifStatement.elseBranch(), mode);

            if (mode == RewriteMode.CLEANUP && condition instanceof BooleanLiteralExpression bool) {
                if (bool.value()) {
                    return thenBranch == null ? emptyBlock(ifStatement.sourceRange()) : thenBranch;
                }
                return elseBranch == null ? emptyBlock(ifStatement.sourceRange()) : elseBranch;
            }

            return new IfStatement(condition, thenBranch, elseBranch, ifStatement.sourceRange());
        }

        if (statement instanceof WhileStatement whileStatement) {
            Expression condition = rewriteExpression(whileStatement.condition(), mode);
            Statement body = rewriteStatement(whileStatement.body(), mode);

            if (mode == RewriteMode.CLEANUP
                    && condition instanceof BooleanLiteralExpression bool
                    && !bool.value()) {
                return emptyBlock(whileStatement.sourceRange());
            }

            return new WhileStatement(condition, body, whileStatement.sourceRange());
        }

        if (statement instanceof ForStatement forStatement) {
            Statement initializer = rewriteStatement(forStatement.initializer(), mode);
            Expression condition = rewriteExpression(forStatement.condition(), mode);
            Expression update = rewriteExpression(forStatement.update(), mode);
            Statement body = rewriteStatement(forStatement.body(), mode);

            ForStatement rewritten = new ForStatement(
                    initializer,
                    condition,
                    update,
                    body,
                    forStatement.sourceRange()
            );

            if (mode == RewriteMode.UNROLL_FOR_LOOPS) {
                Statement unrolled = tryUnrollForLoop(rewritten);
                if (unrolled != null) {
                    return unrolled;
                }
            }

            if (mode == RewriteMode.CLEANUP
                    && condition instanceof BooleanLiteralExpression bool
                    && !bool.value()) {
                return initializer == null ? emptyBlock(forStatement.sourceRange()) : initializer;
            }

            return rewritten;
        }

        return statement;
    }

    private Expression rewriteExpression(Expression expression, RewriteMode mode) {
        if (expression == null) {
            return null;
        }

        Expression rewritten;
        if (expression instanceof BinaryExpression binary) {
            rewritten = new BinaryExpression(
                    rewriteExpression(binary.left(), mode),
                    binary.operator(),
                    rewriteExpression(binary.right(), mode),
                    binary.sourceRange()
            );
        } else if (expression instanceof UnaryExpression unary) {
            rewritten = new UnaryExpression(
                    unary.operator(),
                    rewriteExpression(unary.operand(), mode),
                    unary.sourceRange()
            );
        } else if (expression instanceof GroupExpression group) {
            rewritten = new GroupExpression(
                    rewriteExpression(group.expression(), mode),
                    group.sourceRange()
            );
        } else if (expression instanceof AssignmentExpression assignment) {
            rewritten = new AssignmentExpression(
                    rewriteExpression(assignment.target(), mode),
                    rewriteExpression(assignment.value(), mode),
                    assignment.sourceRange()
            );
        } else if (expression instanceof UpdateExpression update) {
            rewritten = new UpdateExpression(
                    rewriteExpression(update.target(), mode),
                    update.operator(),
                    update.sourceRange()
            );
        } else if (expression instanceof CallExpression call) {
            rewritten = rewriteCall(call, mode);
        } else if (expression instanceof SelectExpression select) {
            rewritten = rewriteSelect(select, mode);
        } else if (expression instanceof NewExpression newExpression) {
            rewritten = rewriteNew(newExpression, mode);
        } else if (expression instanceof ArrayLiteralExpression arrayLiteral) {
            rewritten = rewriteArrayLiteral(arrayLiteral, mode);
        } else if (expression instanceof CompoundLiteralExpression compoundLiteral) {
            rewritten = rewriteCompoundLiteral(compoundLiteral, mode);
        } else {
            rewritten = expression;
        }

        if (mode == RewriteMode.FOLD_EXPRESSIONS
                || mode == RewriteMode.UNROLL_FOR_LOOPS
                || mode == RewriteMode.PRECOMPUTE_FOR_LOOPS
                || mode == RewriteMode.CLEANUP) {
            return foldExpression(rewritten);
        }

        return rewritten;
    }

    private Expression rewriteCall(CallExpression call, RewriteMode mode) {
        List<Expression> arguments = new ArrayList<>();
        for (Expression argument : call.arguments()) {
            arguments.add(rewriteExpression(argument, mode));
        }

        return new CallExpression(rewriteExpression(call.callee(), mode), arguments, call.sourceRange());
    }

    private Expression rewriteSelect(SelectExpression select, RewriteMode mode) {
        return new SelectExpression(
                rewriteExpression(select.target(), mode),
                select.memberName(),
                select.sourceRange()
        );
    }

    private Expression rewriteNew(NewExpression newExpression, RewriteMode mode) {
        List<Expression> arguments = new ArrayList<>();
        for (Expression argument : newExpression.arguments()) {
            arguments.add(rewriteExpression(argument, mode));
        }

        return new NewExpression(newExpression.typeName(), arguments, newExpression.sourceRange());
    }

    private Expression rewriteArrayLiteral(ArrayLiteralExpression arrayLiteral, RewriteMode mode) {
        List<Expression> values = new ArrayList<>();
        for (Expression value : arrayLiteral.values()) {
            values.add(rewriteExpression(value, mode));
        }
        return new ArrayLiteralExpression(values, arrayLiteral.sourceRange());
    }

    private Expression rewriteCompoundLiteral(CompoundLiteralExpression compoundLiteral, RewriteMode mode) {
        List<CompoundLiteralExpression.Entry> entries = new ArrayList<>();
        for (CompoundLiteralExpression.Entry entry : compoundLiteral.entries()) {
            entries.add(new CompoundLiteralExpression.Entry(entry.key(), rewriteExpression(entry.value(), mode)));
        }
        return new CompoundLiteralExpression(entries, compoundLiteral.sourceRange());
    }

    private Expression foldExpression(Expression expression) {
        if (expression instanceof GroupExpression group) {
            return foldExpression(group.expression());
        }

        if (expression instanceof UnaryExpression unary) {
            Expression operand = foldExpression(unary.operand());
            if (unary.operator() == UnaryOperator.NEGATE && operand instanceof NumberLiteralExpression number) {
                Integer value = parseIntLiteral(number);
                if (value != null) {
                    return new NumberLiteralExpression(Integer.toString(-value), unary.sourceRange());
                }
            }
            if (unary.operator() == UnaryOperator.NOT && operand instanceof BooleanLiteralExpression bool) {
                return new BooleanLiteralExpression(!bool.value(), unary.sourceRange());
            }
            return new UnaryExpression(unary.operator(), operand, unary.sourceRange());
        }

        if (!(expression instanceof BinaryExpression binary)) {
            return expression;
        }

        Expression left = foldExpression(binary.left());
        Expression right = foldExpression(binary.right());

        Expression folded = foldBinary(left, binary.operator(), right, binary.sourceRange());
        if (folded != null) {
            return folded;
        }

        return new BinaryExpression(left, binary.operator(), right, binary.sourceRange());
    }

    private Expression foldBinary(Expression left, BinaryOperator operator, Expression right, net.villagerzock.compiler.ast.SourceRange sourceRange) {
        if (operator == BinaryOperator.ADD && left instanceof StringLiteralExpression leftString && right instanceof StringLiteralExpression rightString) {
            return new StringLiteralExpression(leftString.rawValue() + rightString.rawValue(), sourceRange);
        }

        Integer leftInt = left instanceof NumberLiteralExpression leftNumber ? parseIntLiteral(leftNumber) : null;
        Integer rightInt = right instanceof NumberLiteralExpression rightNumber ? parseIntLiteral(rightNumber) : null;

        if (leftInt != null && rightInt != null) {
            return switch (operator) {
                case ADD -> new NumberLiteralExpression(Integer.toString(leftInt + rightInt), sourceRange);
                case SUBTRACT -> new NumberLiteralExpression(Integer.toString(leftInt - rightInt), sourceRange);
                case MULTIPLY -> new NumberLiteralExpression(Integer.toString(leftInt * rightInt), sourceRange);
                case DIVIDE -> rightInt == 0 ? null : new NumberLiteralExpression(Integer.toString(leftInt / rightInt), sourceRange);
                case MODULO -> rightInt == 0 ? null : new NumberLiteralExpression(Integer.toString(leftInt % rightInt), sourceRange);
                case GREATER -> new BooleanLiteralExpression(leftInt > rightInt, sourceRange);
                case LESS -> new BooleanLiteralExpression(leftInt < rightInt, sourceRange);
                case GREATER_EQUAL -> new BooleanLiteralExpression(leftInt >= rightInt, sourceRange);
                case LESS_EQUAL -> new BooleanLiteralExpression(leftInt <= rightInt, sourceRange);
                case EQUAL -> new BooleanLiteralExpression(leftInt.equals(rightInt), sourceRange);
                case NOT_EQUAL -> new BooleanLiteralExpression(!leftInt.equals(rightInt), sourceRange);
                default -> null;
            };
        }

        if (left instanceof BooleanLiteralExpression leftBool && right instanceof BooleanLiteralExpression rightBool) {
            return switch (operator) {
                case LOGICAL_AND -> new BooleanLiteralExpression(leftBool.value() && rightBool.value(), sourceRange);
                case LOGICAL_OR -> new BooleanLiteralExpression(leftBool.value() || rightBool.value(), sourceRange);
                case EQUAL -> new BooleanLiteralExpression(leftBool.value() == rightBool.value(), sourceRange);
                case NOT_EQUAL -> new BooleanLiteralExpression(leftBool.value() != rightBool.value(), sourceRange);
                default -> null;
            };
        }

        if (left instanceof StringLiteralExpression leftString && right instanceof StringLiteralExpression rightString) {
            return switch (operator) {
                case EQUAL -> new BooleanLiteralExpression(leftString.rawValue().equals(rightString.rawValue()), sourceRange);
                case NOT_EQUAL -> new BooleanLiteralExpression(!leftString.rawValue().equals(rightString.rawValue()), sourceRange);
                default -> null;
            };
        }

        return null;
    }

    private BlockStatement rewriteBlockWithLoopPrecomputation(BlockStatement block) {
        List<Statement> statements = new ArrayList<>();
        Map<String, Integer> intValues = new HashMap<>();
        Map<String, VariableDeclarationStatement> intDeclarations = new HashMap<>();

        for (Statement statement : block.statements()) {
            Statement rewritten = rewriteStatement(statement, RewriteMode.PRECOMPUTE_FOR_LOOPS);

            if (rewritten instanceof VariableDeclarationStatement variable
                    && variable.initializer() instanceof NumberLiteralExpression number
                    && parseIntLiteral(number) != null) {
                intValues.put(variable.name(), parseIntLiteral(number));
                intDeclarations.put(variable.name(), variable);
                statements.add(variable);
                continue;
            }

            if (rewritten instanceof ForStatement forStatement) {
                List<Statement> precomputed = tryPrecomputeForLoopAway(forStatement, intValues, intDeclarations);
                if (precomputed != null) {
                    statements.addAll(precomputed);
                    continue;
                }
            }

            invalidateAssignedValues(rewritten, intValues, intDeclarations);
            statements.add(rewritten);
        }

        return new BlockStatement(statements, block.sourceRange());
    }

    private List<Statement> tryPrecomputeForLoopAway(
            ForStatement statement,
            Map<String, Integer> intValues,
            Map<String, VariableDeclarationStatement> intDeclarations
    ) {
        LoopPlan loop = loopPlan(statement);
        if (loop == null) {
            return null;
        }

        List<Integer> values = loopValues(
                loop.start(),
                loop.end(),
                loop.condition(),
                loop.update(),
                MAX_FOR_PRECOMPUTE_ITERATIONS
        );
        if (values == null) {
            return null;
        }

        List<ExpressionStatement> operations = simpleIntegerLoopOperations(statement.body());
        if (operations == null || operations.isEmpty()) {
            return null;
        }

        Map<String, Integer> simulated = new HashMap<>(intValues);
        for (int loopValue : values) {
            simulated.put(loop.variableName(), loopValue);

            for (ExpressionStatement operation : operations) {
                if (!applyIntegerOperation(operation.expression(), simulated)) {
                    return null;
                }
            }
        }

        simulated.remove(loop.variableName());

        List<Statement> replacements = new ArrayList<>();
        Set<String> changedNames = new LinkedHashSet<>();
        for (String name : intValues.keySet()) {
            Integer before = intValues.get(name);
            Integer after = simulated.get(name);
            if (after != null && !Objects.equals(before, after)) {
                changedNames.add(name);
            }
        }

        if (changedNames.isEmpty()) {
            return List.of();
        }

        for (String name : changedNames) {
            intValues.put(name, simulated.get(name));
            VariableDeclarationStatement declaration = intDeclarations.get(name);
            if (declaration == null) {
                return null;
            }

            replacements.add(new ExpressionStatement(
                    new AssignmentExpression(
                            new IdentifierExpression(name, declaration.sourceRange()),
                            new NumberLiteralExpression(Integer.toString(simulated.get(name)), declaration.sourceRange()),
                            statement.sourceRange()
                    ),
                    statement.sourceRange()
            ));
        }

        return replacements;
    }

    private LoopPlan loopPlan(ForStatement statement) {
        if (!(statement.initializer() instanceof VariableDeclarationStatement variable)
                || !(variable.initializer() instanceof NumberLiteralExpression startLiteral)
                || !(statement.update() instanceof UpdateExpression update)
                || !(update.target() instanceof IdentifierExpression updateTarget)
                || !updateTarget.name().equals(variable.name())
                || !(statement.condition() instanceof BinaryExpression condition)
                || !(condition.left() instanceof IdentifierExpression conditionTarget)
                || !conditionTarget.name().equals(variable.name())
                || !(condition.right() instanceof NumberLiteralExpression endLiteral)) {
            return null;
        }

        Integer start = parseIntLiteral(startLiteral);
        Integer end = parseIntLiteral(endLiteral);
        if (start == null || end == null) {
            return null;
        }

        return new LoopPlan(variable.name(), start, end, condition.operator(), update.operator());
    }

    private List<ExpressionStatement> simpleIntegerLoopOperations(Statement statement) {
        List<ExpressionStatement> result = new ArrayList<>();
        if (!collectSimpleIntegerLoopOperations(statement, result)) {
            return null;
        }
        return result;
    }

    private boolean collectSimpleIntegerLoopOperations(Statement statement, List<ExpressionStatement> result) {
        if (statement instanceof BlockStatement block) {
            for (Statement child : block.statements()) {
                if (!collectSimpleIntegerLoopOperations(child, result)) {
                    return false;
                }
            }
            return true;
        }

        if (statement instanceof ExpressionStatement expressionStatement
                && (expressionStatement.expression() instanceof AssignmentExpression
                || expressionStatement.expression() instanceof UpdateExpression)) {
            result.add(expressionStatement);
            return true;
        }

        return false;
    }

    private boolean applyIntegerOperation(Expression expression, Map<String, Integer> values) {
        if (expression instanceof UpdateExpression update && update.target() instanceof IdentifierExpression identifier) {
            Integer current = values.get(identifier.name());
            if (current == null) {
                return false;
            }

            values.put(
                    identifier.name(),
                    update.operator() == UpdateOperator.INCREMENT ? current + 1 : current - 1
            );
            return true;
        }

        if (!(expression instanceof AssignmentExpression assignment)
                || !(assignment.target() instanceof IdentifierExpression identifier)) {
            return false;
        }

        Integer value = evaluateIntegerExpression(assignment.value(), values);
        if (value == null) {
            return false;
        }

        values.put(identifier.name(), value);
        return true;
    }

    private Integer evaluateIntegerExpression(Expression expression, Map<String, Integer> values) {
        expression = foldExpression(expression);

        if (expression instanceof NumberLiteralExpression number) {
            return parseIntLiteral(number);
        }

        if (expression instanceof IdentifierExpression identifier) {
            return values.get(identifier.name());
        }

        if (expression instanceof GroupExpression group) {
            return evaluateIntegerExpression(group.expression(), values);
        }

        if (expression instanceof UnaryExpression unary) {
            Integer operand = evaluateIntegerExpression(unary.operand(), values);
            if (operand == null || unary.operator() != UnaryOperator.NEGATE) {
                return null;
            }
            return -operand;
        }

        if (!(expression instanceof BinaryExpression binary)) {
            return null;
        }

        Integer left = evaluateIntegerExpression(binary.left(), values);
        Integer right = evaluateIntegerExpression(binary.right(), values);
        if (left == null || right == null) {
            return null;
        }

        return switch (binary.operator()) {
            case ADD -> left + right;
            case SUBTRACT -> left - right;
            case MULTIPLY -> left * right;
            case DIVIDE -> right == 0 ? null : left / right;
            case MODULO -> right == 0 ? null : left % right;
            default -> null;
        };
    }

    private void invalidateAssignedValues(
            Statement statement,
            Map<String, Integer> intValues,
            Map<String, VariableDeclarationStatement> intDeclarations
    ) {
        Set<String> assignedNames = new HashSet<>();
        collectAssignedNames(statement, assignedNames);

        for (String name : assignedNames) {
            intValues.remove(name);
            intDeclarations.remove(name);
        }
    }

    private void collectAssignedNames(Statement statement, Set<String> names) {
        if (statement == null) {
            return;
        }

        if (statement instanceof ExpressionStatement expressionStatement) {
            collectAssignedNames(expressionStatement.expression(), names);
        } else if (statement instanceof BlockStatement block) {
            for (Statement child : block.statements()) {
                collectAssignedNames(child, names);
            }
        } else if (statement instanceof IfStatement ifStatement) {
            collectAssignedNames(ifStatement.thenBranch(), names);
            collectAssignedNames(ifStatement.elseBranch(), names);
        } else if (statement instanceof WhileStatement whileStatement) {
            collectAssignedNames(whileStatement.body(), names);
        } else if (statement instanceof ForStatement forStatement) {
            collectAssignedNames(forStatement.initializer(), names);
            collectAssignedNames(forStatement.update(), names);
            collectAssignedNames(forStatement.body(), names);
        }
    }

    private void collectAssignedNames(Expression expression, Set<String> names) {
        if (expression instanceof AssignmentExpression assignment && assignment.target() instanceof IdentifierExpression identifier) {
            names.add(identifier.name());
        } else if (expression instanceof UpdateExpression update && update.target() instanceof IdentifierExpression identifier) {
            names.add(identifier.name());
        }
    }

    private Statement tryUnrollForLoop(ForStatement statement) {
        if (!(statement.initializer() instanceof VariableDeclarationStatement variable)
                || !(variable.initializer() instanceof NumberLiteralExpression startLiteral)
                || !(statement.update() instanceof UpdateExpression update)
                || !(update.target() instanceof IdentifierExpression updateTarget)
                || !updateTarget.name().equals(variable.name())
                || !(statement.condition() instanceof BinaryExpression condition)
                || !(condition.left() instanceof IdentifierExpression conditionTarget)
                || !conditionTarget.name().equals(variable.name())
                || !(condition.right() instanceof NumberLiteralExpression endLiteral)) {
            return null;
        }

        Integer start = parseIntLiteral(startLiteral);
        Integer end = parseIntLiteral(endLiteral);
        if (start == null
                || end == null
                || bodyAssignsName(statement.body(), variable.name())
                || bodyDeclaresLocals(statement.body())) {
            return null;
        }

        List<Integer> values = loopValues(start, end, condition.operator(), update.operator(), MAX_FOR_UNROLL_ITERATIONS);
        if (values == null || values.size() > MAX_FOR_UNROLL_ITERATIONS) {
            return null;
        }

        List<Statement> statements = new ArrayList<>();
        for (int value : values) {
            statements.add(cloneWithIntegerSubstitution(statement.body(), variable.name(), value));
        }

        return new BlockStatement(statements, statement.sourceRange());
    }

    private List<Integer> loopValues(
            int start,
            int end,
            BinaryOperator condition,
            UpdateOperator update,
            int maxIterations
    ) {
        List<Integer> values = new ArrayList<>();
        int current = start;

        for (int guard = 0; guard <= maxIterations; guard++) {
            if (!matchesLoopCondition(current, end, condition)) {
                return values;
            }

            values.add(current);
            current += update == UpdateOperator.INCREMENT ? 1 : -1;
        }

        return null;
    }

    private boolean matchesLoopCondition(int current, int end, BinaryOperator condition) {
        return switch (condition) {
            case LESS -> current < end;
            case LESS_EQUAL -> current <= end;
            case GREATER -> current > end;
            case GREATER_EQUAL -> current >= end;
            case NOT_EQUAL -> current != end;
            default -> false;
        };
    }

    private Statement cloneWithIntegerSubstitution(Statement statement, String name, int value) {
        if (statement instanceof BlockStatement block) {
            List<Statement> statements = new ArrayList<>();
            for (Statement child : block.statements()) {
                statements.add(cloneWithIntegerSubstitution(child, name, value));
            }
            return new BlockStatement(statements, block.sourceRange());
        }

        if (statement instanceof VariableDeclarationStatement variable) {
            return new VariableDeclarationStatement(
                    variable.type(),
                    variable.name(),
                    substituteInteger(variable.initializer(), name, value),
                    variable.sourceRange()
            );
        }

        if (statement instanceof ExpressionStatement expressionStatement) {
            return new ExpressionStatement(
                    substituteInteger(expressionStatement.expression(), name, value),
                    expressionStatement.sourceRange()
            );
        }

        if (statement instanceof ReturnStatement returnStatement) {
            return new ReturnStatement(
                    substituteInteger(returnStatement.value(), name, value),
                    returnStatement.sourceRange()
            );
        }

        if (statement instanceof IfStatement ifStatement) {
            return new IfStatement(
                    substituteInteger(ifStatement.condition(), name, value),
                    cloneWithIntegerSubstitution(ifStatement.thenBranch(), name, value),
                    ifStatement.elseBranch() == null ? null : cloneWithIntegerSubstitution(ifStatement.elseBranch(), name, value),
                    ifStatement.sourceRange()
            );
        }

        if (statement instanceof WhileStatement whileStatement) {
            return new WhileStatement(
                    substituteInteger(whileStatement.condition(), name, value),
                    cloneWithIntegerSubstitution(whileStatement.body(), name, value),
                    whileStatement.sourceRange()
            );
        }

        if (statement instanceof ForStatement forStatement) {
            return new ForStatement(
                    forStatement.initializer() == null ? null : cloneWithIntegerSubstitution(forStatement.initializer(), name, value),
                    substituteInteger(forStatement.condition(), name, value),
                    substituteInteger(forStatement.update(), name, value),
                    cloneWithIntegerSubstitution(forStatement.body(), name, value),
                    forStatement.sourceRange()
            );
        }

        return statement;
    }

    private Expression substituteInteger(Expression expression, String name, int value) {
        if (expression == null) {
            return null;
        }

        if (expression instanceof IdentifierExpression identifier && identifier.name().equals(name)) {
            return new NumberLiteralExpression(Integer.toString(value), identifier.sourceRange());
        }

        if (expression instanceof TStringLiteralExpression string) {
            String raw = substituteTemplateInteger(string.rawValue(), name, value);
            return new TStringLiteralExpression(raw, string.sourceRange());
        }

        if (expression instanceof BinaryExpression binary) {
            return foldExpression(new BinaryExpression(
                    substituteInteger(binary.left(), name, value),
                    binary.operator(),
                    substituteInteger(binary.right(), name, value),
                    binary.sourceRange()
            ));
        }

        if (expression instanceof UnaryExpression unary) {
            return foldExpression(new UnaryExpression(
                    unary.operator(),
                    substituteInteger(unary.operand(), name, value),
                    unary.sourceRange()
            ));
        }

        if (expression instanceof GroupExpression group) {
            return foldExpression(new GroupExpression(
                    substituteInteger(group.expression(), name, value),
                    group.sourceRange()
            ));
        }

        if (expression instanceof AssignmentExpression assignment) {
            return new AssignmentExpression(
                    substituteInteger(assignment.target(), name, value),
                    substituteInteger(assignment.value(), name, value),
                    assignment.sourceRange()
            );
        }

        if (expression instanceof UpdateExpression update) {
            return new UpdateExpression(
                    substituteInteger(update.target(), name, value),
                    update.operator(),
                    update.sourceRange()
            );
        }

        if (expression instanceof CallExpression call) {
            List<Expression> arguments = new ArrayList<>();
            for (Expression argument : call.arguments()) {
                arguments.add(substituteInteger(argument, name, value));
            }
            return new CallExpression(substituteInteger(call.callee(), name, value), arguments, call.sourceRange());
        }

        if (expression instanceof SelectExpression select) {
            return new SelectExpression(substituteInteger(select.target(), name, value), select.memberName(), select.sourceRange());
        }

        if (expression instanceof NewExpression newExpression) {
            List<Expression> arguments = new ArrayList<>();
            for (Expression argument : newExpression.arguments()) {
                arguments.add(substituteInteger(argument, name, value));
            }
            return new NewExpression(newExpression.typeName(), arguments, newExpression.sourceRange());
        }

        if (expression instanceof ArrayLiteralExpression arrayLiteral) {
            List<Expression> values = new ArrayList<>();
            for (Expression child : arrayLiteral.values()) {
                values.add(substituteInteger(child, name, value));
            }
            return new ArrayLiteralExpression(values, arrayLiteral.sourceRange());
        }

        if (expression instanceof CompoundLiteralExpression compoundLiteral) {
            List<CompoundLiteralExpression.Entry> entries = new ArrayList<>();
            for (CompoundLiteralExpression.Entry entry : compoundLiteral.entries()) {
                entries.add(new CompoundLiteralExpression.Entry(entry.key(), substituteInteger(entry.value(), name, value)));
            }
            return new CompoundLiteralExpression(entries, compoundLiteral.sourceRange());
        }

        return expression;
    }

    private List<Statement> removeDeadAndUnusedStatements(List<Statement> statements) {
        Set<String> usedNames = new HashSet<>();
        for (Statement statement : statements) {
            collectUsedNames(statement, usedNames);
        }

        List<Statement> result = new ArrayList<>();
        boolean terminated = false;

        for (Statement statement : statements) {
            if (terminated) {
                continue;
            }

            if (statement instanceof VariableDeclarationStatement variable
                    && !usedNames.contains(variable.name())
                    && isPureExpression(variable.initializer())) {
                continue;
            }

            result.add(statement);
            if (statement instanceof ReturnStatement) {
                terminated = true;
            }
        }

        return result;
    }

    private void collectUsedNames(Statement statement, Set<String> names) {
        if (statement == null) {
            return;
        }

        if (statement instanceof VariableDeclarationStatement variable) {
            collectUsedNames(variable.initializer(), names);
        } else if (statement instanceof ExpressionStatement expressionStatement) {
            collectUsedNames(expressionStatement.expression(), names);
        } else if (statement instanceof ReturnStatement returnStatement) {
            collectUsedNames(returnStatement.value(), names);
        } else if (statement instanceof IfStatement ifStatement) {
            collectUsedNames(ifStatement.condition(), names);
            collectUsedNames(ifStatement.thenBranch(), names);
            collectUsedNames(ifStatement.elseBranch(), names);
        } else if (statement instanceof WhileStatement whileStatement) {
            collectUsedNames(whileStatement.condition(), names);
            collectUsedNames(whileStatement.body(), names);
        } else if (statement instanceof ForStatement forStatement) {
            collectUsedNames(forStatement.initializer(), names);
            collectUsedNames(forStatement.condition(), names);
            collectUsedNames(forStatement.update(), names);
            collectUsedNames(forStatement.body(), names);
        } else if (statement instanceof BlockStatement block) {
            for (Statement child : block.statements()) {
                collectUsedNames(child, names);
            }
        }
    }

    private void collectUsedNames(Expression expression, Set<String> names) {
        if (expression == null) {
            return;
        }

        if (expression instanceof IdentifierExpression identifier) {
            names.add(identifier.name());
        } else if (expression instanceof TStringLiteralExpression string) {
            collectTemplateNames(string.rawValue(), names);
        } else if (expression instanceof BinaryExpression binary) {
            collectUsedNames(binary.left(), names);
            collectUsedNames(binary.right(), names);
        } else if (expression instanceof UnaryExpression unary) {
            collectUsedNames(unary.operand(), names);
        } else if (expression instanceof GroupExpression group) {
            collectUsedNames(group.expression(), names);
        } else if (expression instanceof AssignmentExpression assignment) {
            collectUsedNames(assignment.target(), names);
            collectUsedNames(assignment.value(), names);
        } else if (expression instanceof UpdateExpression update) {
            collectUsedNames(update.target(), names);
        } else if (expression instanceof CallExpression call) {
            collectUsedNames(call.callee(), names);
            for (Expression argument : call.arguments()) {
                collectUsedNames(argument, names);
            }
        } else if (expression instanceof SelectExpression select) {
            collectUsedNames(select.target(), names);
        } else if (expression instanceof NewExpression newExpression) {
            for (Expression argument : newExpression.arguments()) {
                collectUsedNames(argument, names);
            }
        } else if (expression instanceof ArrayLiteralExpression arrayLiteral) {
            for (Expression value : arrayLiteral.values()) {
                collectUsedNames(value, names);
            }
        } else if (expression instanceof CompoundLiteralExpression compoundLiteral) {
            for (CompoundLiteralExpression.Entry entry : compoundLiteral.entries()) {
                collectUsedNames(entry.value(), names);
            }
        }
    }

    private boolean bodyAssignsName(Statement statement, String name) {
        if (statement == null) {
            return false;
        }

        if (statement instanceof ExpressionStatement expressionStatement) {
            return expressionAssignsName(expressionStatement.expression(), name);
        }

        if (statement instanceof BlockStatement block) {
            for (Statement child : block.statements()) {
                if (bodyAssignsName(child, name)) {
                    return true;
                }
            }
        }

        if (statement instanceof IfStatement ifStatement) {
            return bodyAssignsName(ifStatement.thenBranch(), name)
                    || bodyAssignsName(ifStatement.elseBranch(), name);
        }

        if (statement instanceof WhileStatement whileStatement) {
            return bodyAssignsName(whileStatement.body(), name);
        }

        if (statement instanceof ForStatement forStatement) {
            return expressionAssignsName(forStatement.update(), name)
                    || bodyAssignsName(forStatement.body(), name);
        }

        return false;
    }

    private boolean bodyDeclaresLocals(Statement statement) {
        if (statement == null) {
            return false;
        }

        if (statement instanceof VariableDeclarationStatement) {
            return true;
        }

        if (statement instanceof BlockStatement block) {
            for (Statement child : block.statements()) {
                if (bodyDeclaresLocals(child)) {
                    return true;
                }
            }
        }

        if (statement instanceof IfStatement ifStatement) {
            return bodyDeclaresLocals(ifStatement.thenBranch())
                    || bodyDeclaresLocals(ifStatement.elseBranch());
        }

        if (statement instanceof WhileStatement whileStatement) {
            return bodyDeclaresLocals(whileStatement.body());
        }

        if (statement instanceof ForStatement forStatement) {
            return bodyDeclaresLocals(forStatement.initializer())
                    || bodyDeclaresLocals(forStatement.body());
        }

        return false;
    }

    private boolean expressionAssignsName(Expression expression, String name) {
        if (expression instanceof AssignmentExpression assignment && assignment.target() instanceof IdentifierExpression identifier) {
            return identifier.name().equals(name);
        }

        if (expression instanceof UpdateExpression update && update.target() instanceof IdentifierExpression identifier) {
            return identifier.name().equals(name);
        }

        return false;
    }

    private boolean bodyHasUnsafeTemplateUse(Statement statement, String name) {
        if (statement == null) {
            return false;
        }

        if (statement instanceof VariableDeclarationStatement variable) {
            return expressionHasUnsafeTemplateUse(variable.initializer(), name);
        }

        if (statement instanceof ExpressionStatement expressionStatement) {
            return expressionHasUnsafeTemplateUse(expressionStatement.expression(), name);
        }

        if (statement instanceof ReturnStatement returnStatement) {
            return expressionHasUnsafeTemplateUse(returnStatement.value(), name);
        }

        if (statement instanceof IfStatement ifStatement) {
            return expressionHasUnsafeTemplateUse(ifStatement.condition(), name)
                    || bodyHasUnsafeTemplateUse(ifStatement.thenBranch(), name)
                    || bodyHasUnsafeTemplateUse(ifStatement.elseBranch(), name);
        }

        if (statement instanceof WhileStatement whileStatement) {
            return expressionHasUnsafeTemplateUse(whileStatement.condition(), name)
                    || bodyHasUnsafeTemplateUse(whileStatement.body(), name);
        }

        if (statement instanceof ForStatement forStatement) {
            return bodyHasUnsafeTemplateUse(forStatement.initializer(), name)
                    || expressionHasUnsafeTemplateUse(forStatement.condition(), name)
                    || expressionHasUnsafeTemplateUse(forStatement.update(), name)
                    || bodyHasUnsafeTemplateUse(forStatement.body(), name);
        }

        if (statement instanceof BlockStatement block) {
            for (Statement child : block.statements()) {
                if (bodyHasUnsafeTemplateUse(child, name)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean expressionHasUnsafeTemplateUse(Expression expression, String name) {
        if (expression == null) {
            return false;
        }

        if (expression instanceof TStringLiteralExpression string) {
            return templateHasUnsafeUse(string.rawValue(), name);
        }

        if (expression instanceof BinaryExpression binary) {
            return expressionHasUnsafeTemplateUse(binary.left(), name)
                    || expressionHasUnsafeTemplateUse(binary.right(), name);
        }

        if (expression instanceof UnaryExpression unary) {
            return expressionHasUnsafeTemplateUse(unary.operand(), name);
        }

        if (expression instanceof GroupExpression group) {
            return expressionHasUnsafeTemplateUse(group.expression(), name);
        }

        if (expression instanceof AssignmentExpression assignment) {
            return expressionHasUnsafeTemplateUse(assignment.target(), name)
                    || expressionHasUnsafeTemplateUse(assignment.value(), name);
        }

        if (expression instanceof UpdateExpression update) {
            return expressionHasUnsafeTemplateUse(update.target(), name);
        }

        if (expression instanceof CallExpression call) {
            if (expressionHasUnsafeTemplateUse(call.callee(), name)) {
                return true;
            }
            for (Expression argument : call.arguments()) {
                if (expressionHasUnsafeTemplateUse(argument, name)) {
                    return true;
                }
            }
        }

        if (expression instanceof SelectExpression select) {
            return expressionHasUnsafeTemplateUse(select.target(), name);
        }

        if (expression instanceof NewExpression newExpression) {
            for (Expression argument : newExpression.arguments()) {
                if (expressionHasUnsafeTemplateUse(argument, name)) {
                    return true;
                }
            }
        }

        if (expression instanceof ArrayLiteralExpression arrayLiteral) {
            for (Expression value : arrayLiteral.values()) {
                if (expressionHasUnsafeTemplateUse(value, name)) {
                    return true;
                }
            }
        }

        if (expression instanceof CompoundLiteralExpression compoundLiteral) {
            for (CompoundLiteralExpression.Entry entry : compoundLiteral.entries()) {
                if (expressionHasUnsafeTemplateUse(entry.value(), name)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean templateHasUnsafeUse(String raw, String name) {
        int index = 0;
        while (index < raw.length()) {
            int start = raw.indexOf("${", index);
            if (start == -1) {
                return false;
            }

            int end = raw.indexOf("}", start + 2);
            if (end == -1) {
                return false;
            }

            String source = raw.substring(start + 2, end).trim();
            Set<String> names = new HashSet<>();
            collectIdentifierLikeNames(source, names);
            if (names.contains(name) && !source.equals(name)) {
                return true;
            }

            index = end + 1;
        }

        return false;
    }

    private void collectTemplateNames(String raw, Set<String> names) {
        int index = 0;
        while (index < raw.length()) {
            int start = raw.indexOf("${", index);
            if (start == -1) {
                return;
            }

            int end = raw.indexOf("}", start + 2);
            if (end == -1) {
                return;
            }

            String source = raw.substring(start + 2, end);
            collectIdentifierLikeNames(source, names);
            index = end + 1;
        }
    }

    private void collectIdentifierLikeNames(String source, Set<String> names) {
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_') {
                current.append(c);
                continue;
            }

            addIdentifierLikeName(current, names);
        }

        addIdentifierLikeName(current, names);
    }

    private void addIdentifierLikeName(StringBuilder current, Set<String> names) {
        if (current.isEmpty()) {
            return;
        }

        String name = current.toString();
        if (!Character.isDigit(name.charAt(0))) {
            names.add(name);
        }
        current.setLength(0);
    }

    private String substituteTemplateInteger(String raw, String name, int value) {
        StringBuilder result = new StringBuilder();
        int index = 0;

        while (index < raw.length()) {
            int start = raw.indexOf("${", index);
            if (start == -1) {
                result.append(raw.substring(index));
                break;
            }

            int end = raw.indexOf("}", start + 2);
            if (end == -1) {
                result.append(raw.substring(index));
                break;
            }

            result.append(raw, index, start);
            String source = raw.substring(start + 2, end).trim();
            result.append("${").append(substituteIdentifierToken(source, name, Integer.toString(value))).append("}");

            index = end + 1;
        }

        return result.toString();
    }

    private String substituteIdentifierToken(String source, String name, String value) {
        StringBuilder result = new StringBuilder();
        int index = 0;

        while (index < source.length()) {
            char c = source.charAt(index);
            if (!Character.isLetter(c) && c != '_') {
                result.append(c);
                index++;
                continue;
            }

            int start = index;
            index++;
            while (index < source.length()) {
                char next = source.charAt(index);
                if (!Character.isLetterOrDigit(next) && next != '_') {
                    break;
                }
                index++;
            }

            String token = source.substring(start, index);
            result.append(token.equals(name) ? value : token);
        }

        return result.toString();
    }

    private boolean isPureExpression(Expression expression) {
        if (expression == null) {
            return true;
        }

        if (expression instanceof NumberLiteralExpression
                || expression instanceof StringLiteralExpression
                || expression instanceof BooleanLiteralExpression
                || expression instanceof NullLiteralExpression
                || expression instanceof IdentifierExpression) {
            return true;
        }

        if (expression instanceof GroupExpression group) {
            return isPureExpression(group.expression());
        }

        if (expression instanceof UnaryExpression unary) {
            return isPureExpression(unary.operand());
        }

        if (expression instanceof BinaryExpression binary) {
            return isPureExpression(binary.left()) && isPureExpression(binary.right());
        }

        if (expression instanceof ArrayLiteralExpression arrayLiteral) {
            for (Expression value : arrayLiteral.values()) {
                if (!isPureExpression(value)) {
                    return false;
                }
            }
            return true;
        }

        if (expression instanceof CompoundLiteralExpression compoundLiteral) {
            for (CompoundLiteralExpression.Entry entry : compoundLiteral.entries()) {
                if (!isPureExpression(entry.value())) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    private boolean shouldFlatten(BlockStatement block, RewriteMode mode) {
        return mode == RewriteMode.UNROLL_FOR_LOOPS || mode == RewriteMode.CLEANUP;
    }

    private BlockStatement emptyBlock(net.villagerzock.compiler.ast.SourceRange sourceRange) {
        return new BlockStatement(List.of(), sourceRange);
    }

    private Integer parseIntLiteral(NumberLiteralExpression number) {
        String raw = number.rawValue();
        if (raw.contains(".") || raw.endsWith("f") || raw.endsWith("F")) {
            return null;
        }

        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private enum RewriteMode {
        FOLD_EXPRESSIONS,
        PRECOMPUTE_FOR_LOOPS,
        UNROLL_FOR_LOOPS,
        CLEANUP
    }

    private record LoopPlan(
            String variableName,
            int start,
            int end,
            BinaryOperator condition,
            UpdateOperator update
    ) {
    }
}
