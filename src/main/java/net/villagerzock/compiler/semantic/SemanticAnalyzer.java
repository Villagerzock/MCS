package net.villagerzock.compiler.semantic;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.Node;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.compiler.ast.decl.*;
import net.villagerzock.compiler.ast.expr.*;
import net.villagerzock.compiler.ast.stmt.*;

import java.util.*;

public final class SemanticAnalyzer {
	private static final StaticImportDeclaration STD_STATIC_IMPORT = new StaticImportDeclaration(
			new QualifiedPathNode("std", List.of("std", "Std", "*"))
	);

	private final List<SemanticDiagnostic> diagnostics = new ArrayList<>();
	private final Map<String, ProgramInfo> programsByPackage = new LinkedHashMap<>();

	private ProgramInfo currentProgram;
	private Map<String, ClassInfo> currentVisibleClasses = Map.of();
	private Map<String, MethodSymbol> currentVisibleStaticMethods = Map.of();

	private ClassInfo currentClass;
	private MethodSymbol currentMethod;
	private Scope currentScope;

	private SemanticType createSemanticType(net.villagerzock.compiler.ast.type.TypeNode typeNode) {
		SemanticType type = SemanticType.from(typeNode);

		if (type == null || type.isBuiltin() || type.isUnknown()) {
			return type;
		}

		ProgramNode node = currentProgram.node;
		type.setNamespace(node.packagePath().namespace());
		type.setPath(node.packagePath().path());

		return type;
	}

	public List<SemanticDiagnostic> analyze(List<? extends Node> nodes) {
		diagnostics.clear();
		programsByPackage.clear();
		currentProgram = null;
		currentVisibleClasses = Map.of();
		currentVisibleStaticMethods = Map.of();
		currentClass = null;
		currentMethod = null;
		currentScope = null;

		List<ProgramNode> programs = collectAnalyzablePrograms(nodes);

		collectPrograms(programs);
		collectAllClasses();
		collectAllMembers();

		for (ProgramInfo program : programsByPackage.values()) {
			checkProgram(program);
		}

		return List.copyOf(diagnostics);
	}

	public void analyzeOrThrow(List<? extends Node> nodes) {
		List<SemanticDiagnostic> result = analyze(nodes);
		if (!result.isEmpty()) {
			throw new SemanticException(result);
		}
	}

	private List<ProgramNode> collectAnalyzablePrograms(List<? extends Node> nodes) {
		List<ProgramNode> programs = new ArrayList<>();

		for (Node node : nodes) {
			if (node instanceof ISemanticAnalyzed) {
				continue;
			}

			if (node instanceof ProgramNode program) {
				programs.add(program);
			}
		}

		return programs;
	}

	private void collectPrograms(List<ProgramNode> programs) {
		for (ProgramNode program : programs) {
			String packageName = packageName(program);

			if (programsByPackage.containsKey(packageName)) {
				currentProgram = new ProgramInfo(packageName, program);
				error(program, "Duplicate script package '" + packageName + "'.");
				currentProgram = null;
				continue;
			}

			programsByPackage.put(packageName, new ProgramInfo(packageName, program));
		}
	}

	private void collectAllClasses() {
		for (ProgramInfo program : programsByPackage.values()) {
			currentProgram = program;

			for (ClassDeclaration classDeclaration : program.node.classes()) {
				if (program.classes.containsKey(classDeclaration.name())) {
					error(
							classDeclaration,
							"Duplicate class '" + classDeclaration.name()
									+ "' in package '" + program.packageName + "'."
					);
					continue;
				}

				program.classes.put(classDeclaration.name(), new ClassInfo(program, classDeclaration));
			}
		}

		currentProgram = null;
	}

	private void collectAllMembers() {
		for (ProgramInfo program : programsByPackage.values()) {
			currentProgram = program;

			for (ClassInfo classInfo : program.classes.values()) {
				currentClass = classInfo;

				for (Declaration member : classInfo.declaration.members()) {
					if (member instanceof FieldDeclaration field) {
						collectField(classInfo, field);
					} else if (member instanceof MethodDeclaration method) {
						collectMethod(classInfo, method);
					}
				}

				currentClass = null;
			}
		}

		currentProgram = null;
	}

