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

        List<ClassDeclaration> classes = new ArrayList<>();
        for (MCSParser.ClassDeclContext classDecl : ctx.classDecl()) {
            classes.add((ClassDeclaration) visit(classDecl));
        }

        return new ProgramNode(packagePath, imports, classes);
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
    public Node visitQualifiedPath(MCSParser.QualifiedPathContext ctx) {
        String namespace = ctx.IDENTIFIER().getText();

        List<String> segments = new ArrayList<>();
        for (MCSParser.PathSegmentContext segment : ctx.pathSegment()) {
            segments.add(segment.IDENTIFIER().getText());
        }

        return new QualifiedPathNode(namespace, segments);
    }

    @Override
    public Node visitClassDecl(MCSParser.ClassDeclContext ctx) {
        String name = ctx.IDENTIFIER().getText();

        List<Declaration> members = new ArrayList<>();
        for (MCSParser.MemberDeclContext member : ctx.classBody().memberDecl()) {
            members.add((Declaration) visit(member));
        }

        return new ClassDeclaration(name, members);
    }

    @Override
    public Node visitMemberDecl(MCSParser.MemberDeclContext ctx) {
        if (ctx.fieldDecl() != null) {
            return visit(ctx.fieldDecl());
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
                visitMethodModifiers(ctx.methodModifier()),
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

        System.out.println("DEBUG NATIVE BODY = ["
                + nativeBody
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                + "]");

        return new MethodDeclaration(
                Set.of(MethodModifier.NATIVE),
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

    private Set<MethodModifier> visitMethodModifiers(List<MCSParser.MethodModifierContext> contexts) {
        EnumSet<MethodModifier> modifiers = EnumSet.noneOf(MethodModifier.class);

        for (MCSParser.MethodModifierContext context : contexts) {
            if (context.REPLACE() != null) {
                modifiers.add(MethodModifier.REPLACE);
            }
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
        return new TypeNode(ctx.IDENTIFIER().getText());
    }

    @Override
    public Node visitParameter(MCSParser.ParameterContext ctx) {
        TypeNode type = (TypeNode) visit(ctx.typeName());
        String name = ctx.IDENTIFIER().getText();

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

        return visit(ctx.expressionStatement());
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
        TypeNode type = (TypeNode) visit(ctx.typeName());
        String name = ctx.IDENTIFIER().getText();

        Expression initializer = ctx.expression() != null
                ? (Expression) visit(ctx.expression())
                : null;

        return new VariableDeclarationStatement(type, name, initializer);
    }

    @Override
    public Node visitExpressionStatement(MCSParser.ExpressionStatementContext ctx) {
        return new ExpressionStatement((Expression) visit(ctx.expression()));
    }

    @Override
    public Node visitExpression(MCSParser.ExpressionContext ctx) {
        return visit(ctx.assignmentExpression());
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

        for (MCSParser.PostfixSuffixContext suffix : ctx.postfixSuffix()) {
            if (suffix.DOT() != null && suffix.LPAREN() == null) {
                current = new MemberAccessExpression(current, suffix.IDENTIFIER().getText());
            } else if (suffix.DOT() != null) {
                current = new CallExpression(
                        new MemberAccessExpression(current, suffix.IDENTIFIER().getText()),
                        visitArguments(suffix.argumentList())
                );
            } else {
                current = new CallExpression(
                        current,
                        visitArguments(suffix.argumentList())
                );
            }
        }

        return current;
    }

    @Override
    public Node visitPrimaryExpression(MCSParser.PrimaryExpressionContext ctx) {
        if (ctx.NUMBER() != null) {
            return new NumberLiteralExpression(ctx.NUMBER().getText());
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

        if (ctx.IDENTIFIER() != null) {
            return new IdentifierExpression(ctx.IDENTIFIER().getText());
        }

        return new GroupExpression((Expression) visit(ctx.expression()));
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