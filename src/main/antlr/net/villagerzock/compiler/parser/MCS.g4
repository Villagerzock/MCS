grammar MCS;

program
	: packageDecl? importDecl* classDecl* EOF
	;

packageDecl
	: 'package' qualifiedPath
	;

importDecl
	: 'import' qualifiedPath
	;

qualifiedPath
	: IDENTIFIER ':' pathSegment ('/' pathSegment)*
	;

pathSegment
	: IDENTIFIER
	;

classDecl
	: 'class' IDENTIFIER classBody
	;

classBody
	: '{' memberDecl* '}'
	;

memberDecl
	: fieldDecl
	| methodDecl
	;

fieldDecl
	: typeName IDENTIFIER ('=' expression)? ';'
	;

methodDecl
	: returnType IDENTIFIER '(' parameterList? ')' block
	;

returnType
	: 'function'
	| typeName
	;

typeName
	: IDENTIFIER
	;

parameterList
	: parameter (',' parameter)*
	;

parameter
	: typeName IDENTIFIER
	;

block
	: '{' statement* '}'
	;

statement
	: block
	| ifStatement
	| returnStatement
	| variableDeclStatement
	| expressionStatement
	;

ifStatement
	: 'if' '(' expression ')' statement ('else' statement)?
	;

returnStatement
	: 'return' expression? ';'
	;

variableDeclStatement
	: typeName IDENTIFIER ('=' expression)? ';'
	;

expressionStatement
	: expression ';'
	;

expression
	: assignmentExpression
	;

assignmentExpression
	: postfixExpression '=' assignmentExpression
	| logicalOrExpression
	;

logicalOrExpression
	: logicalAndExpression ('||' logicalAndExpression)*
	;

logicalAndExpression
	: equalityExpression ('&&' equalityExpression)*
	;

equalityExpression
	: relationalExpression (('==' | '!=') relationalExpression)*
	;

relationalExpression
	: additiveExpression (('>' | '<' | '>=' | '<=') additiveExpression)*
	;

additiveExpression
	: multiplicativeExpression (('+' | '-') multiplicativeExpression)*
	;

multiplicativeExpression
	: unaryExpression (('*' | '/' | '%') unaryExpression)*
	;

unaryExpression
	: '!' unaryExpression
	| '-' unaryExpression
	| postfixExpression
	;

postfixExpression
	: primaryExpression postfixSuffix*
	;

postfixSuffix
	: '.' IDENTIFIER
	| '.' IDENTIFIER '(' argumentList? ')'
	| '(' argumentList? ')'
	;

argumentList
	: expression (',' expression)*
	;

primaryExpression
	: NUMBER
	| STRING
	| 'true'
	| 'false'
	| IDENTIFIER
	| '(' expression ')'
	;

IDENTIFIER
	: [a-zA-Z_][a-zA-Z0-9_]*
	;

NUMBER
	: [0-9]+
	;

STRING
	: '"' (~["\\] | '\\' .)* '"'
	;

LINE_COMMENT
	: '//' ~[\r\n]* -> skip
	;

BLOCK_COMMENT
	: '/*' .*? '*/' -> skip
	;

WS
	: [ \t\r\n]+ -> skip
	;