	private void collectField(ClassInfo classInfo, FieldDeclaration field) {
		if (classInfo.fields.containsKey(field.name()) || classInfo.methods.containsKey(field.name())) {
			error(
					field,
					"Duplicate member '" + field.name()
							+ "' in class '" + classInfo.qualifiedName() + "'."
			);
			return;
		}

		SemanticType fieldType = createSemanticType(field.type());

		Symbol symbol = new Symbol(field.name(), fieldType, SymbolKind.FIELD, field);
		field.setResolvedSymbol(symbol);

		classInfo.fields.put(field.name(), symbol);
	}

	private void collectMethod(ClassInfo classInfo, MethodDeclaration method) {
		if (classInfo.fields.containsKey(method.name()) || classInfo.methods.containsKey(method.name())) {
			error(
					method,
					"Duplicate member '" + method.name()
							+ "' in class '" + classInfo.qualifiedName() + "'."
			);
			return;
		}

		if (method.isNative() && method.nativeBody() == null) {
			error(method, "Native method '" + method.name() + "' must have a native body.");
		}

		if (!method.isNative() && method.body() == null) {
			error(method, "Method '" + method.name() + "' must have a body.");
		}

		List<Symbol> parameters = new ArrayList<>();
		Set<String> parameterNames = new HashSet<>();

		for (ParameterDeclaration parameter : method.parameters()) {
			if (!parameterNames.add(parameter.name())) {
				error(
						parameter,
						"Duplicate parameter '" + parameter.name()
								+ "' in method '" + method.name() + "'."
				);
				continue;
			}

			SemanticType parameterType = createSemanticType(parameter.type());
			Symbol parameterSymbol = new Symbol(
					parameter.name(),
					parameterType,
					SymbolKind.PARAMETER,
					parameter
			);
			parameter.setResolvedSymbol(parameterSymbol);
			parameters.add(parameterSymbol);
		}

		SemanticType returnType = createSemanticType(method.returnType());
		MethodSymbol methodSymbol = new MethodSymbol(
				method.name(),
				returnType,
				List.copyOf(parameters),
				method
		);
		method.setResolvedMethodSymbol(methodSymbol);
		classInfo.methods.put(method.name(), methodSymbol);
	}

	private void checkProgram(ProgramInfo program) {
		currentProgram = program;
		currentVisibleClasses = buildVisibleClasses(program);
		currentVisibleStaticMethods = buildVisibleStaticMethods(program);

		checkTypeDeclarations(program);

		for (ClassInfo classInfo : program.classes.values()) {
			currentClass = classInfo;

			checkFields(classInfo);

			for (MethodSymbol method : classInfo.methods.values()) {
				checkMethod(method);
			}

			currentClass = null;
		}

		currentProgram = null;
		currentVisibleClasses = Map.of();
		currentVisibleStaticMethods = Map.of();
		currentMethod = null;
		currentScope = null;
	}

	private Map<String, ClassInfo> buildVisibleClasses(ProgramInfo program) {
		Map<String, ClassInfo> visible = new LinkedHashMap<>();

		for (ImportDeclaration importDeclaration : program.node.imports()) {
			addImportedClasses(visible, program, importDeclaration);
		}

		for (ClassInfo classInfo : program.classes.values()) {
			ClassInfo conflict = visible.put(classInfo.declaration.name(), classInfo);

			if (conflict != null && conflict != classInfo) {
				error(
						classInfo.declaration,
						"Class name conflict for '" + classInfo.declaration.name()
								+ "'. Current package '" + program.packageName
								+ "' conflicts with imported class from package '"
								+ conflict.owner.packageName + "'."
				);
			}
		}

		return visible;
	}

	private Map<String, MethodSymbol> buildVisibleStaticMethods(ProgramInfo program) {
		Map<String, MethodSymbol> visible = new LinkedHashMap<>();

		addStaticImportedMethods(visible, program, STD_STATIC_IMPORT);

		for (StaticImportDeclaration staticImportDeclaration : program.node.staticImports()) {
			addStaticImportedMethods(visible, program, staticImportDeclaration);
		}

		return visible;
	}

