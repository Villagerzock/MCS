parser grammar MCSParser;

options {
	tokenVocab = MCSLexer;
}

program
	: packageDecl? importDecl* classDecl* EOF
	;

packageDecl
	: PACKAGE qualifiedPath SEMICOLON
	;

importDecl
	: IMPORT qualifiedPath SEMICOLON
	;

qualifiedPath
	: IDENTIFIER COLON pathSegment (SLASH pathSegment)*
	;

pathSegment
	: IDENTIFIER
	;

classDecl
	: CLASS IDENTIFIER classBody
	;

classBody
	: LBRACE memberDecl* RBRACE
	;

memberDecl
	: fieldDecl
	| methodDecl
	;

fieldDecl
	: typeName IDENTIFIER (EQUAL expression)? SEMICOLON
	;

methodDecl
	: nativeMethodDecl
	| normalMethodDecl
	;

nativeMethodDecl
	: NATIVE returnType IDENTIFIER LPAREN parameterList? RPAREN nativeBlock
	;

normalMethodDecl
	: methodModifier* returnType IDENTIFIER LPAREN parameterList? RPAREN block
	;

methodModifier
	: REPLACE
	;

nativeBlock
	: NATIVE_BLOCK_START .*? NATIVE_BLOCK_END
	;

returnType
	: FUNCTION
	| typeName
	;

typeName
	: IDENTIFIER
	;

parameterList
	: parameter (COMMA parameter)*
	;

parameter
	: typeName IDENTIFIER
	;

block
	: LBRACE statement* RBRACE
	;

statement
	: block
	| ifStatement
	| returnStatement
	| variableDeclStatement
	| forStatement
	| whileStatement
	| expressionStatement
	;

whileStatement
	: WHILE LPAREN expression RPAREN statement
	;

forStatement
	: FOR LPAREN forInit? SEMICOLON expression? SEMICOLON forUpdate? RPAREN statement
	;

forInit
	: variableDecl
	| expression
	;

forUpdate
	: expression
	;

ifStatement
	: IF LPAREN expression RPAREN statement (ELSE statement)?
	;

returnStatement
	: RETURN expression? SEMICOLON
	;

variableDeclStatement
	: variableDecl SEMICOLON
	;

variableDecl
	: typeName IDENTIFIER (EQUAL expression)?
	;

expressionStatement
	: expression SEMICOLON
	;

expression
	: assignmentExpression
	;

assignmentExpression
	: postfixExpression EQUAL assignmentExpression
	| logicalOrExpression
	;

logicalOrExpression
	: logicalAndExpression (LOGICAL_OR logicalAndExpression)*
	;

logicalAndExpression
	: equalityExpression (LOGICAL_AND equalityExpression)*
	;

equalityExpression
	: relationalExpression ((EQUAL_EQUAL | NOT_EQUAL) relationalExpression)*
	;

relationalExpression
	: additiveExpression ((GT | LT | GREATER_EQUAL | LESS_EQUAL) additiveExpression)*
	;

additiveExpression
	: multiplicativeExpression ((PLUS | MINUS) multiplicativeExpression)*
	;

multiplicativeExpression
	: unaryExpression ((STAR | SLASH | PERCENT) unaryExpression)*
	;

unaryExpression
	: EXCLAMATION unaryExpression
	| MINUS unaryExpression
	| postfixExpression
	;

postfixExpression
	: primaryExpression (postfixSuffix | postfixOp)*
	;

postfixSuffix
	: DOT IDENTIFIER
	| DOT IDENTIFIER LPAREN argumentList? RPAREN
	| LPAREN argumentList? RPAREN
	;

postfixOp
	: PLUS_PLUS
	| MINUS_MINUS
	;

argumentList
	: expression (COMMA expression)*
	;

primaryExpression
	: NUMBER
	| STRING
	| TRUE
	| FALSE
	| IDENTIFIER
	| LPAREN expression RPAREN
	;
