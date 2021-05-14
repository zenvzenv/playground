// Generated from C:/Users/zw305/Desktop/MyWorkspace/playground/antlr/src/main/java/zhengwei/antlr/g4\UserViolationRule.g4 by ANTLR 4.9.1
package zhengwei.antlr.userviolation;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class UserViolationRuleLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		WINDOWTIME=1, ALLDAY=2, INT=3, FLOAT=4, UNIT=5, DO=6, SOMETHING=7, RANGETIME=8, 
		SINGLE=9, WHO=10, WS=11;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"WINDOWTIME", "ALLDAY", "INT", "FLOAT", "UNIT", "DO", "SOMETHING", "RANGETIME", 
			"SINGLE", "WHO", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'windowTime'", "'allDay'", null, null, null, null, null, "'range'", 
			"'single'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "WINDOWTIME", "ALLDAY", "INT", "FLOAT", "UNIT", "DO", "SOMETHING", 
			"RANGETIME", "SINGLE", "WHO", "WS"
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


	public UserViolationRuleLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "UserViolationRule.g4"; }

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

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\r\u0088\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3"+
		"\3\3\3\3\3\3\3\3\3\4\6\4-\n\4\r\4\16\4.\3\5\6\5\62\n\5\r\5\16\5\63\3\5"+
		"\3\5\7\58\n\5\f\5\16\5;\13\5\3\5\3\5\6\5?\n\5\r\5\16\5@\5\5C\n\5\3\6\3"+
		"\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6"+
		"\3\6\3\6\3\6\3\6\5\6\\\n\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\5\7g\n"+
		"\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\5\bq\n\b\3\t\3\t\3\t\3\t\3\t\3\t\3"+
		"\n\3\n\3\n\3\n\3\n\3\n\3\n\3\13\6\13\u0081\n\13\r\13\16\13\u0082\3\f\3"+
		"\f\3\f\3\f\2\2\r\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\3"+
		"\2\5\3\2\62;\5\2\62;C\\c|\5\2\13\f\17\17\"\"\2\u0092\2\3\3\2\2\2\2\5\3"+
		"\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2"+
		"\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\3\31\3\2\2\2\5$\3\2"+
		"\2\2\7,\3\2\2\2\tB\3\2\2\2\13[\3\2\2\2\rf\3\2\2\2\17p\3\2\2\2\21r\3\2"+
		"\2\2\23x\3\2\2\2\25\u0080\3\2\2\2\27\u0084\3\2\2\2\31\32\7y\2\2\32\33"+
		"\7k\2\2\33\34\7p\2\2\34\35\7f\2\2\35\36\7q\2\2\36\37\7y\2\2\37 \7V\2\2"+
		" !\7k\2\2!\"\7o\2\2\"#\7g\2\2#\4\3\2\2\2$%\7c\2\2%&\7n\2\2&\'\7n\2\2\'"+
		"(\7F\2\2()\7c\2\2)*\7{\2\2*\6\3\2\2\2+-\t\2\2\2,+\3\2\2\2-.\3\2\2\2.,"+
		"\3\2\2\2./\3\2\2\2/\b\3\2\2\2\60\62\5\7\4\2\61\60\3\2\2\2\62\63\3\2\2"+
		"\2\63\61\3\2\2\2\63\64\3\2\2\2\64\65\3\2\2\2\659\7\60\2\2\668\5\7\4\2"+
		"\67\66\3\2\2\28;\3\2\2\29\67\3\2\2\29:\3\2\2\2:C\3\2\2\2;9\3\2\2\2<>\7"+
		"\60\2\2=?\5\7\4\2>=\3\2\2\2?@\3\2\2\2@>\3\2\2\2@A\3\2\2\2AC\3\2\2\2B\61"+
		"\3\2\2\2B<\3\2\2\2C\n\3\2\2\2DE\7F\2\2EF\7C\2\2FG\7[\2\2G\\\7U\2\2HI\7"+
		"J\2\2IJ\7Q\2\2JK\7W\2\2KL\7T\2\2L\\\7U\2\2MN\7O\2\2NO\7K\2\2OP\7P\2\2"+
		"PQ\7W\2\2QR\7V\2\2RS\7G\2\2S\\\7U\2\2TU\7U\2\2UV\7G\2\2VW\7E\2\2WX\7Q"+
		"\2\2XY\7P\2\2YZ\7F\2\2Z\\\7U\2\2[D\3\2\2\2[H\3\2\2\2[M\3\2\2\2[T\3\2\2"+
		"\2\\\f\3\2\2\2]^\7n\2\2^_\7q\2\2_`\7i\2\2`a\7k\2\2ag\7p\2\2bc\7d\2\2c"+
		"d\7w\2\2de\7t\2\2eg\7p\2\2f]\3\2\2\2fb\3\2\2\2g\16\3\2\2\2hi\7u\2\2ij"+
		"\7{\2\2jk\7u\2\2kl\7v\2\2lm\7g\2\2mq\7o\2\2no\7E\2\2oq\7F\2\2ph\3\2\2"+
		"\2pn\3\2\2\2q\20\3\2\2\2rs\7t\2\2st\7c\2\2tu\7p\2\2uv\7i\2\2vw\7g\2\2"+
		"w\22\3\2\2\2xy\7u\2\2yz\7k\2\2z{\7p\2\2{|\7i\2\2|}\7n\2\2}~\7g\2\2~\24"+
		"\3\2\2\2\177\u0081\t\3\2\2\u0080\177\3\2\2\2\u0081\u0082\3\2\2\2\u0082"+
		"\u0080\3\2\2\2\u0082\u0083\3\2\2\2\u0083\26\3\2\2\2\u0084\u0085\t\4\2"+
		"\2\u0085\u0086\3\2\2\2\u0086\u0087\b\f\2\2\u0087\30\3\2\2\2\f\2.\639@"+
		"B[fp\u0082\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}