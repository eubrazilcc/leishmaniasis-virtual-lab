// Generated from eu/eubrazilcc/lvl/storage/antlr4/ql/LvLQLLexer.g4 by ANTLR 4.5
package eu.eubrazilcc.lvl.storage.antlr4.ql;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class LvLQLLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		SEQUENCE_SIMILARITY_SEARCH=1, NEAR_SEARCH=2, FIELD_SEP=3, COMMA=4, OPEN_BRACK=5, 
		CLOSE_BRACK=6, OPEN_PAREN=7, CLOSE_PAREN=8, EQUALS=9, NEQUALS=10, GTEQUALS=11, 
		LTEQUALS=12, GT=13, LT=14, AND=15, NOT=16, OR=17, NUMERIC=18, FLOAT=19, 
		INT=20, DOUBLE_QUOT=21, OPEN_STRING=22, STRING=23, SPACES=24, CLOSE_STRING=25, 
		S_STRING=26;
	public static final int STRING_MODE = 1;
	public static String[] modeNames = {
		"DEFAULT_MODE", "STRING_MODE"
	};

	public static final String[] ruleNames = {
		"SEQUENCE_SIMILARITY_SEARCH", "NEAR_SEARCH", "FIELD_SEP", "COMMA", "OPEN_BRACK", 
		"CLOSE_BRACK", "OPEN_PAREN", "CLOSE_PAREN", "EQUALS", "NEQUALS", "GTEQUALS", 
		"LTEQUALS", "GT", "LT", "AND", "NOT", "OR", "NUMERIC", "FLOAT", "INT", 
		"DOUBLE_QUOT", "OPEN_STRING", "STRING", "WORD", "EXP", "DIGIT", "LETTER", 
		"SPACE", "SPACES", "A", "N", "D", "O", "R", "T", "E", "S", "I", "M", "Q", 
		"CLOSE_STRING", "S_STRING"
	};

	private static final String[] _LITERAL_NAMES = {
		null, null, null, "':'", "','", "'['", "']'", "'('", "')'", "'='", "'!='", 
		"'>='", "'<='", "'>'", "'<'", null, null, null, null, null, null, "'\"'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "SEQUENCE_SIMILARITY_SEARCH", "NEAR_SEARCH", "FIELD_SEP", "COMMA", 
		"OPEN_BRACK", "CLOSE_BRACK", "OPEN_PAREN", "CLOSE_PAREN", "EQUALS", "NEQUALS", 
		"GTEQUALS", "LTEQUALS", "GT", "LT", "AND", "NOT", "OR", "NUMERIC", "FLOAT", 
		"INT", "DOUBLE_QUOT", "OPEN_STRING", "STRING", "SPACES", "CLOSE_STRING", 
		"S_STRING"
	};
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


	public LvLQLLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "LvLQLLexer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\34\u0101\b\1\b\1"+
		"\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t"+
		"\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4"+
		"\22\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4"+
		"\31\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4"+
		" \t \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4"+
		"+\t+\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\5\3\5\3"+
		"\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\13\3\f\3\f\3\f\3\r"+
		"\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3"+
		"\22\3\22\3\22\3\23\3\23\5\23\u008d\n\23\3\24\6\24\u0090\n\24\r\24\16\24"+
		"\u0091\3\24\3\24\7\24\u0096\n\24\f\24\16\24\u0099\13\24\3\24\5\24\u009c"+
		"\n\24\3\24\6\24\u009f\n\24\r\24\16\24\u00a0\3\24\5\24\u00a4\n\24\3\24"+
		"\3\24\6\24\u00a8\n\24\r\24\16\24\u00a9\3\24\5\24\u00ad\n\24\5\24\u00af"+
		"\n\24\3\25\6\25\u00b2\n\25\r\25\16\25\u00b3\3\26\3\26\3\27\3\27\3\27\3"+
		"\27\3\30\3\30\3\30\3\30\3\30\5\30\u00c1\n\30\3\31\3\31\3\31\6\31\u00c6"+
		"\n\31\r\31\16\31\u00c7\3\32\3\32\5\32\u00cc\n\32\3\32\6\32\u00cf\n\32"+
		"\r\32\16\32\u00d0\3\33\3\33\3\34\3\34\3\35\3\35\3\36\6\36\u00da\n\36\r"+
		"\36\16\36\u00db\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3#\3$\3$\3"+
		"%\3%\3&\3&\3\'\3\'\3(\3(\3)\3)\3*\3*\3*\3*\3+\7+\u00fb\n+\f+\16+\u00fe"+
		"\13+\3+\3+\3\u00fc\2,\4\3\6\4\b\5\n\6\f\7\16\b\20\t\22\n\24\13\26\f\30"+
		"\r\32\16\34\17\36\20 \21\"\22$\23&\24(\25*\26,\27.\30\60\31\62\2\64\2"+
		"\66\28\2:\2<\32>\2@\2B\2D\2F\2H\2J\2L\2N\2P\2R\2T\33V\34\4\2\3\24\4\2"+
		"//aa\4\2--//\3\2\62;\4\2C\\c|\5\2\13\13\r\r\"\"\5\2\13\r\17\17\"\"\4\2"+
		"CCcc\4\2PPpp\4\2FFff\4\2QQqq\4\2TTtt\4\2VVvv\4\2GGgg\4\2UUuu\4\2KKkk\4"+
		"\2OOoo\4\2SSss\3\2$$\u0102\2\4\3\2\2\2\2\6\3\2\2\2\2\b\3\2\2\2\2\n\3\2"+
		"\2\2\2\f\3\2\2\2\2\16\3\2\2\2\2\20\3\2\2\2\2\22\3\2\2\2\2\24\3\2\2\2\2"+
		"\26\3\2\2\2\2\30\3\2\2\2\2\32\3\2\2\2\2\34\3\2\2\2\2\36\3\2\2\2\2 \3\2"+
		"\2\2\2\"\3\2\2\2\2$\3\2\2\2\2&\3\2\2\2\2(\3\2\2\2\2*\3\2\2\2\2,\3\2\2"+
		"\2\2.\3\2\2\2\2\60\3\2\2\2\2<\3\2\2\2\3T\3\2\2\2\3V\3\2\2\2\4X\3\2\2\2"+
		"\6_\3\2\2\2\bd\3\2\2\2\nf\3\2\2\2\fh\3\2\2\2\16j\3\2\2\2\20l\3\2\2\2\22"+
		"n\3\2\2\2\24p\3\2\2\2\26r\3\2\2\2\30u\3\2\2\2\32x\3\2\2\2\34{\3\2\2\2"+
		"\36}\3\2\2\2 \177\3\2\2\2\"\u0083\3\2\2\2$\u0087\3\2\2\2&\u008c\3\2\2"+
		"\2(\u00ae\3\2\2\2*\u00b1\3\2\2\2,\u00b5\3\2\2\2.\u00b7\3\2\2\2\60\u00c0"+
		"\3\2\2\2\62\u00c5\3\2\2\2\64\u00c9\3\2\2\2\66\u00d2\3\2\2\28\u00d4\3\2"+
		"\2\2:\u00d6\3\2\2\2<\u00d9\3\2\2\2>\u00df\3\2\2\2@\u00e1\3\2\2\2B\u00e3"+
		"\3\2\2\2D\u00e5\3\2\2\2F\u00e7\3\2\2\2H\u00e9\3\2\2\2J\u00eb\3\2\2\2L"+
		"\u00ed\3\2\2\2N\u00ef\3\2\2\2P\u00f1\3\2\2\2R\u00f3\3\2\2\2T\u00f5\3\2"+
		"\2\2V\u00fc\3\2\2\2XY\5L&\2YZ\5N\'\2Z[\5P(\2[\\\5L&\2\\]\5J%\2]^\5R)\2"+
		"^\5\3\2\2\2_`\5@ \2`a\5J%\2ab\5>\37\2bc\5F#\2c\7\3\2\2\2de\7<\2\2e\t\3"+
		"\2\2\2fg\7.\2\2g\13\3\2\2\2hi\7]\2\2i\r\3\2\2\2jk\7_\2\2k\17\3\2\2\2l"+
		"m\7*\2\2m\21\3\2\2\2no\7+\2\2o\23\3\2\2\2pq\7?\2\2q\25\3\2\2\2rs\7#\2"+
		"\2st\7?\2\2t\27\3\2\2\2uv\7@\2\2vw\7?\2\2w\31\3\2\2\2xy\7>\2\2yz\7?\2"+
		"\2z\33\3\2\2\2{|\7@\2\2|\35\3\2\2\2}~\7>\2\2~\37\3\2\2\2\177\u0080\5>"+
		"\37\2\u0080\u0081\5@ \2\u0081\u0082\5B!\2\u0082!\3\2\2\2\u0083\u0084\5"+
		"@ \2\u0084\u0085\5D\"\2\u0085\u0086\5H$\2\u0086#\3\2\2\2\u0087\u0088\5"+
		"D\"\2\u0088\u0089\5F#\2\u0089%\3\2\2\2\u008a\u008d\5(\24\2\u008b\u008d"+
		"\5*\25\2\u008c\u008a\3\2\2\2\u008c\u008b\3\2\2\2\u008d\'\3\2\2\2\u008e"+
		"\u0090\5\66\33\2\u008f\u008e\3\2\2\2\u0090\u0091\3\2\2\2\u0091\u008f\3"+
		"\2\2\2\u0091\u0092\3\2\2\2\u0092\u0093\3\2\2\2\u0093\u0097\7\60\2\2\u0094"+
		"\u0096\5\66\33\2\u0095\u0094\3\2\2\2\u0096\u0099\3\2\2\2\u0097\u0095\3"+
		"\2\2\2\u0097\u0098\3\2\2\2\u0098\u009b\3\2\2\2\u0099\u0097\3\2\2\2\u009a"+
		"\u009c\5\64\32\2\u009b\u009a\3\2\2\2\u009b\u009c\3\2\2\2\u009c\u00af\3"+
		"\2\2\2\u009d\u009f\5\66\33\2\u009e\u009d\3\2\2\2\u009f\u00a0\3\2\2\2\u00a0"+
		"\u009e\3\2\2\2\u00a0\u00a1\3\2\2\2\u00a1\u00a3\3\2\2\2\u00a2\u00a4\5\64"+
		"\32\2\u00a3\u00a2\3\2\2\2\u00a3\u00a4\3\2\2\2\u00a4\u00af\3\2\2\2\u00a5"+
		"\u00a7\7\60\2\2\u00a6\u00a8\5\66\33\2\u00a7\u00a6\3\2\2\2\u00a8\u00a9"+
		"\3\2\2\2\u00a9\u00a7\3\2\2\2\u00a9\u00aa\3\2\2\2\u00aa\u00ac\3\2\2\2\u00ab"+
		"\u00ad\5\64\32\2\u00ac\u00ab\3\2\2\2\u00ac\u00ad\3\2\2\2\u00ad\u00af\3"+
		"\2\2\2\u00ae\u008f\3\2\2\2\u00ae\u009e\3\2\2\2\u00ae\u00a5\3\2\2\2\u00af"+
		")\3\2\2\2\u00b0\u00b2\5\66\33\2\u00b1\u00b0\3\2\2\2\u00b2\u00b3\3\2\2"+
		"\2\u00b3\u00b1\3\2\2\2\u00b3\u00b4\3\2\2\2\u00b4+\3\2\2\2\u00b5\u00b6"+
		"\7$\2\2\u00b6-\3\2\2\2\u00b7\u00b8\5,\26\2\u00b8\u00b9\3\2\2\2\u00b9\u00ba"+
		"\b\27\2\2\u00ba/\3\2\2\2\u00bb\u00c1\5\62\31\2\u00bc\u00bd\5,\26\2\u00bd"+
		"\u00be\5V+\2\u00be\u00bf\5,\26\2\u00bf\u00c1\3\2\2\2\u00c0\u00bb\3\2\2"+
		"\2\u00c0\u00bc\3\2\2\2\u00c1\61\3\2\2\2\u00c2\u00c6\58\34\2\u00c3\u00c6"+
		"\5\66\33\2\u00c4\u00c6\t\2\2\2\u00c5\u00c2\3\2\2\2\u00c5\u00c3\3\2\2\2"+
		"\u00c5\u00c4\3\2\2\2\u00c6\u00c7\3\2\2\2\u00c7\u00c5\3\2\2\2\u00c7\u00c8"+
		"\3\2\2\2\u00c8\63\3\2\2\2\u00c9\u00cb\5J%\2\u00ca\u00cc\t\3\2\2\u00cb"+
		"\u00ca\3\2\2\2\u00cb\u00cc\3\2\2\2\u00cc\u00ce\3\2\2\2\u00cd\u00cf\5\66"+
		"\33\2\u00ce\u00cd\3\2\2\2\u00cf\u00d0\3\2\2\2\u00d0\u00ce\3\2\2\2\u00d0"+
		"\u00d1\3\2\2\2\u00d1\65\3\2\2\2\u00d2\u00d3\t\4\2\2\u00d3\67\3\2\2\2\u00d4"+
		"\u00d5\t\5\2\2\u00d59\3\2\2\2\u00d6\u00d7\t\6\2\2\u00d7;\3\2\2\2\u00d8"+
		"\u00da\t\7\2\2\u00d9\u00d8\3\2\2\2\u00da\u00db\3\2\2\2\u00db\u00d9\3\2"+
		"\2\2\u00db\u00dc\3\2\2\2\u00dc\u00dd\3\2\2\2\u00dd\u00de\b\36\3\2\u00de"+
		"=\3\2\2\2\u00df\u00e0\t\b\2\2\u00e0?\3\2\2\2\u00e1\u00e2\t\t\2\2\u00e2"+
		"A\3\2\2\2\u00e3\u00e4\t\n\2\2\u00e4C\3\2\2\2\u00e5\u00e6\t\13\2\2\u00e6"+
		"E\3\2\2\2\u00e7\u00e8\t\f\2\2\u00e8G\3\2\2\2\u00e9\u00ea\t\r\2\2\u00ea"+
		"I\3\2\2\2\u00eb\u00ec\t\16\2\2\u00ecK\3\2\2\2\u00ed\u00ee\t\17\2\2\u00ee"+
		"M\3\2\2\2\u00ef\u00f0\t\20\2\2\u00f0O\3\2\2\2\u00f1\u00f2\t\21\2\2\u00f2"+
		"Q\3\2\2\2\u00f3\u00f4\t\22\2\2\u00f4S\3\2\2\2\u00f5\u00f6\5,\26\2\u00f6"+
		"\u00f7\3\2\2\2\u00f7\u00f8\b*\4\2\u00f8U\3\2\2\2\u00f9\u00fb\13\2\2\2"+
		"\u00fa\u00f9\3\2\2\2\u00fb\u00fe\3\2\2\2\u00fc\u00fd\3\2\2\2\u00fc\u00fa"+
		"\3\2\2\2\u00fd\u00ff\3\2\2\2\u00fe\u00fc\3\2\2\2\u00ff\u0100\n\23\2\2"+
		"\u0100W\3\2\2\2\25\2\3\u008c\u0091\u0097\u009b\u00a0\u00a3\u00a9\u00ac"+
		"\u00ae\u00b3\u00c0\u00c5\u00c7\u00cb\u00d0\u00db\u00fc\5\7\3\2\b\2\2\6"+
		"\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}