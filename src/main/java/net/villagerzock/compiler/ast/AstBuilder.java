package net.villagerzock.compiler.ast;

import net.villagerzock.compiler.ast.decl.*;
import net.villagerzock.compiler.ast.expr.*;
import net.villagerzock.compiler.ast.stmt.*;
import net.villagerzock.compiler.ast.type.TypeNode;
import net.villagerzock.compiler.parser.MCSParser;
import net.villagerzock.compiler.parser.MCSParserBaseVisitor;
import org.antlr.v4.runtime.misc.Interval;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class AstBuilder extends MCSParserBaseVisitor<Node> {

    @Override
    public Node visitProgram(MCSParser.ProgramContext ctx) {
        QualifiedPathNode packagePath = ctx.packageDecl() != null
                ? (QualifiedPathNode) visit(ctx.packageDecl())
                : null;

        List<ImportDeclaration> imports = new ArrayList<>();
        for (MCSParser.ImportDeclContext importDecl : ctx.importDecl()) {
            imports.add((ImportDeclaration) visit(importDecl));
        }
        List<StaticImportDeclaration> staticImports = new ArrayList<>();
        for (MCSParser.StaticImportDeclContext importDecl : ctx.staticImportDecl()) {
            staticImports.add((StaticImportDeclaration) visit(importDecl));
        }

        List<ClassDeclaration> classes = new ArrayList<>();
        for (MCSParser.ClassDeclContext classDecl : ctx.classDecl()) {
            classes.add((ClassDeclaration) visit(classDecl));
        }

        return new ProgramNode(packagePath, imports, classes,staticImports);
    }

    @Override
    public Node visitPackageDecl(MCSParser.PackageDeclContext ctx) {
        return visit(ctx.qualifiedPath());
    }

    @Override
    public Node visitImportDecl(MCSParser.ImportDeclContext ctx) {
        return new ImportDeclaration((QualifiedPathNode) visit(ctx.qualifiedPath()));
    }

    @Override
    public Node visitStaticImportDecl(MCSParser.StaticImportDeclContext ctx) {
        return new StaticImportDeclaration((QualifiedPathNode) visit(ctx.qualifiedPath()));
    }

    @Override
    public Node visitQualifiedPath(MCSParser.QualifiedPathContext ctx) {
        String namespace = ctx.IDENTIFIER().getText();

        List<String> segments = new ArrayList<>();
        collectPathTail(ctx.pathTail(), segments);

        return new QualifiedPathNode(namespace, segments);
    }

    private void collectPathTail(MCSParser.PathTailContext ctx, List<String> segments) {
        for (var identifier : ctx.IDENTIFIER()) {
            segments.add(identifier.getText());
        }
    }

    @Override
    public Node visitClassDecl(MCSParser.ClassDeclContext ctx) {
        boolean isRecord = ctx.CLASS() == null;
        String name = ctx.IDENTIFIER().getText();

        List<Declaration> members = new ArrayList<>();
        if (isRecord){
            List<ParameterDeclaration> components = visitParameters(ctx.parameterList());
            for (ParameterDeclaration parameterDeclaration : components){
                FieldDeclaration fieldDeclaration = new FieldDeclaration(parameterDeclaration.type(),parameterDeclaration.name(),null);
                members.add(fieldDeclaration);
            }
            for (MCSParser.RecordMemberDeclContext member : ctx.recordBody().recordMemberDecl()){
                members.add((Declaration) visit(member));
            }
            return new RecordDeclaration(name, components, members);
        }
        for (MCSParser.MemberDeclContext member : ctx.classBody().memberDecl()) {
            members.add((Declaration) visit(member));
        }

        return new ClassDeclaration(name, members);
    }

    @Override
    public Node visitRecordMemberDecl(MCSParser.RecordMemberDeclContext ctx) {
        return visit(ctx.methodDecl());
    }

    @Override
    public Node visitMemberDecl(MCSParser.MemberDeclContext ctx) {
        if (ctx.fieldDecl() != null) {
            return visit(ctx.fieldDecl());
        }
        if (ctx.classDecl() != null){
            return visit(ctx.classDecl());
        }
        if (ctx.constructorDecl() != null){
            return visit(ctx.constructorDecl());
        }

        return visit(ctx.methodDecl());
    }

    @Override
    public Node visitFieldDecl(MCSParser.FieldDeclContext ctx) {
        TypeNode type = (TypeNode) visit(ctx.typeName());
        String name = ctx.IDENTIFIER().getText();

        Expression initializer = ctx.expression() != null
                ? (Expression) visit(ctx.expression())
                : null;

        return new FieldDeclaration(type, name, initializer);
    }

    @Override
    public Node visitConstructorDecl(MCSParser.ConstructorDeclContext ctx) {
        return new ConstructorDeclaration(visitParameters(ctx.parameterList()), (BlockStatement) visit(ctx.block()));
    }

    @Override
    public Node visitMethodDecl(MCSParser.MethodDeclContext ctx) {
        if (ctx.nativeMethodDecl() != null) {
            return visit(ctx.nativeMethodDecl());
        }

        return visit(ctx.normalMethodDecl());
    }

    @Override
    public Node visitNormalMethodDecl(MCSParser.NormalMethodDeclContext ctx) {
        TypeNode returnType = (TypeNode) visit(ctx.returnType());
        String name = ctx.IDENTIFIER().getText();
        List<ParameterDeclaration> parameters = visitParameters(ctx.parameterList());
        BlockStatement body = (BlockStatement) visit(ctx.block());

        return new MethodDeclaration(
                visitMethodModifiers(ctx.methodModifier(), false),
                returnType,
                name,
                parameters,
                body
        );
    }

    @Override
    public Node visitNativeMethodDecl(MCSParser.NativeMethodDeclContext ctx) {
        TypeNode returnType = (TypeNode) visit(ctx.returnType());
        String name = ctx.IDENTIFIER().getText();
        List<ParameterDeclaration> parameters = visitParameters(ctx.parameterList());
        String nativeBody = getNativeBody(ctx.nativeBlock());
        Set<MethodModifier> modifiers = visitMethodModifiers(ctx.methodModifier(), true);

        System.out.println("DEBUG NATIVE BODY = ["
                + nativeBody
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                + "]");

        return new MethodDeclaration(
                modifiers,
                returnType,
                name,
                parameters,
                null,
                nativeBody
        );
    }

    private String getNativeBody(MCSParser.NativeBlockContext ctx) {
        int start = ctx.NATIVE_BLOCK_START().getSymbol().getStopIndex() + 1;
        int stop = ctx.NATIVE_BLOCK_END().getSymbol().getStartIndex() - 1;

        if (stop < start) {
            return "";
        }

        return ctx.start.getInputStream().getText(Interval.of(start, stop));
    }

    private List<ParameterDeclaration> visitParameters(MCSParser.ParameterListContext ctx) {
        List<ParameterDeclaration> parameters = new ArrayList<>();

        if (ctx == null) {
            return parameters;
        }

        for (MCSParser.ParameterContext parameter : ctx.parameter()) {
            parameters.add((ParameterDeclaration) visit(parameter));
        }

        return parameters;
    }



    private Set<MethodModifier> visitMethodModifiers(List<MCSParser.MethodModifierContext> contexts, boolean isNative) {
        EnumSet<MethodModifier> modifiers = EnumSet.noneOf(MethodModifier.class);

        for (MCSParser.MethodModifierContext context : contexts) {
            if (context.STATIC() != null) {
                modifiers.add(MethodModifier.STATIC);

            }
        }
        if (isNative){
            modifiers.add(MethodModifier.NATIVE);
        }
        return modifiers;
    }

    private String stripOuterBraces(String text) {
        if (text == null || text.length() < 2) {
            return "";
        }

        return text.substring(1, text.length() - 1);
    }

    @Override
    public Node visitReturnType(MCSParser.ReturnTypeContext ctx) {
        if (ctx.FUNCTION() != null) {
            return new TypeNode("function");
        }

        return visit(ctx.typeName());
    }

    @Override
    public Node visitTypeName(MCSParser.TypeNameContext ctx) {
        if (ctx.typeName() != null) {
            return new TypeNode(ctx.IDENTIFIER().getText() + "[" + ((TypeNode) visit(ctx.typeName())).name() + "]");
        }

        StringBuilder name = new StringBuilder(ctx.IDENTIFIER().getText());
        for (int i = 0; i < ctx.LBRACKET().size(); i++) {
            name.append("[]");
        }

        return new TypeNode(name.toString());
    }

    @Override
    public Node visitParameter(MCSParser.ParameterContext ctx) {
        TypeNode type = (TypeNode) visit(ctx.typeName());
        String name = ctx.identifierName().getText();

        return new ParameterDeclaration(type, name);
    }

    @Override
    public Node visitBlock(MCSParser.BlockContext ctx) {
        List<Statement> statements = new ArrayList<>();

        for (MCSParser.StatementContext statement : ctx.statement()) {
            statements.add((Statement) visit(statement));
        }

        return new BlockStatement(statements);
    }

    @Override
    public Node visitStatement(MCSParser.StatementContext ctx) {
        if (ctx.block() != null) {
            return visit(ctx.block());
        }
        if (ctx.ifStatement() != null) {
            return visit(ctx.ifStatement());
        }
        if (ctx.returnStatement() != null) {
            return visit(ctx.returnStatement());
        }
        if (ctx.variableDeclStatement() != null) {
            return visit(ctx.variableDeclStatement());
        }
        if (ctx.forStatement() != null) {
            return visit(ctx.forStatement());
        }
        if (ctx.whileStatement() != null) {
            return visit(ctx.whileStatement());
        }
        if (ctx.withStatement() != null) {
            return visit(ctx.withStatement());
        }

        return visit(ctx.expressionStatement());
    }

    @Override
    public Node visitWithStatement(MCSParser.WithStatementContext ctx) {
        List<WithStatement.Part> parts = new ArrayList<>();
        for (MCSParser.WithPartContext part : ctx.withPart()) {
            parts.add(visitWithPartValue(part));
        }
        return new WithStatement(parts, (Statement) visit(ctx.statement()));
    }

    private WithStatement.Part visitWithPartValue(MCSParser.WithPartContext ctx) {
        return new WithStatement.Part(ctx.IDENTIFIER().getText(), visitWithValueValue(ctx.withValue()));
    }

    private WithStatement.Value visitWithValueValue(MCSParser.WithValueContext ctx) {
        if (ctx.selector() != null && ctx.IDENTIFIER() == null) {
            return new WithStatement.SelectorValue(ctx.selector().getText());
        }
        if (ctx.IDENTIFIER() != null) {
            if (ctx.selector() != null) {
                return new WithStatement.CallValue(
                        ctx.IDENTIFIER().getText(),
                        List.of(new SelectorExpression(ctx.selector().getText()))
                );
            }
            return new WithStatement.CallValue(ctx.IDENTIFIER().getText(), visitArguments(ctx.argumentList()));
        }

        return new WithStatement.CoordinateValue(List.of(
                (Expression) visit(ctx.expression(0)),
                (Expression) visit(ctx.expression(1)),
                (Expression) visit(ctx.expression(2))
        ));
    }

    @Override
    public Node visitIfStatement(MCSParser.IfStatementContext ctx) {
        Expression condition = (Expression) visit(ctx.expression());
        Statement thenBranch = (Statement) visit(ctx.statement(0));

        Statement elseBranch = ctx.statement().size() > 1
                ? (Statement) visit(ctx.statement(1))
                : null;

        return new IfStatement(condition, thenBranch, elseBranch);
    }

    @Override
    public Node visitReturnStatement(MCSParser.ReturnStatementContext ctx) {
        Expression value = ctx.expression() != null
                ? (Expression) visit(ctx.expression())
                : null;

        return new ReturnStatement(value);
    }

    @Override
    public Node visitVariableDeclStatement(MCSParser.VariableDeclStatementContext ctx) {
        return visit(ctx.variableDecl());
    }

    @Override
    public Node visitVariableDecl(MCSParser.VariableDeclContext ctx) {
        TypeNode type = (TypeNode) visit(ctx.typeName());
        String name = ctx.IDENTIFIER().getText();

        Expression initializer = ctx.expression() != null
                ? (Expression) visit(ctx.expression())
                : null;

        return new VariableDeclarationStatement(type, name, initializer);
    }

    @Override
    public Node visitWhileStatement(MCSParser.WhileStatementContext ctx) {
        Expression condition = (Expression) visit(ctx.expression());
        Statement body = (Statement) visit(ctx.statement());

        return new WhileStatement(condition, body);
    }

    @Override
    public Node visitForStatement(MCSParser.ForStatementContext ctx) {
        Statement initializer = ctx.forInit() != null
                ? (Statement) visit(ctx.forInit())
                : null;

        Expression condition = ctx.expression() != null
                ? (Expression) visit(ctx.expression())
                : null;

        Expression update = ctx.forUpdate() != null
                ? (Expression) visit(ctx.forUpdate())
                : null;

        Statement body = (Statement) visit(ctx.statement());

        return new ForStatement(initializer, condition, update, body);
    }

    @Override
    public Node visitForInit(MCSParser.ForInitContext ctx) {
        if (ctx.variableDecl() != null) {
            return visit(ctx.variableDecl());
        }

        return new ExpressionStatement((Expression) visit(ctx.expression()));
    }

    @Override
    public Node visitForUpdate(MCSParser.ForUpdateContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Node visitExpressionStatement(MCSParser.ExpressionStatementContext ctx) {
        return new ExpressionStatement((Expression) visit(ctx.expression()));
    }

    @Override
    public Expression visitInlineExpression(MCSParser.InlineExpressionContext ctx) {
        return visitExpression(ctx.expression());
    }

    @Override
    public Expression visitExpression(MCSParser.ExpressionContext ctx) {
        return (Expression) visit(ctx.assignmentExpression());
    }

    @Override
    public Node visitAssignmentExpression(MCSParser.AssignmentExpressionContext ctx) {
        if (ctx.postfixExpression() != null) {
            Expression target = (Expression) visit(ctx.postfixExpression());
            Expression value = (Expression) visit(ctx.assignmentExpression());

            return new AssignmentExpression(target, value);
        }

        return visit(ctx.logicalOrExpression());
    }

    @Override
    public Node visitLogicalOrExpression(MCSParser.LogicalOrExpressionContext ctx) {
        Expression current = (Expression) visit(ctx.logicalAndExpression(0));

        for (int i = 1; i < ctx.logicalAndExpression().size(); i++) {
            current = new BinaryExpression(
                    current,
                    BinaryOperator.LOGICAL_OR,
                    (Expression) visit(ctx.logicalAndExpression(i))
            );
        }

        return current;
    }

    @Override
    public Node visitLogicalAndExpression(MCSParser.LogicalAndExpressionContext ctx) {
        Expression current = (Expression) visit(ctx.equalityExpression(0));

        for (int i = 1; i < ctx.equalityExpression().size(); i++) {
            current = new BinaryExpression(
                    current,
                    BinaryOperator.LOGICAL_AND,
                    (Expression) visit(ctx.equalityExpression(i))
            );
        }

        return current;
    }

    @Override
    public Node visitEqualityExpression(MCSParser.EqualityExpressionContext ctx) {
        Expression current = (Expression) visit(ctx.relationalExpression(0));

        for (int i = 1; i < ctx.relationalExpression().size(); i++) {
            String op = ctx.getChild(i * 2 - 1).getText();

            current = new BinaryExpression(
                    current,
                    mapEqualityOperator(op),
                    (Expression) visit(ctx.relationalExpression(i))
            );
        }

        return current;
    }

    @Override
    public Node visitRelationalExpression(MCSParser.RelationalExpressionContext ctx) {
        Expression current = (Expression) visit(ctx.additiveExpression(0));

        for (int i = 1; i < ctx.additiveExpression().size(); i++) {
            String op = ctx.getChild(i * 2 - 1).getText();

            current = new BinaryExpression(
                    current,
                    mapRelationalOperator(op),
                    (Expression) visit(ctx.additiveExpression(i))
            );
        }

        return current;
    }

    @Override
    public Node visitAdditiveExpression(MCSParser.AdditiveExpressionContext ctx) {
        Expression current = (Expression) visit(ctx.multiplicativeExpression(0));

        for (int i = 1; i < ctx.multiplicativeExpression().size(); i++) {
            String op = ctx.getChild(i * 2 - 1).getText();

            current = new BinaryExpression(
                    current,
                    mapAdditiveOperator(op),
                    (Expression) visit(ctx.multiplicativeExpression(i))
            );
        }

        return current;
    }

    @Override
    public Node visitMultiplicativeExpression(MCSParser.MultiplicativeExpressionContext ctx) {
        Expression current = (Expression) visit(ctx.unaryExpression(0));

        for (int i = 1; i < ctx.unaryExpression().size(); i++) {
            String op = ctx.getChild(i * 2 - 1).getText();

            current = new BinaryExpression(
                    current,
                    mapMultiplicativeOperator(op),
                    (Expression) visit(ctx.unaryExpression(i))
            );
        }

        return current;
    }

    @Override
    public Node visitUnaryExpression(MCSParser.UnaryExpressionContext ctx) {
        if (ctx.postfixExpression() != null) {
            return visit(ctx.postfixExpression());
        }

        String op = ctx.getChild(0).getText();
        Expression value = (Expression) visit(ctx.unaryExpression());

        return new UnaryExpression(mapUnaryOperator(op), value);
    }

    @Override
    public Node visitPostfixExpression(MCSParser.PostfixExpressionContext ctx) {
        Expression current = (Expression) visit(ctx.primaryExpression());

        for (int i = 1; i < ctx.getChildCount(); i++) {
            var child = ctx.getChild(i);

            if (child instanceof MCSParser.PostfixSuffixContext suffix) {
                if (suffix.DOT() != null && suffix.LPAREN() == null) {
                    current = new SelectExpression(current, suffix.IDENTIFIER().getText());
                } else if (suffix.DOT() != null) {
                    current = new CallExpression(
                            new SelectExpression(current, suffix.IDENTIFIER().getText()),
                            visitArguments(suffix.argumentList())
                    );
                } else {
                    current = new CallExpression(
                            current,
                            visitArguments(suffix.argumentList())
                    );
                }
            }

            if (child instanceof MCSParser.PostfixOpContext op) {
                if (op.PLUS_PLUS() != null) {
                    current = new UpdateExpression(current, UpdateOperator.INCREMENT);
                } else if (op.MINUS_MINUS() != null) {
                    current = new UpdateExpression(current, UpdateOperator.DECREMENT);
                }
            }
        }

        return current;
    }

    @Override
    public Node visitPrimaryExpression(MCSParser.PrimaryExpressionContext ctx) {
        if (ctx.NUMBER() != null) {
            return new NumberLiteralExpression(ctx.NUMBER().getText());
        }
        if (ctx.DOLLAR() != null && ctx.STRING() != null){
            String raw = ctx.STRING().getText();
            raw = raw.substring(1, raw.length() - 1);

            return new TStringLiteralExpression(raw);
        }
        if (ctx.STRING() != null) {
            String raw = ctx.STRING().getText();
            raw = raw.substring(1, raw.length() - 1);

            return new StringLiteralExpression(raw);
        }

        if (ctx.TRUE() != null) {
            return new BooleanLiteralExpression(true);
        }

        if (ctx.FALSE() != null) {
            return new BooleanLiteralExpression(false);
        }

        if (ctx.selector() != null) {
            return new SelectorExpression(ctx.selector().getText());
        }

        if (ctx.newExpression() != null){
            return visit(ctx.newExpression());
        }

        if (ctx.arrayLiteralExpression() != null) {
            return visit(ctx.arrayLiteralExpression());
        }

        if (ctx.compoundLiteralExpression() != null) {
            return visit(ctx.compoundLiteralExpression());
        }

        if (ctx.identifierName() != null) {
            return new IdentifierExpression(ctx.identifierName().getText());
        }

        return new GroupExpression((Expression) visit(ctx.expression()));
    }

    @Override
    public Node visitNewExpression(MCSParser.NewExpressionContext ctx) {
        return new NewExpression(ctx.IDENTIFIER().getText(),visitArguments(ctx.argumentList()));
    }

    @Override
    public Node visitArrayLiteralExpression(MCSParser.ArrayLiteralExpressionContext ctx) {
        List<Expression> values = new ArrayList<>();

        for (MCSParser.ExpressionContext expression : ctx.expression()) {
            values.add((Expression) visit(expression));
        }

        return new ArrayLiteralExpression(values);
    }

    @Override
    public Node visitCompoundLiteralExpression(MCSParser.CompoundLiteralExpressionContext ctx) {
        List<CompoundLiteralExpression.Entry> entries = new ArrayList<>();

        for (MCSParser.CompoundEntryContext entry : ctx.compoundEntry()) {
            String key;
            if (entry.compoundKey().STRING() != null) {
                key = entry.compoundKey().STRING().getText();
                key = key.substring(1, key.length() - 1);
            } else {
                key = entry.compoundKey().IDENTIFIER().getText();
            }

            entries.add(new CompoundLiteralExpression.Entry(key, (Expression) visit(entry.expression())));
        }

        return new CompoundLiteralExpression(entries);
    }

    private List<Expression> visitArguments(MCSParser.ArgumentListContext ctx) {
        List<Expression> arguments = new ArrayList<>();

        if (ctx == null) {
            return arguments;
        }

        for (MCSParser.ExpressionContext expression : ctx.expression()) {
            arguments.add((Expression) visit(expression));
        }

        return arguments;
    }

    private BinaryOperator mapEqualityOperator(String op) {
        return switch (op) {
            case "==" -> BinaryOperator.EQUAL;
            case "!=" -> BinaryOperator.NOT_EQUAL;
            default -> throw new IllegalStateException("Unknown equality operator: " + op);
        };
    }

    private BinaryOperator mapRelationalOperator(String op) {
        return switch (op) {
            case ">" -> BinaryOperator.GREATER;
            case "<" -> BinaryOperator.LESS;
            case ">=" -> BinaryOperator.GREATER_EQUAL;
            case "<=" -> BinaryOperator.LESS_EQUAL;
            default -> throw new IllegalStateException("Unknown relational operator: " + op);
        };
    }

    private BinaryOperator mapAdditiveOperator(String op) {
        return switch (op) {
            case "+" -> BinaryOperator.ADD;
            case "-" -> BinaryOperator.SUBTRACT;
            default -> throw new IllegalStateException("Unknown additive operator: " + op);
        };
    }

    private BinaryOperator mapMultiplicativeOperator(String op) {
        return switch (op) {
            case "*" -> BinaryOperator.MULTIPLY;
            case "/" -> BinaryOperator.DIVIDE;
            case "%" -> BinaryOperator.MODULO;
            default -> throw new IllegalStateException("Unknown multiplicative operator: " + op);
        };
    }

    private UnaryOperator mapUnaryOperator(String op) {
        return switch (op) {
            case "!" -> UnaryOperator.NOT;
            case "-" -> UnaryOperator.NEGATE;
            default -> throw new IllegalStateException("Unknown unary operator: " + op);
        };
    }
}