	private void addImportedClasses(
			Map<String, ClassInfo> visible,
			ProgramInfo current,
			ImportDeclaration importDeclaration
	) {
		QualifiedPathNode path = importDeclaration.path();
		if (path == null) {
			return;
		}

		List<String> segments = path.segments();
		if (segments.isEmpty()) {
			error(importDeclaration, "Invalid import '" + importName(importDeclaration) + "'.");
			return;
		}

		String last = segments.get(segments.size() - 1);
		if ("*".equals(last)) {
			String packageName = toPackageName(path.namespace(), segments.subList(0, segments.size() - 1));
			addImportedPackageClasses(visible, current, packageName, importDeclaration);
			return;
		}

		String className = last;
		String packageName = toPackageName(path.namespace(), segments.subList(0, segments.size() - 1));
		ProgramInfo imported = programsByPackage.get(packageName);

		if (imported == null) {
			error(
					importDeclaration,
					"Unknown import package '" + packageName
							+ "' while checking package '" + current.packageName + "'."
			);
			return;
		}

		ClassInfo importedClass = imported.classes.get(className);
		if (importedClass == null) {
			error(
					importDeclaration,
					"Unknown imported class '" + className
							+ "' in package '" + packageName + "'."
			);
			return;
		}

		ClassInfo conflict = visible.putIfAbsent(importedClass.declaration.name(), importedClass);
		if (conflict != null && conflict != importedClass) {
			error(
					importDeclaration,
					"Imported class name conflict for '" + importedClass.declaration.name()
							+ "'. Package '" + imported.packageName
							+ "' conflicts with package '" + conflict.owner.packageName + "'."
			);
		}
	}

	private void addImportedPackageClasses(
			Map<String, ClassInfo> visible,
			ProgramInfo current,
			String packageName,
			Node errorNode
	) {
		ProgramInfo imported = programsByPackage.get(packageName);

		if (imported == null) {
			error(
					errorNode,
					"Unknown import package '" + packageName
							+ "' while checking package '" + current.packageName + "'."
			);
			return;
		}

		for (ClassInfo importedClass : imported.classes.values()) {
			ClassInfo conflict = visible.putIfAbsent(importedClass.declaration.name(), importedClass);

			if (conflict != null && conflict != importedClass) {
				error(
						errorNode,
						"Imported class name conflict for '" + importedClass.declaration.name()
								+ "'. Package '" + imported.packageName
								+ "' conflicts with package '" + conflict.owner.packageName + "'."
				);
			}
		}
	}

	private void addStaticImportedMethods(
			Map<String, MethodSymbol> visible,
			ProgramInfo current,
			StaticImportDeclaration staticImportDeclaration
	) {
		QualifiedPathNode path = staticImportDeclaration.path();
		if (path == null) {
			return;
		}

		List<String> segments = path.segments();
		if (segments.size() < 2) {
			error(staticImportDeclaration, "Invalid static import '" + staticImportName(staticImportDeclaration) + "'.");
			return;
		}

		String memberName = segments.get(segments.size() - 1);
		String className = segments.get(segments.size() - 2);
		String packageName = toPackageName(path.namespace(), segments.subList(0, segments.size() - 2));

		ProgramInfo imported = programsByPackage.get(packageName);
		if (imported == null) {
			error(
					staticImportDeclaration,
					"Unknown static import package '" + packageName
							+ "' while checking package '" + current.packageName + "'."
			);
			return;
		}

		ClassInfo importedClass = imported.classes.get(className);
		if (importedClass == null) {
			error(
					staticImportDeclaration,
					"Unknown static import class '" + className
							+ "' in package '" + packageName + "'."
			);
			return;
		}

		if ("*".equals(memberName)) {
			for (MethodSymbol method : importedClass.methods.values()) {
				addVisibleStaticMethod(visible, staticImportDeclaration, method, importedClass);
			}
			return;
		}

		MethodSymbol method = importedClass.methods.get(memberName);
		if (method == null) {
			error(
					staticImportDeclaration,
					"Unknown static imported method '" + memberName
							+ "' on class '" + importedClass.qualifiedName() + "'."
			);
			return;
		}

		addVisibleStaticMethod(visible, staticImportDeclaration, method, importedClass);
	}

	private void addVisibleStaticMethod(
			Map<String, MethodSymbol> visible,
			Node errorNode,
			MethodSymbol method,
			ClassInfo importedClass
	) {
		MethodSymbol conflict = visible.putIfAbsent(method.name(), method);

		if (conflict != null && conflict != method) {
			error(
					errorNode,
					"Static import method conflict for '" + method.name()
							+ "'. Class '" + importedClass.qualifiedName()
							+ "' conflicts with another static import."
			);
		}
	}

