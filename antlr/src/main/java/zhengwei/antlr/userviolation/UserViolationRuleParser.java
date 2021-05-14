// Generated from C:/Users/zw305/Desktop/MyWorkspace/playground/antlr/src/main/java/zhengwei/antlr/g4\UserViolationRule.g4 by ANTLR 4.9.1
package zhengwei.antlr.userviolation;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class UserViolationRuleParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		WINDOWTIME=1, ALLDAY=2, INT=3, FLOAT=4, UNIT=5, DO=6, SOMETHING=7, RANGETIME=8, 
		SINGLE=9, WHO=10, WS=11;
	public static final int
		RULE_rule = 0, RULE_who = 1, RULE_when = 2, RULE_what = 3, RULE_frequency = 4;
	private static String[] makeRuleNames() {
		return new String[] {
			"rule", "who", "when", "what", "frequency"
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

	@Override
	public String getGrammarFileName() { return "UserViolationRule.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public UserViolationRuleParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class RuleContext extends ParserRuleContext {
		public WhoContext who() {
			return getRuleContext(WhoContext.class,0);
		}
		public WhenContext when() {
			return getRuleContext(WhenContext.class,0);
		}
		public WhatContext what() {
			return getRuleContext(WhatContext.class,0);
		}
		public FrequencyContext frequency() {
			return getRuleContext(FrequencyContext.class,0);
		}
		public RuleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rule; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof UserViolationRuleListener ) ((UserViolationRuleListener)listener).enterRule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof UserViolationRuleListener ) ((UserViolationRuleListener)listener).exitRule(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof UserViolationRuleVisitor ) return ((UserViolationRuleVisitor<? extends T>)visitor).visitRule(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RuleContext rule() throws RecognitionException {
		RuleContext _localctx = new RuleContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_rule);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(10);
			who();
			setState(11);
			when();
			setState(12);
			what();
			setState(13);
			frequency();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WhoContext extends ParserRuleContext {
		public TerminalNode WHO() { return getToken(UserViolationRuleParser.WHO, 0); }
		public WhoContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_who; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof UserViolationRuleListener ) ((UserViolationRuleListener)listener).enterWho(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof UserViolationRuleListener ) ((UserViolationRuleListener)listener).exitWho(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof UserViolationRuleVisitor ) return ((UserViolationRuleVisitor<? extends T>)visitor).visitWho(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhoContext who() throws RecognitionException {
		WhoContext _localctx = new WhoContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_who);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(15);
			match(WHO);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WhenContext extends ParserRuleContext {
		public WhenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_when; }
	 
		public WhenContext() { }
		public void copyFrom(WhenContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class SingleContext extends WhenContext {
		public TerminalNode SINGLE() { return getToken(UserViolationRuleParser.SINGLE, 0); }
		public SingleContext(WhenContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof UserViolationRuleListener ) ((UserViolationRuleListener)listener).enterSingle(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof UserViolationRuleListener ) ((UserViolationRuleListener)listener).exitSingle(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof UserViolationRuleVisitor ) return ((UserViolationRuleVisitor<? extends T>)visitor).visitSingle(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AllDayContext extends WhenContext {
		public TerminalNode ALLDAY() { return getToken(UserViolationRuleParser.ALLDAY, 0); }
		public AllDayContext(WhenContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof UserViolationRuleListener ) ((UserViolationRuleListener)listener).enterAllDay(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof UserViolationRuleListener ) ((UserViolationRuleListener)listener).exitAllDay(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof UserViolationRuleVisitor ) return ((UserViolationRuleVisitor<? extends T>)visitor).visitAllDay(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RangeTimeContext extends WhenContext {
		public TerminalNode RANGETIME() { return getToken(UserViolationRuleParser.RANGETIME, 0); }
		public List<TerminalNode> INT() { return getTokens(UserViolationRuleParser.INT); }
		public TerminalNode INT(int i) {
			return getToken(UserViolationRuleParser.INT, i);
		}
		public RangeTimeContext(WhenContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof UserViolationRuleListener ) ((UserViolationRuleListener)listener).enterRangeTime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof UserViolationRuleListener ) ((UserViolationRuleListener)listener).exitRangeTime(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof UserViolationRuleVisitor ) return ((UserViolationRuleVisitor<? extends T>)visitor).visitRangeTime(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class WindowTimeContext extends WhenContext {
		public TerminalNode WINDOWTIME() { return getToken(UserViolationRuleParser.WINDOWTIME, 0); }
		public TerminalNode INT() { return getToken(UserViolationRuleParser.INT, 0); }
		public TerminalNode UNIT() { return getToken(UserViolationRuleParser.UNIT, 0); }
		public WindowTimeContext(WhenContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof UserViolationRuleListener ) ((UserViolationRuleListener)listener).enterWindowTime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof UserViolationRuleListener ) ((UserViolationRuleListener)listener).exitWindowTime(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof UserViolationRuleVisitor ) return ((UserViolationRuleVisitor<? extends T>)visitor).visitWindowTime(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhenContext when() throws RecognitionException {
		WhenContext _localctx = new WhenContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_when);
		try {
			setState(25);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case RANGETIME:
				_localctx = new RangeTimeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(17);
				match(RANGETIME);
				setState(18);
				match(INT);
				setState(19);
				match(INT);
				}
				break;
			case WINDOWTIME:
				_localctx = new WindowTimeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(20);
				match(WINDOWTIME);
				setState(21);
				match(INT);
				setState(22);
				match(UNIT);
				}
				break;
			case SINGLE:
				_localctx = new SingleContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(23);
				match(SINGLE);
				}
				break;
			case ALLDAY:
				_localctx = new AllDayContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(24);
				match(ALLDAY);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WhatContext extends ParserRuleContext {
		public TerminalNode DO() { return getToken(UserViolationRuleParser.DO, 0); }
		public TerminalNode SOMETHING() { return getToken(UserViolationRuleParser.SOMETHING, 0); }
		public WhatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_what; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof UserViolationRuleListener ) ((UserViolationRuleListener)listener).enterWhat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof UserViolationRuleListener ) ((UserViolationRuleListener)listener).exitWhat(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof UserViolationRuleVisitor ) return ((UserViolationRuleVisitor<? extends T>)visitor).visitWhat(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhatContext what() throws RecognitionException {
		WhatContext _localctx = new WhatContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_what);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(27);
			match(DO);
			setState(28);
			match(SOMETHING);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FrequencyContext extends ParserRuleContext {
		public TerminalNode INT() { return getToken(UserViolationRuleParser.INT, 0); }
		public FrequencyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frequency; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof UserViolationRuleListener ) ((UserViolationRuleListener)listener).enterFrequency(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof UserViolationRuleListener ) ((UserViolationRuleListener)listener).exitFrequency(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof UserViolationRuleVisitor ) return ((UserViolationRuleVisitor<? extends T>)visitor).visitFrequency(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FrequencyContext frequency() throws RecognitionException {
		FrequencyContext _localctx = new FrequencyContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_frequency);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(30);
			match(INT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\r#\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\4\3\4\3\4\3\4"+
		"\3\4\3\4\3\4\3\4\5\4\34\n\4\3\5\3\5\3\5\3\6\3\6\3\6\2\2\7\2\4\6\b\n\2"+
		"\2\2 \2\f\3\2\2\2\4\21\3\2\2\2\6\33\3\2\2\2\b\35\3\2\2\2\n \3\2\2\2\f"+
		"\r\5\4\3\2\r\16\5\6\4\2\16\17\5\b\5\2\17\20\5\n\6\2\20\3\3\2\2\2\21\22"+
		"\7\f\2\2\22\5\3\2\2\2\23\24\7\n\2\2\24\25\7\5\2\2\25\34\7\5\2\2\26\27"+
		"\7\3\2\2\27\30\7\5\2\2\30\34\7\7\2\2\31\34\7\13\2\2\32\34\7\4\2\2\33\23"+
		"\3\2\2\2\33\26\3\2\2\2\33\31\3\2\2\2\33\32\3\2\2\2\34\7\3\2\2\2\35\36"+
		"\7\b\2\2\36\37\7\t\2\2\37\t\3\2\2\2 !\7\5\2\2!\13\3\2\2\2\3\33";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}