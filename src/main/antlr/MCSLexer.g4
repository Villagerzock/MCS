lexer grammar MCSLexer;



@members {
	private boolean inNativeDeclaration = false;
	private boolean expectNativeBlock = false;
	private int nativeParenDepth = 0;
	private int nativeBraceDepth = 0;
}

PACKAGE : 'package';
IMPORT : 'import';
CLASS : 'class';
NATIVE : 'native' { inNativeDeclaration = true; nativeParenDepth = 0; expectNativeBlock = false; };
STATIC : 'static';
FUNCTION : 'function';
IF : 'if';
ELSE : 'else';
RETURN : 'return';
TRUE : 'true';
FALSE : 'false';
CONSTRUCTOR : 'constructor';
NEW : 'new';
NATIVE_BLOCK_START
	: {expectNativeBlock}? '{'
		{
			expectNativeBlock = false;
			inNativeDeclaration = false;
			nativeBraceDepth = 1;
		}
		-> pushMode(NATIVE_BODY)
	;

LBRACE : '{';
RBRACE : '}';
LPAREN
	: '('
		{
			if (inNativeDeclaration) {
				nativeParenDepth++;
			}
		}
	;

RPAREN
	: ')'
		{
			if (inNativeDeclaration) {
				nativeParenDepth--;
				if (nativeParenDepth == 0) {
					expectNativeBlock = true;
				}
			}
		}
	;
FOR : 'for';
WHILE : 'while';

PLUS_PLUS : '++';
MINUS_MINUS : '--';

LBRACKET : '[';
RBRACKET : ']';
DOT : '.';
COMMA : ',';
COLON : ':';
SEMICOLON : ';';
EQUAL : '=';
PERCENT : '%';
DOLLAR : '$';
AT : '@';
TILDE : '~';
SLASH : '/';
MINUS : '-';
PLUS : '+';
STAR : '*';
LT : '<';
GT : '>';
EXCLAMATION : '!';
LOGICAL_OR : '||';
LOGICAL_AND : '&&';
EQUAL_EQUAL : '==';
NOT_EQUAL : '!=';
GREATER_EQUAL : '>=';
LESS_EQUAL : '<=';

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




mode NATIVE_BODY;

NATIVE_INNER_LBRACE
	: '{' { nativeBraceDepth++; } -> type(NATIVE_TEXT)
	;

NATIVE_INNER_RBRACE
	: {nativeBraceDepth > 1}? '}' { nativeBraceDepth--; } -> type(NATIVE_TEXT)
	;

NATIVE_BLOCK_END
	: '}' {
		nativeBraceDepth = 0;
		popMode();
	}
	;

NATIVE_TEXT
	: ~[{}]+
	;