	private void checkTypeDeclarations(ProgramInfo program) {
		for (ClassInfo classInfo : program.classes.values()) {
			currentClass = classInfo;

			for (Symbol field : classInfo.fields.values()) {
				checkKnownType(((FieldDeclaration) field.declaration()).type(), field.type(), "field '" + field.name() + "'");
			}

			for (MethodSymbol method : classInfo.methods.values()) {
				checkKnownType(method.declaration().returnType(), method.returnType(), "return type of method '" + method.name() + "'");

				for (Symbol parameter : method.parameters()) {
					checkKnownType(
							((ParameterDeclaration) parameter.declaration()).type(),
							parameter.type(),
							"parameter '" + parameter.name() + "' of method '" + method.name() + "'"
					);
				}
			}

			currentClass = null;
		}
	}

	private void checkFields(ClassInfo classInfo) {
		for (Symbol field : classInfo.fields.values()) {
			FieldDeclaration declaration = (FieldDeclaration) field.declaration();

			if (declaration.initializer() != null) {
				currentScope = fieldScope(classInfo);

				SemanticType initializerType = checkExpression(declaration.initializer());
				if (!field.type().isAssignableFrom(initializerType)) {
					error(
							declaration.initializer(),
							"Cannot assign '" + initializerType
									+ "' to field '" + field.name()
									+ "' of type '" + field.type() + "'."
					);
				}
			}
		}
	}

	private Scope fieldScope(ClassInfo classInfo) {
		Scope scope = new Scope(null);

		for (Symbol field : classInfo.fields.values()) {
			scope.define(field);
		}

		for (MethodSymbol method : classInfo.methods.values()) {
			scope.define(new Symbol(
					method.name(),
					method.returnType(),
					SymbolKind.METHOD,
					method.declaration()
			));
		}

		return scope;
	}

	private void checkMethod(MethodSymbol method) {
		currentMethod = method;
		currentScope = fieldScope(currentClass);

		for (Symbol parameter : method.parameters()) {
			currentScope.define(parameter);
		}

		MethodDeclaration declaration = method.declaration();
		if (declaration.isNative()) {
			if (declaration.body() != null) {
				error(declaration, "Native method '" + declaration.name() + "' must not have a normal AST body.");
			}
			if (declaration.nativeBody() == null) {
				error(declaration, "Native method '" + declaration.name() + "' has no native body.");
			}

			currentMethod = null;
			currentScope = null;
			return;
		}

		if (declaration.body() == null) {
			error(declaration, "Method '" + declaration.name() + "' has no body.");
			currentMethod = null;
			currentScope = null;
			return;
		}

		checkBlock(declaration.body(), false);

		currentMethod = null;
		currentScope = null;
	}

	private void checkBlock(BlockStatement block, boolean createScope) {
		Scope previous = currentScope;

		if (createScope) {
			currentScope = new Scope(currentScope);
		}

		for (Statement statement : block.statements()) {
			checkStatement(statement);
		}

		currentScope = previous;
	}

	private void checkStatement(Statement statement) {
		if (statement instanceof BlockStatement block) {
			checkBlock(block, true);
		} else if (statement instanceof VariableDeclarationStatement variable) {
			checkVariableDeclaration(variable);
		} else if (statement instanceof ExpressionStatement expressionStatement) {
			checkExpression(expressionStatement.expression());
		} else if (statement instanceof IfStatement ifStatement) {
			SemanticType conditionType = checkExpression(ifStatement.condition());

			if (!isConditionExpressionAllowed(conditionType)) {
				error(
						ifStatement.condition(),
						"If condition cannot be '" + conditionType + "'."
				);
			}

			checkStatement(ifStatement.thenBranch());

			if (ifStatement.elseBranch() != null) {
				checkStatement(ifStatement.elseBranch());
			}
		} else if (statement instanceof WhileStatement whileStatement) {
			checkWhile(whileStatement);
		} else if (statement instanceof ForStatement forStatement) {
			checkFor(forStatement);
		} else if (statement instanceof ReturnStatement returnStatement) {
			checkReturn(returnStatement);
		} else {
			error(statement, "Unsupported statement: " + statement.getClass().getSimpleName() + ".");
		}
	}

	private boolean isConditionExpressionAllowed(SemanticType type) {
		return type == null || !type.isVoid();
	}

	private void checkWhile(WhileStatement statement) {
		SemanticType conditionType = checkExpression(statement.condition());

		if (!isConditionExpressionAllowed(conditionType)) {
			error(
					statement.condition(),
					"While condition cannot be '" + conditionType + "'."
			);
		}

		checkStatement(statement.body());
	}

