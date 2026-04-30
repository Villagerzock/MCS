// Generated from //wsl.localhost/Debian/home/ssiebm/projects/MCS/src/main/antlr/MCSLexer.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class MCSLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		PACKAGE=1, IMPORT=2, CLASS=3, NATIVE=4, STATIC=5, FUNCTION=6, IF=7, ELSE=8, 
		RETURN=9, TRUE=10, FALSE=11, NATIVE_BLOCK_START=12, LBRACE=13, RBRACE=14, 
		LPAREN=15, RPAREN=16, FOR=17, WHILE=18, PLUS_PLUS=19, MINUS_MINUS=20, 
		LBRACKET=21, RBRACKET=22, DOT=23, COMMA=24, COLON=25, SEMICOLON=26, EQUAL=27, 
		PERCENT=28, DOLLAR=29, AT=30, TILDE=31, SLASH=32, MINUS=33, PLUS=34, STAR=35, 
		LT=36, GT=37, EXCLAMATION=38, LOGICAL_OR=39, LOGICAL_AND=40, EQUAL_EQUAL=41, 
		NOT_EQUAL=42, GREATER_EQUAL=43, LESS_EQUAL=44, IDENTIFIER=45, NUMBER=46, 
		STRING=47, LINE_COMMENT=48, BLOCK_COMMENT=49, WS=50, NATIVE_BLOCK_END=51, 
		NATIVE_TEXT=52;
	public static final int
		NATIVE_BODY=1;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE", "NATIVE_BODY"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"PACKAGE", "IMPORT", "CLASS", "NATIVE", "STATIC", "FUNCTION", "IF", "ELSE", 
			"RETURN", "TRUE", "FALSE", "NATIVE_BLOCK_START", "LBRACE", "RBRACE", 
			"LPAREN", "RPAREN", "FOR", "WHILE", "PLUS_PLUS", "MINUS_MINUS", "LBRACKET", 
			"RBRACKET", "DOT", "COMMA", "COLON", "SEMICOLON", "EQUAL", "PERCENT", 
			"DOLLAR", "AT", "TILDE", "SLASH", "MINUS", "PLUS", "STAR", "LT", "GT", 
			"EXCLAMATION", "LOGICAL_OR", "LOGICAL_AND", "EQUAL_EQUAL", "NOT_EQUAL", 
			"GREATER_EQUAL", "LESS_EQUAL", "IDENTIFIER", "NUMBER", "STRING", "LINE_COMMENT", 
			"BLOCK_COMMENT", "WS", "NATIVE_INNER_LBRACE", "NATIVE_INNER_RBRACE", 
			"NATIVE_BLOCK_END", "NATIVE_TEXT"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'package'", "'import'", "'class'", "'native'", "'static'", "'function'", 
			"'if'", "'else'", "'return'", "'true'", "'false'", null, "'{'", null, 
			"'('", "')'", "'for'", "'while'", "'++'", "'--'", "'['", "']'", "'.'", 
			"','", "':'", "';'", "'='", "'%'", "'$'", "'@'", "'~'", "'/'", "'-'", 
			"'+'", "'*'", "'<'", "'>'", "'!'", "'||'", "'&&'", "'=='", "'!='", "'>='", 
			"'<='"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "PACKAGE", "IMPORT", "CLASS", "NATIVE", "STATIC", "FUNCTION", "IF", 
			"ELSE", "RETURN", "TRUE", "FALSE", "NATIVE_BLOCK_START", "LBRACE", "RBRACE", 
			"LPAREN", "RPAREN", "FOR", "WHILE", "PLUS_PLUS", "MINUS_MINUS", "LBRACKET", 
			"RBRACKET", "DOT", "COMMA", "COLON", "SEMICOLON", "EQUAL", "PERCENT", 
			"DOLLAR", "AT", "TILDE", "SLASH", "MINUS", "PLUS", "STAR", "LT", "GT", 
			"EXCLAMATION", "LOGICAL_OR", "LOGICAL_AND", "EQUAL_EQUAL", "NOT_EQUAL", 
			"GREATER_EQUAL", "LESS_EQUAL", "IDENTIFIER", "NUMBER", "STRING", "LINE_COMMENT", 
			"BLOCK_COMMENT", "WS", "NATIVE_BLOCK_END", "NATIVE_TEXT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


		private boolean inNativeDeclaration = false;
		private boolean expectNativeBlock = false;
		private int nativeParenDepth = 0;
		private int nativeBraceDepth = 0;


	public MCSLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "MCSLexer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
		switch (ruleIndex) {
		case 3:
			NATIVE_action((RuleContext)_localctx, actionIndex);
			break;
		case 11:
			NATIVE_BLOCK_START_action((RuleContext)_localctx, actionIndex);
			break;
		case 14:
			LPAREN_action((RuleContext)_localctx, actionIndex);
			break;
		case 15:
			RPAREN_action((RuleContext)_localctx, actionIndex);
			break;
		case 50:
			NATIVE_INNER_LBRACE_action((RuleContext)_localctx, actionIndex);
			break;
		case 51:
			NATIVE_INNER_RBRACE_action((RuleContext)_localctx, actionIndex);
			break;
		case 52:
			NATIVE_BLOCK_END_action((RuleContext)_localctx, actionIndex);
			break;
		}
	}
	private void NATIVE_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0:
			 inNativeDeclaration = true; nativeParenDepth = 0; expectNativeBlock = false; 
			break;
		}
	}
	private void NATIVE_BLOCK_START_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 1:

						expectNativeBlock = false;
						inNativeDeclaration = false;
						nativeBraceDepth = 1;
					
			break;
		}
	}
	private void LPAREN_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 2:

						if (inNativeDeclaration) {
							nativeParenDepth++;
						}
					
			break;
		}
	}
	private void RPAREN_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 3:

						if (inNativeDeclaration) {
							nativeParenDepth--;
							if (nativeParenDepth == 0) {
								expectNativeBlock = true;
							}
						}
					
			break;
		}
	}
	private void NATIVE_INNER_LBRACE_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 4:
			 nativeBraceDepth++; 
			break;
		}
	}
	private void NATIVE_INNER_RBRACE_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 5:
			 nativeBraceDepth--; 
			break;
		}
	}
	private void NATIVE_BLOCK_END_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 6:

					nativeBraceDepth = 0;
					popMode();
				
			break;
		}
	}
	@Override
	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 11:
			return NATIVE_BLOCK_START_sempred((RuleContext)_localctx, predIndex);
		case 51:
			return NATIVE_INNER_RBRACE_sempred((RuleContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean NATIVE_BLOCK_START_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return expectNativeBlock;
		}
		return true;
	}
	private boolean NATIVE_INNER_RBRACE_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return nativeBraceDepth > 1;
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u00004\u0156\u0006\uffff\uffff\u0006\uffff\uffff\u0002\u0000\u0007"+
		"\u0000\u0002\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007"+
		"\u0003\u0002\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007"+
		"\u0006\u0002\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n"+
		"\u0007\n\u0002\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002"+
		"\u000e\u0007\u000e\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002"+
		"\u0011\u0007\u0011\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002"+
		"\u0014\u0007\u0014\u0002\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002"+
		"\u0017\u0007\u0017\u0002\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0002"+
		"\u001a\u0007\u001a\u0002\u001b\u0007\u001b\u0002\u001c\u0007\u001c\u0002"+
		"\u001d\u0007\u001d\u0002\u001e\u0007\u001e\u0002\u001f\u0007\u001f\u0002"+
		" \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0002#\u0007#\u0002$\u0007$\u0002"+
		"%\u0007%\u0002&\u0007&\u0002\'\u0007\'\u0002(\u0007(\u0002)\u0007)\u0002"+
		"*\u0007*\u0002+\u0007+\u0002,\u0007,\u0002-\u0007-\u0002.\u0007.\u0002"+
		"/\u0007/\u00020\u00070\u00021\u00071\u00022\u00072\u00023\u00073\u0002"+
		"4\u00074\u00025\u00075\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001"+
		"\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\r\u0001\r\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u0010\u0001"+
		"\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001"+
		"\u0011\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0013\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0015\u0001"+
		"\u0015\u0001\u0016\u0001\u0016\u0001\u0017\u0001\u0017\u0001\u0018\u0001"+
		"\u0018\u0001\u0019\u0001\u0019\u0001\u001a\u0001\u001a\u0001\u001b\u0001"+
		"\u001b\u0001\u001c\u0001\u001c\u0001\u001d\u0001\u001d\u0001\u001e\u0001"+
		"\u001e\u0001\u001f\u0001\u001f\u0001 \u0001 \u0001!\u0001!\u0001\"\u0001"+
		"\"\u0001#\u0001#\u0001$\u0001$\u0001%\u0001%\u0001&\u0001&\u0001&\u0001"+
		"\'\u0001\'\u0001\'\u0001(\u0001(\u0001(\u0001)\u0001)\u0001)\u0001*\u0001"+
		"*\u0001*\u0001+\u0001+\u0001+\u0001,\u0001,\u0005,\u010f\b,\n,\f,\u0112"+
		"\t,\u0001-\u0004-\u0115\b-\u000b-\f-\u0116\u0001.\u0001.\u0001.\u0001"+
		".\u0005.\u011d\b.\n.\f.\u0120\t.\u0001.\u0001.\u0001/\u0001/\u0001/\u0001"+
		"/\u0005/\u0128\b/\n/\f/\u012b\t/\u0001/\u0001/\u00010\u00010\u00010\u0001"+
		"0\u00050\u0133\b0\n0\f0\u0136\t0\u00010\u00010\u00010\u00010\u00010\u0001"+
		"1\u00041\u013e\b1\u000b1\f1\u013f\u00011\u00011\u00012\u00012\u00012\u0001"+
		"2\u00012\u00013\u00013\u00013\u00013\u00013\u00013\u00014\u00014\u0001"+
		"4\u00015\u00045\u0153\b5\u000b5\f5\u0154\u0001\u0134\u00006\u0002\u0001"+
		"\u0004\u0002\u0006\u0003\b\u0004\n\u0005\f\u0006\u000e\u0007\u0010\b\u0012"+
		"\t\u0014\n\u0016\u000b\u0018\f\u001a\r\u001c\u000e\u001e\u000f \u0010"+
		"\"\u0011$\u0012&\u0013(\u0014*\u0015,\u0016.\u00170\u00182\u00194\u001a"+
		"6\u001b8\u001c:\u001d<\u001e>\u001f@ B!D\"F#H$J%L&N\'P(R)T*V+X,Z-\\.^"+
		"/`0b1d2f\u0000h\u0000j3l4\u0002\u0000\u0001\u0007\u0003\u0000AZ__az\u0004"+
		"\u000009AZ__az\u0001\u000009\u0002\u0000\"\"\\\\\u0002\u0000\n\n\r\r\u0003"+
		"\u0000\t\n\r\r  \u0002\u0000{{}}\u015c\u0000\u0002\u0001\u0000\u0000\u0000"+
		"\u0000\u0004\u0001\u0000\u0000\u0000\u0000\u0006\u0001\u0000\u0000\u0000"+
		"\u0000\b\u0001\u0000\u0000\u0000\u0000\n\u0001\u0000\u0000\u0000\u0000"+
		"\f\u0001\u0000\u0000\u0000\u0000\u000e\u0001\u0000\u0000\u0000\u0000\u0010"+
		"\u0001\u0000\u0000\u0000\u0000\u0012\u0001\u0000\u0000\u0000\u0000\u0014"+
		"\u0001\u0000\u0000\u0000\u0000\u0016\u0001\u0000\u0000\u0000\u0000\u0018"+
		"\u0001\u0000\u0000\u0000\u0000\u001a\u0001\u0000\u0000\u0000\u0000\u001c"+
		"\u0001\u0000\u0000\u0000\u0000\u001e\u0001\u0000\u0000\u0000\u0000 \u0001"+
		"\u0000\u0000\u0000\u0000\"\u0001\u0000\u0000\u0000\u0000$\u0001\u0000"+
		"\u0000\u0000\u0000&\u0001\u0000\u0000\u0000\u0000(\u0001\u0000\u0000\u0000"+
		"\u0000*\u0001\u0000\u0000\u0000\u0000,\u0001\u0000\u0000\u0000\u0000."+
		"\u0001\u0000\u0000\u0000\u00000\u0001\u0000\u0000\u0000\u00002\u0001\u0000"+
		"\u0000\u0000\u00004\u0001\u0000\u0000\u0000\u00006\u0001\u0000\u0000\u0000"+
		"\u00008\u0001\u0000\u0000\u0000\u0000:\u0001\u0000\u0000\u0000\u0000<"+
		"\u0001\u0000\u0000\u0000\u0000>\u0001\u0000\u0000\u0000\u0000@\u0001\u0000"+
		"\u0000\u0000\u0000B\u0001\u0000\u0000\u0000\u0000D\u0001\u0000\u0000\u0000"+
		"\u0000F\u0001\u0000\u0000\u0000\u0000H\u0001\u0000\u0000\u0000\u0000J"+
		"\u0001\u0000\u0000\u0000\u0000L\u0001\u0000\u0000\u0000\u0000N\u0001\u0000"+
		"\u0000\u0000\u0000P\u0001\u0000\u0000\u0000\u0000R\u0001\u0000\u0000\u0000"+
		"\u0000T\u0001\u0000\u0000\u0000\u0000V\u0001\u0000\u0000\u0000\u0000X"+
		"\u0001\u0000\u0000\u0000\u0000Z\u0001\u0000\u0000\u0000\u0000\\\u0001"+
		"\u0000\u0000\u0000\u0000^\u0001\u0000\u0000\u0000\u0000`\u0001\u0000\u0000"+
		"\u0000\u0000b\u0001\u0000\u0000\u0000\u0000d\u0001\u0000\u0000\u0000\u0001"+
		"f\u0001\u0000\u0000\u0000\u0001h\u0001\u0000\u0000\u0000\u0001j\u0001"+
		"\u0000\u0000\u0000\u0001l\u0001\u0000\u0000\u0000\u0002n\u0001\u0000\u0000"+
		"\u0000\u0004v\u0001\u0000\u0000\u0000\u0006}\u0001\u0000\u0000\u0000\b"+
		"\u0083\u0001\u0000\u0000\u0000\n\u008c\u0001\u0000\u0000\u0000\f\u0093"+
		"\u0001\u0000\u0000\u0000\u000e\u009c\u0001\u0000\u0000\u0000\u0010\u009f"+
		"\u0001\u0000\u0000\u0000\u0012\u00a4\u0001\u0000\u0000\u0000\u0014\u00ab"+
		"\u0001\u0000\u0000\u0000\u0016\u00b0\u0001\u0000\u0000\u0000\u0018\u00b6"+
		"\u0001\u0000\u0000\u0000\u001a\u00bc\u0001\u0000\u0000\u0000\u001c\u00be"+
		"\u0001\u0000\u0000\u0000\u001e\u00c0\u0001\u0000\u0000\u0000 \u00c3\u0001"+
		"\u0000\u0000\u0000\"\u00c6\u0001\u0000\u0000\u0000$\u00ca\u0001\u0000"+
		"\u0000\u0000&\u00d0\u0001\u0000\u0000\u0000(\u00d3\u0001\u0000\u0000\u0000"+
		"*\u00d6\u0001\u0000\u0000\u0000,\u00d8\u0001\u0000\u0000\u0000.\u00da"+
		"\u0001\u0000\u0000\u00000\u00dc\u0001\u0000\u0000\u00002\u00de\u0001\u0000"+
		"\u0000\u00004\u00e0\u0001\u0000\u0000\u00006\u00e2\u0001\u0000\u0000\u0000"+
		"8\u00e4\u0001\u0000\u0000\u0000:\u00e6\u0001\u0000\u0000\u0000<\u00e8"+
		"\u0001\u0000\u0000\u0000>\u00ea\u0001\u0000\u0000\u0000@\u00ec\u0001\u0000"+
		"\u0000\u0000B\u00ee\u0001\u0000\u0000\u0000D\u00f0\u0001\u0000\u0000\u0000"+
		"F\u00f2\u0001\u0000\u0000\u0000H\u00f4\u0001\u0000\u0000\u0000J\u00f6"+
		"\u0001\u0000\u0000\u0000L\u00f8\u0001\u0000\u0000\u0000N\u00fa\u0001\u0000"+
		"\u0000\u0000P\u00fd\u0001\u0000\u0000\u0000R\u0100\u0001\u0000\u0000\u0000"+
		"T\u0103\u0001\u0000\u0000\u0000V\u0106\u0001\u0000\u0000\u0000X\u0109"+
		"\u0001\u0000\u0000\u0000Z\u010c\u0001\u0000\u0000\u0000\\\u0114\u0001"+
		"\u0000\u0000\u0000^\u0118\u0001\u0000\u0000\u0000`\u0123\u0001\u0000\u0000"+
		"\u0000b\u012e\u0001\u0000\u0000\u0000d\u013d\u0001\u0000\u0000\u0000f"+
		"\u0143\u0001\u0000\u0000\u0000h\u0148\u0001\u0000\u0000\u0000j\u014e\u0001"+
		"\u0000\u0000\u0000l\u0152\u0001\u0000\u0000\u0000no\u0005p\u0000\u0000"+
		"op\u0005a\u0000\u0000pq\u0005c\u0000\u0000qr\u0005k\u0000\u0000rs\u0005"+
		"a\u0000\u0000st\u0005g\u0000\u0000tu\u0005e\u0000\u0000u\u0003\u0001\u0000"+
		"\u0000\u0000vw\u0005i\u0000\u0000wx\u0005m\u0000\u0000xy\u0005p\u0000"+
		"\u0000yz\u0005o\u0000\u0000z{\u0005r\u0000\u0000{|\u0005t\u0000\u0000"+
		"|\u0005\u0001\u0000\u0000\u0000}~\u0005c\u0000\u0000~\u007f\u0005l\u0000"+
		"\u0000\u007f\u0080\u0005a\u0000\u0000\u0080\u0081\u0005s\u0000\u0000\u0081"+
		"\u0082\u0005s\u0000\u0000\u0082\u0007\u0001\u0000\u0000\u0000\u0083\u0084"+
		"\u0005n\u0000\u0000\u0084\u0085\u0005a\u0000\u0000\u0085\u0086\u0005t"+
		"\u0000\u0000\u0086\u0087\u0005i\u0000\u0000\u0087\u0088\u0005v\u0000\u0000"+
		"\u0088\u0089\u0005e\u0000\u0000\u0089\u008a\u0001\u0000\u0000\u0000\u008a"+
		"\u008b\u0006\u0003\u0000\u0000\u008b\t\u0001\u0000\u0000\u0000\u008c\u008d"+
		"\u0005s\u0000\u0000\u008d\u008e\u0005t\u0000\u0000\u008e\u008f\u0005a"+
		"\u0000\u0000\u008f\u0090\u0005t\u0000\u0000\u0090\u0091\u0005i\u0000\u0000"+
		"\u0091\u0092\u0005c\u0000\u0000\u0092\u000b\u0001\u0000\u0000\u0000\u0093"+
		"\u0094\u0005f\u0000\u0000\u0094\u0095\u0005u\u0000\u0000\u0095\u0096\u0005"+
		"n\u0000\u0000\u0096\u0097\u0005c\u0000\u0000\u0097\u0098\u0005t\u0000"+
		"\u0000\u0098\u0099\u0005i\u0000\u0000\u0099\u009a\u0005o\u0000\u0000\u009a"+
		"\u009b\u0005n\u0000\u0000\u009b\r\u0001\u0000\u0000\u0000\u009c\u009d"+
		"\u0005i\u0000\u0000\u009d\u009e\u0005f\u0000\u0000\u009e\u000f\u0001\u0000"+
		"\u0000\u0000\u009f\u00a0\u0005e\u0000\u0000\u00a0\u00a1\u0005l\u0000\u0000"+
		"\u00a1\u00a2\u0005s\u0000\u0000\u00a2\u00a3\u0005e\u0000\u0000\u00a3\u0011"+
		"\u0001\u0000\u0000\u0000\u00a4\u00a5\u0005r\u0000\u0000\u00a5\u00a6\u0005"+
		"e\u0000\u0000\u00a6\u00a7\u0005t\u0000\u0000\u00a7\u00a8\u0005u\u0000"+
		"\u0000\u00a8\u00a9\u0005r\u0000\u0000\u00a9\u00aa\u0005n\u0000\u0000\u00aa"+
		"\u0013\u0001\u0000\u0000\u0000\u00ab\u00ac\u0005t\u0000\u0000\u00ac\u00ad"+
		"\u0005r\u0000\u0000\u00ad\u00ae\u0005u\u0000\u0000\u00ae\u00af\u0005e"+
		"\u0000\u0000\u00af\u0015\u0001\u0000\u0000\u0000\u00b0\u00b1\u0005f\u0000"+
		"\u0000\u00b1\u00b2\u0005a\u0000\u0000\u00b2\u00b3\u0005l\u0000\u0000\u00b3"+
		"\u00b4\u0005s\u0000\u0000\u00b4\u00b5\u0005e\u0000\u0000\u00b5\u0017\u0001"+
		"\u0000\u0000\u0000\u00b6\u00b7\u0004\u000b\u0000\u0000\u00b7\u00b8\u0005"+
		"{\u0000\u0000\u00b8\u00b9\u0006\u000b\u0001\u0000\u00b9\u00ba\u0001\u0000"+
		"\u0000\u0000\u00ba\u00bb\u0006\u000b\u0002\u0000\u00bb\u0019\u0001\u0000"+
		"\u0000\u0000\u00bc\u00bd\u0005{\u0000\u0000\u00bd\u001b\u0001\u0000\u0000"+
		"\u0000\u00be\u00bf\u0005}\u0000\u0000\u00bf\u001d\u0001\u0000\u0000\u0000"+
		"\u00c0\u00c1\u0005(\u0000\u0000\u00c1\u00c2\u0006\u000e\u0003\u0000\u00c2"+
		"\u001f\u0001\u0000\u0000\u0000\u00c3\u00c4\u0005)\u0000\u0000\u00c4\u00c5"+
		"\u0006\u000f\u0004\u0000\u00c5!\u0001\u0000\u0000\u0000\u00c6\u00c7\u0005"+
		"f\u0000\u0000\u00c7\u00c8\u0005o\u0000\u0000\u00c8\u00c9\u0005r\u0000"+
		"\u0000\u00c9#\u0001\u0000\u0000\u0000\u00ca\u00cb\u0005w\u0000\u0000\u00cb"+
		"\u00cc\u0005h\u0000\u0000\u00cc\u00cd\u0005i\u0000\u0000\u00cd\u00ce\u0005"+
		"l\u0000\u0000\u00ce\u00cf\u0005e\u0000\u0000\u00cf%\u0001\u0000\u0000"+
		"\u0000\u00d0\u00d1\u0005+\u0000\u0000\u00d1\u00d2\u0005+\u0000\u0000\u00d2"+
		"\'\u0001\u0000\u0000\u0000\u00d3\u00d4\u0005-\u0000\u0000\u00d4\u00d5"+
		"\u0005-\u0000\u0000\u00d5)\u0001\u0000\u0000\u0000\u00d6\u00d7\u0005["+
		"\u0000\u0000\u00d7+\u0001\u0000\u0000\u0000\u00d8\u00d9\u0005]\u0000\u0000"+
		"\u00d9-\u0001\u0000\u0000\u0000\u00da\u00db\u0005.\u0000\u0000\u00db/"+
		"\u0001\u0000\u0000\u0000\u00dc\u00dd\u0005,\u0000\u0000\u00dd1\u0001\u0000"+
		"\u0000\u0000\u00de\u00df\u0005:\u0000\u0000\u00df3\u0001\u0000\u0000\u0000"+
		"\u00e0\u00e1\u0005;\u0000\u0000\u00e15\u0001\u0000\u0000\u0000\u00e2\u00e3"+
		"\u0005=\u0000\u0000\u00e37\u0001\u0000\u0000\u0000\u00e4\u00e5\u0005%"+
		"\u0000\u0000\u00e59\u0001\u0000\u0000\u0000\u00e6\u00e7\u0005$\u0000\u0000"+
		"\u00e7;\u0001\u0000\u0000\u0000\u00e8\u00e9\u0005@\u0000\u0000\u00e9="+
		"\u0001\u0000\u0000\u0000\u00ea\u00eb\u0005~\u0000\u0000\u00eb?\u0001\u0000"+
		"\u0000\u0000\u00ec\u00ed\u0005/\u0000\u0000\u00edA\u0001\u0000\u0000\u0000"+
		"\u00ee\u00ef\u0005-\u0000\u0000\u00efC\u0001\u0000\u0000\u0000\u00f0\u00f1"+
		"\u0005+\u0000\u0000\u00f1E\u0001\u0000\u0000\u0000\u00f2\u00f3\u0005*"+
		"\u0000\u0000\u00f3G\u0001\u0000\u0000\u0000\u00f4\u00f5\u0005<\u0000\u0000"+
		"\u00f5I\u0001\u0000\u0000\u0000\u00f6\u00f7\u0005>\u0000\u0000\u00f7K"+
		"\u0001\u0000\u0000\u0000\u00f8\u00f9\u0005!\u0000\u0000\u00f9M\u0001\u0000"+
		"\u0000\u0000\u00fa\u00fb\u0005|\u0000\u0000\u00fb\u00fc\u0005|\u0000\u0000"+
		"\u00fcO\u0001\u0000\u0000\u0000\u00fd\u00fe\u0005&\u0000\u0000\u00fe\u00ff"+
		"\u0005&\u0000\u0000\u00ffQ\u0001\u0000\u0000\u0000\u0100\u0101\u0005="+
		"\u0000\u0000\u0101\u0102\u0005=\u0000\u0000\u0102S\u0001\u0000\u0000\u0000"+
		"\u0103\u0104\u0005!\u0000\u0000\u0104\u0105\u0005=\u0000\u0000\u0105U"+
		"\u0001\u0000\u0000\u0000\u0106\u0107\u0005>\u0000\u0000\u0107\u0108\u0005"+
		"=\u0000\u0000\u0108W\u0001\u0000\u0000\u0000\u0109\u010a\u0005<\u0000"+
		"\u0000\u010a\u010b\u0005=\u0000\u0000\u010bY\u0001\u0000\u0000\u0000\u010c"+
		"\u0110\u0007\u0000\u0000\u0000\u010d\u010f\u0007\u0001\u0000\u0000\u010e"+
		"\u010d\u0001\u0000\u0000\u0000\u010f\u0112\u0001\u0000\u0000\u0000\u0110"+
		"\u010e\u0001\u0000\u0000\u0000\u0110\u0111\u0001\u0000\u0000\u0000\u0111"+
		"[\u0001\u0000\u0000\u0000\u0112\u0110\u0001\u0000\u0000\u0000\u0113\u0115"+
		"\u0007\u0002\u0000\u0000\u0114\u0113\u0001\u0000\u0000\u0000\u0115\u0116"+
		"\u0001\u0000\u0000\u0000\u0116\u0114\u0001\u0000\u0000\u0000\u0116\u0117"+
		"\u0001\u0000\u0000\u0000\u0117]\u0001\u0000\u0000\u0000\u0118\u011e\u0005"+
		"\"\u0000\u0000\u0119\u011d\b\u0003\u0000\u0000\u011a\u011b\u0005\\\u0000"+
		"\u0000\u011b\u011d\t\u0000\u0000\u0000\u011c\u0119\u0001\u0000\u0000\u0000"+
		"\u011c\u011a\u0001\u0000\u0000\u0000\u011d\u0120\u0001\u0000\u0000\u0000"+
		"\u011e\u011c\u0001\u0000\u0000\u0000\u011e\u011f\u0001\u0000\u0000\u0000"+
		"\u011f\u0121\u0001\u0000\u0000\u0000\u0120\u011e\u0001\u0000\u0000\u0000"+
		"\u0121\u0122\u0005\"\u0000\u0000\u0122_\u0001\u0000\u0000\u0000\u0123"+
		"\u0124\u0005/\u0000\u0000\u0124\u0125\u0005/\u0000\u0000\u0125\u0129\u0001"+
		"\u0000\u0000\u0000\u0126\u0128\b\u0004\u0000\u0000\u0127\u0126\u0001\u0000"+
		"\u0000\u0000\u0128\u012b\u0001\u0000\u0000\u0000\u0129\u0127\u0001\u0000"+
		"\u0000\u0000\u0129\u012a\u0001\u0000\u0000\u0000\u012a\u012c\u0001\u0000"+
		"\u0000\u0000\u012b\u0129\u0001\u0000\u0000\u0000\u012c\u012d\u0006/\u0005"+
		"\u0000\u012da\u0001\u0000\u0000\u0000\u012e\u012f\u0005/\u0000\u0000\u012f"+
		"\u0130\u0005*\u0000\u0000\u0130\u0134\u0001\u0000\u0000\u0000\u0131\u0133"+
		"\t\u0000\u0000\u0000\u0132\u0131\u0001\u0000\u0000\u0000\u0133\u0136\u0001"+
		"\u0000\u0000\u0000\u0134\u0135\u0001\u0000\u0000\u0000\u0134\u0132\u0001"+
		"\u0000\u0000\u0000\u0135\u0137\u0001\u0000\u0000\u0000\u0136\u0134\u0001"+
		"\u0000\u0000\u0000\u0137\u0138\u0005*\u0000\u0000\u0138\u0139\u0005/\u0000"+
		"\u0000\u0139\u013a\u0001\u0000\u0000\u0000\u013a\u013b\u00060\u0005\u0000"+
		"\u013bc\u0001\u0000\u0000\u0000\u013c\u013e\u0007\u0005\u0000\u0000\u013d"+
		"\u013c\u0001\u0000\u0000\u0000\u013e\u013f\u0001\u0000\u0000\u0000\u013f"+
		"\u013d\u0001\u0000\u0000\u0000\u013f\u0140\u0001\u0000\u0000\u0000\u0140"+
		"\u0141\u0001\u0000\u0000\u0000\u0141\u0142\u00061\u0005\u0000\u0142e\u0001"+
		"\u0000\u0000\u0000\u0143\u0144\u0005{\u0000\u0000\u0144\u0145\u00062\u0006"+
		"\u0000\u0145\u0146\u0001\u0000\u0000\u0000\u0146\u0147\u00062\u0007\u0000"+
		"\u0147g\u0001\u0000\u0000\u0000\u0148\u0149\u00043\u0001\u0000\u0149\u014a"+
		"\u0005}\u0000\u0000\u014a\u014b\u00063\b\u0000\u014b\u014c\u0001\u0000"+
		"\u0000\u0000\u014c\u014d\u00063\u0007\u0000\u014di\u0001\u0000\u0000\u0000"+
		"\u014e\u014f\u0005}\u0000\u0000\u014f\u0150\u00064\t\u0000\u0150k\u0001"+
		"\u0000\u0000\u0000\u0151\u0153\b\u0006\u0000\u0000\u0152\u0151\u0001\u0000"+
		"\u0000\u0000\u0153\u0154\u0001\u0000\u0000\u0000\u0154\u0152\u0001\u0000"+
		"\u0000\u0000\u0154\u0155\u0001\u0000\u0000\u0000\u0155m\u0001\u0000\u0000"+
		"\u0000\n\u0000\u0001\u0110\u0116\u011c\u011e\u0129\u0134\u013f\u0154\n"+
		"\u0001\u0003\u0000\u0001\u000b\u0001\u0005\u0001\u0000\u0001\u000e\u0002"+
		"\u0001\u000f\u0003\u0006\u0000\u0000\u00012\u0004\u00074\u0000\u00013"+
		"\u0005\u00014\u0006";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}