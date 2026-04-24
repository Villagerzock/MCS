package net.villagerzock.compiler.gen;

import net.villagerzock.compiler.ast.decl.ClassDeclaration;
import net.villagerzock.compiler.ast.decl.Declaration;
import net.villagerzock.compiler.ast.decl.MethodDeclaration;
import net.villagerzock.compiler.ast.decl.ProgramNode;
import net.villagerzock.compiler.ast.expr.CallExpression;
import net.villagerzock.compiler.ast.expr.Expression;
import net.villagerzock.compiler.ast.expr.StringLiteralExpression;
import net.villagerzock.compiler.ast.stmt.BlockStatement;
import net.villagerzock.compiler.ast.stmt.ExpressionStatement;
import net.villagerzock.compiler.ast.stmt.IfStatement;
import net.villagerzock.compiler.ast.stmt.Statement;
import net.villagerzock.compiler.semantic.MethodSymbol;
import net.villagerzock.mcfunction.MCFunction;
import net.villagerzock.mcfunction.MCFunctionUnit;
import net.villagerzock.mcfunction.commandParts.CreateStackFrame;
import net.villagerzock.mcfunction.commandParts.FunctionCall;
import net.villagerzock.mcfunction.commandParts.NativePart;
import net.villagerzock.mcfunction.commandParts.TmpWrite;
import net.villagerzock.snbt.Example;
import net.villagerzock.snbt.SnbtCompound;

import java.awt.geom.Line2D;
import java.sql.Struct;
import java.util.List;
import java.util.Stack;

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
                    System.out.println("Native Part: " + methodDeclaration.nativeBody().getCode());
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
            }
        }
        if (statement instanceof IfStatement ifStatement){
            Statement then = ifStatement.thenBranch();
            if (then instanceof BlockStatement blockStatement){
                MCFunction f = unit.create(function.getNamespace(), function.getPath(), baseName + "_if");
                blockStatement.setAssociatedFunction(f);
                generateBlock(blockStatement,unit,f,pathStack,baseName);
            }
        }
    }
}