	private void checkFor(ForStatement statement) {
		Scope previous = currentScope;
		currentScope = new Scope(currentScope);

		if (statement.initializer() != null) {
			checkStatement(statement.initializer());
		}

		if (statement.condition() != null) {
			SemanticType conditionType = checkExpression(statement.condition());

			if (!isConditionExpressionAllowed(conditionType)) {
				error(
						statement.condition(),
						"For condition cannot be '" + conditionType + "'."
				);
			}
		}

		if (statement.update() != null) {
			checkExpression(statement.update());
		}

		checkStatement(statement.body());

		currentScope = previous;
	}

	private void checkVariableDeclaration(VariableDeclarationStatement variable) {
		SemanticType declaredType = createSemanticType(variable.type());

		checkKnownType(variable.type(), declaredType, "variable '" + variable.name() + "'");

		if (declaredType.isVoid()) {
			error(variable, "Variable '" + variable.name() + "' cannot have type 'function'.");
		}

		Symbol variableSymbol = new Symbol(variable.name(), declaredType, SymbolKind.LOCAL, variable);
		variable.setResolvedSymbol(variableSymbol);

		if (!currentScope.define(variableSymbol)) {
			error(variable, "Variable '" + variable.name() + "' is already declared in this scope.");
		}

		if (variable.initializer() != null) {
			SemanticType initializerType = checkExpression(variable.initializer());

			if (!declaredType.isAssignableFrom(initializerType)) {
				error(
						variable.initializer(),
						"Cannot assign '" + initializerType
								+ "' to variable '" + variable.name()
								+ "' of type '" + declaredType + "'."
				);
			}
		}
	}

	private void checkReturn(ReturnStatement statement) {
		SemanticType expected = currentMethod == null
				? SemanticType.UNKNOWN
				: currentMethod.returnType();

		SemanticType actual = statement.value() == null
				? SemanticType.VOID
				: checkExpression(statement.value());

		if (expected.isVoid() && statement.value() != null) {
			error(statement, "Method '" + currentMethod.name() + "' does not return a value.");
			return;
		}

		if (!expected.isVoid() && statement.value() == null) {
			error(statement, "Method '" + currentMethod.name() + "' must return '" + expected + "'.");
			return;
		}

		if (!expected.isAssignableFrom(actual)) {
			error(
					statement.value(),
					"Cannot return '" + actual
							+ "' from method '" + currentMethod.name()
							+ "' with return type '" + expected + "'."
			);
		}
	}

	private SemanticType checkExpression(Expression expression) {
		if (expression == null) {
			return SemanticType.VOID;
		}

		SemanticType result;

		if (expression instanceof NumberLiteralExpression) {
			result = SemanticType.INT;
		} else if (expression instanceof StringLiteralExpression) {
			result = SemanticType.STRING;
		} else if (expression instanceof BooleanLiteralExpression) {
			result = SemanticType.BOOLEAN;
		} else if (expression instanceof IdentifierExpression identifier) {
			Symbol symbol = currentScope.resolve(identifier.name());

			if (symbol == null) {
				error(identifier, "Unknown identifier '" + identifier.name() + "'.");
				result = SemanticType.UNKNOWN;
			} else {
				identifier.setResolvedSymbol(symbol);
				result = symbol.type();
			}
		} else if (expression instanceof GroupExpression group) {
			result = checkExpression(group.expression());
		} else if (expression instanceof UnaryExpression unary) {
			result = checkUnary(unary);
		} else if (expression instanceof BinaryExpression binary) {
			result = checkBinary(binary);
		} else if (expression instanceof AssignmentExpression assignment) {
			result = checkAssignment(assignment);
		} else if (expression instanceof UpdateExpression update) {
			result = checkUpdate(update);
		} else if (expression instanceof CallExpression call) {
			result = checkCall(call);
		} else if (expression instanceof SelectExpression memberAccess) {
			result = checkMemberAccess(memberAccess);
		} else {
			error(expression, "Unsupported expression: " + expression.getClass().getSimpleName() + ".");
			result = SemanticType.UNKNOWN;
		}

		expression.setResolvedType(result);
		return result;
	}

	private SemanticType checkUnary(UnaryExpression unary) {
		SemanticType operand = checkExpression(unary.operand());

		return switch (unary.operator()) {
			case NEGATE -> {
				if (!SemanticType.INT.isAssignableFrom(operand)) {
					error(unary.operand(), "Unary '-' requires int, got '" + operand + "'.");
				}

				yield SemanticType.INT;
			}

			case NOT -> {
				if (!SemanticType.BOOLEAN.isAssignableFrom(operand)) {
					error(unary.operand(), "Unary '!' requires boolean, got '" + operand + "'.");
				}

				yield SemanticType.BOOLEAN;
			}
		};
	}

	private SemanticType checkBinary(BinaryExpression binary) {
		SemanticType left = checkExpression(binary.left());
		SemanticType right = checkExpression(binary.right());

		return switch (binary.operator()) {
			case ADD -> {
				if (left.equals(SemanticType.STRING) || right.equals(SemanticType.STRING)) {
					yield SemanticType.STRING;
				}

				requireBoth(binary, left, right, SemanticType.INT);
				yield SemanticType.INT;
			}

			case SUBTRACT, MULTIPLY, DIVIDE, MODULO -> {
				requireBoth(binary, left, right, SemanticType.INT);
				yield SemanticType.INT;
			}

			case GREATER, LESS, GREATER_EQUAL, LESS_EQUAL -> {
				requireBoth(binary, left, right, SemanticType.INT);
				yield SemanticType.BOOLEAN;
			}

			case EQUAL, NOT_EQUAL -> {
				if (!left.isAssignableFrom(right) && !right.isAssignableFrom(left)) {
					error(binary, "Cannot compare '" + left + "' with '" + right + "'.");
				}

				yield SemanticType.BOOLEAN;
			}

			case LOGICAL_AND, LOGICAL_OR -> {
				requireBoth(binary, left, right, SemanticType.BOOLEAN);
				yield SemanticType.BOOLEAN;
			}
		};
	}

	private void requireBoth(BinaryExpression binary, SemanticType left, SemanticType right, SemanticType expected) {
		if (!expected.isAssignableFrom(left) || !expected.isAssignableFrom(right)) {
			error(
					binary,
					"Operator '" + binary.operator()
							+ "' requires '" + expected
							+ "' operands, got '" + left
							+ "' and '" + right + "'."
			);
		}
	}

	private SemanticType checkAssignment(AssignmentExpression assignment) {
		if (!(assignment.target() instanceof IdentifierExpression)
				&& !(assignment.target() instanceof SelectExpression)) {
			error(assignment.target(), "Assignment target must be a variable or field.");
		}

		SemanticType targetType = checkExpression(assignment.target());
		SemanticType valueType = checkExpression(assignment.value());

		if (!targetType.isAssignableFrom(valueType)) {
			error(
					assignment.value(),
					"Cannot assign '" + valueType + "' to '" + targetType + "'."
			);
		}

		return targetType;
	}

	private SemanticType checkUpdate(UpdateExpression update) {
		if (!(update.target() instanceof IdentifierExpression)
				&& !(update.target() instanceof SelectExpression)) {
			error(update.target(), "Update target must be a variable or field.");
		}

		SemanticType targetType = checkExpression(update.target());

		if (!SemanticType.INT.isAssignableFrom(targetType)) {
			error(
					update.target(),
					"Update operator '" + update.operator()
							+ "' requires int, got '" + targetType + "'."
			);
		}

		return targetType;
	}

	private SemanticType checkCall(CallExpression call) {
		MethodSymbol method = resolveMethod(call.callee());

		if (method == null) {
			error(call, "Unknown method call '" + describeExpression(call.callee()) + "'.");

			for (Expression argument : call.arguments()) {
				checkExpression(argument);
			}

			return SemanticType.UNKNOWN;
		}

		call.setResolvedMethod(method);
		call.setResolvedType(method.returnType());

		if (method.parameters().size() != call.arguments().size()) {
			error(
					call,
					"Method '" + method.name()
							+ "' expects " + method.parameters().size()
							+ " argument(s), got " + call.arguments().size() + "."
			);
		}

		int count = Math.min(method.parameters().size(), call.arguments().size());

		for (int i = 0; i < count; i++) {
			SemanticType actual = checkExpression(call.arguments().get(i));
			SemanticType expected = method.parameters().get(i).type();

			if (!expected.isAssignableFrom(actual)) {
				error(
						call.arguments().get(i),
						"Argument " + (i + 1)
								+ " of method '" + method.name()
								+ "' must be '" + expected
								+ "', got '" + actual + "'."
				);
			}
		}

		for (int i = count; i < call.arguments().size(); i++) {
			checkExpression(call.arguments().get(i));
		}

		return method.returnType();
	}

	private MethodSymbol resolveMethod(Expression callee) {
		if (callee instanceof IdentifierExpression identifier) {
			MethodSymbol local = currentClass.methods.get(identifier.name());

			if (local != null) {
				return local;
			}

			return currentVisibleStaticMethods.get(identifier.name());
		}

		if (callee instanceof SelectExpression memberAccess) {
			ClassInfo classInfo = resolveSelectTargetClass(memberAccess);

			if (classInfo == null) {
				return null;
			}

			MethodSymbol method = classInfo.methods.get(memberAccess.memberName());
			if (method != null) {
				memberAccess.setResolvedMethod(method);
				memberAccess.setResolvedType(method.returnType());
			}
			return method;
		}

		return null;
	}

	private SemanticType checkMemberAccess(SelectExpression memberAccess) {
		ClassInfo classInfo = resolveSelectTargetClass(memberAccess);

		if (classInfo == null) {
			SemanticType targetType = checkExpression(memberAccess.target());
			memberAccess.setResolvedTargetType(targetType);

			error(
					memberAccess,
					"Type '" + targetType
							+ "' has no known member '" + memberAccess.memberName() + "'."
			);
			return SemanticType.UNKNOWN;
		}

		Symbol field = classInfo.fields.get(memberAccess.memberName());
		if (field != null) {
			memberAccess.setResolvedField(field);
			memberAccess.setResolvedType(field.type());
			return field.type();
		}

		MethodSymbol method = classInfo.methods.get(memberAccess.memberName());
		if (method != null) {
			memberAccess.setResolvedMethod(method);
			memberAccess.setResolvedType(method.returnType());
			return method.returnType();
		}

		error(
				memberAccess,
				"Unknown member '" + memberAccess.memberName()
						+ "' on type '" + classInfo.declaration.name() + "'."
		);

		return SemanticType.UNKNOWN;
	}

	private ClassInfo resolveSelectTargetClass(SelectExpression memberAccess) {
		if (memberAccess.target() instanceof IdentifierExpression identifier) {
			ClassInfo directClass = currentVisibleClasses.get(identifier.name());
			if (directClass != null) {
				SemanticType targetType = SemanticType.fromName(identifier.name());
				memberAccess.setResolvedTargetType(targetType);
				memberAccess.setResolvedTargetClass(directClass.declaration);
				return directClass;
			}
		}

		SemanticType targetType = checkExpression(memberAccess.target());
		memberAccess.setResolvedTargetType(targetType);

		ClassInfo classInfo = currentVisibleClasses.get(targetType.name());
		if (classInfo != null) {
			memberAccess.setResolvedTargetClass(classInfo.declaration);
		}

		return classInfo;
	}

	private void checkKnownType(Node node, SemanticType type, String elementName) {
		if (node instanceof net.villagerzock.compiler.ast.type.TypeNode typeNode) {
			typeNode.setResolvedType(type);
		}

		if (type == null || type.isUnknown()) {
			return;
		}

		if (type.equals(SemanticType.INT)
				|| type.equals(SemanticType.STRING)
				|| type.equals(SemanticType.BOOLEAN)
				|| type.equals(SemanticType.VOID)) {
			return;
		}

		ClassInfo classInfo = currentVisibleClasses.get(type.name());
		if (classInfo == null) {
			error(
					node,
					"Unknown type '" + type.name()
							+ "' used by " + elementName + "."
			);
			return;
		}

		if (node instanceof net.villagerzock.compiler.ast.type.TypeNode typeNode) {
			typeNode.setResolvedClass(classInfo.declaration);
		}
	}

	private void error(Node node, String message) {
		SourceRange range = SourceRange.UNKNOWN;

		if (node instanceof AstNode astNode) {
			range = astNode.sourceRange();
		}

		diagnostics.add(new SemanticDiagnostic(range, message + contextSuffix(node)));
	}

	private String contextSuffix(Node node) {
		StringBuilder builder = new StringBuilder();

		if (node != null) {
			builder.append(" At ")
					.append(node.getClass().getSimpleName());

			String nodeName = nodeName(node);
			if (nodeName != null) {
				builder.append(" '").append(nodeName).append("'");
			}
		}

		if (currentProgram != null) {
			builder.append(" in package '")
					.append(currentProgram.packageName)
					.append("'");
		}

		if (currentClass != null) {
			builder.append(", class '")
					.append(currentClass.declaration.name())
					.append("'");
		}

		if (currentMethod != null) {
			builder.append(", method '")
					.append(currentMethod.name())
					.append("'");
		}

		builder.append(".");
		return builder.toString();
	}

	private String nodeName(Node node) {
		if (node instanceof ProgramNode program) {
			return packageName(program);
		}

		if (node instanceof ImportDeclaration importDeclaration) {
			return importName(importDeclaration);
		}

		if (node instanceof StaticImportDeclaration staticImportDeclaration) {
			return staticImportName(staticImportDeclaration);
		}

		if (node instanceof ClassDeclaration classDeclaration) {
			return classDeclaration.name();
		}

		if (node instanceof FieldDeclaration fieldDeclaration) {
			return fieldDeclaration.name();
		}

		if (node instanceof MethodDeclaration methodDeclaration) {
			return methodDeclaration.name();
		}

		if (node instanceof ParameterDeclaration parameterDeclaration) {
			return parameterDeclaration.name();
		}

		if (node instanceof VariableDeclarationStatement variableDeclaration) {
			return variableDeclaration.name();
		}

		if (node instanceof ForStatement) {
			return "for";
		}

		if (node instanceof WhileStatement) {
			return "while";
		}

		if (node instanceof IdentifierExpression identifierExpression) {
			return identifierExpression.name();
		}

		if (node instanceof SelectExpression selectExpression) {
			return describeExpression(selectExpression);
		}

		if (node instanceof CallExpression callExpression) {
			return describeExpression(callExpression);
		}

		return null;
	}

	private String describeExpression(Expression expression) {
		if (expression instanceof IdentifierExpression identifier) {
			return identifier.name();
		}

		if (expression instanceof SelectExpression memberAccess) {
			return describeExpression(memberAccess.target()) + "." + memberAccess.memberName();
		}

		if (expression instanceof CallExpression call) {
			return describeExpression(call.callee()) + "(...)";
		}

		if (expression instanceof AssignmentExpression assignment) {
			return describeExpression(assignment.target()) + " = ...";
		}

		if (expression instanceof UpdateExpression update) {
			return describeExpression(update.target()) + update.operator();
		}

		if (expression instanceof BinaryExpression binary) {
			return describeExpression(binary.left())
					+ " " + binary.operator()
					+ " " + describeExpression(binary.right());
		}

		if (expression instanceof UnaryExpression unary) {
			return unary.operator() + " " + describeExpression(unary.operand());
		}

		if (expression instanceof GroupExpression group) {
			return "(" + describeExpression(group.expression()) + ")";
		}

		if (expression instanceof NumberLiteralExpression) {
			return "<number>";
		}

		if (expression instanceof StringLiteralExpression) {
			return "<string>";
		}

		if (expression instanceof BooleanLiteralExpression) {
			return "<boolean>";
		}

		return expression.getClass().getSimpleName();
	}

	private String packageName(ProgramNode program) {
		if (program.packagePath() == null) {
			return "";
		}

		return program.packagePath().asImportString();
	}

	private String importName(ImportDeclaration importDeclaration) {
		if (importDeclaration.path() == null) {
			return "";
		}

		return importDeclaration.path().asImportString();
	}

	private String staticImportName(StaticImportDeclaration staticImportDeclaration) {
		if (staticImportDeclaration.path() == null) {
			return "";
		}

		return staticImportDeclaration.path().asImportString();
	}

	private String toPackageName(String namespace, List<String> segments) {
		if (segments.isEmpty()) {
			return namespace + ":";
		}

		return namespace + ":" + String.join(".", segments);
	}

	private static final class ProgramInfo {
		private final String packageName;
		private final ProgramNode node;
		private final Map<String, ClassInfo> classes = new LinkedHashMap<>();

		private ProgramInfo(String packageName, ProgramNode node) {
			this.packageName = packageName;
			this.node = node;
		}
	}

	private static final class ClassInfo {
		private final ProgramInfo owner;
		private final ClassDeclaration declaration;
		private final Map<String, Symbol> fields = new LinkedHashMap<>();
		private final Map<String, MethodSymbol> methods = new LinkedHashMap<>();

		private ClassInfo(ProgramInfo owner, ClassDeclaration declaration) {
			this.owner = owner;
			this.declaration = declaration;
		}

		private String qualifiedName() {
			return owner.packageName + "." + declaration.name();
		}
	}
}
