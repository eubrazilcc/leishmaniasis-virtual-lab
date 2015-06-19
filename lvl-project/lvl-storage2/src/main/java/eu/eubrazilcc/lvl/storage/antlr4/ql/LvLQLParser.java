// Generated from eu/eubrazilcc/lvl/storage/antlr4/ql/LvLQLParser.g4 by ANTLR 4.5
package eu.eubrazilcc.lvl.storage.antlr4.ql;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class LvLQLParser extends Parser {
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
	public static final int
		RULE_query = 0, RULE_atomic_query = 1, RULE_comparison_operator = 2, RULE_expression = 3, 
		RULE_atomic_expression = 4, RULE_coordinates = 5, RULE_latitude = 6, RULE_longitude = 7, 
		RULE_term = 8, RULE_field = 9;
	public static final String[] ruleNames = {
		"query", "atomic_query", "comparison_operator", "expression", "atomic_expression", 
		"coordinates", "latitude", "longitude", "term", "field"
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

	@Override
	public String getGrammarFileName() { return "LvLQLParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public LvLQLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class QueryContext extends ParserRuleContext {
		public QueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_query; }
	 
		public QueryContext() { }
		public void copyFrom(QueryContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class NegationQueryContext extends QueryContext {
		public TerminalNode NOT() { return getToken(LvLQLParser.NOT, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public NegationQueryContext(QueryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterNegationQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitNegationQuery(this);
		}
	}
	public static class RelationQueryContext extends QueryContext {
		public List<QueryContext> query() {
			return getRuleContexts(QueryContext.class);
		}
		public QueryContext query(int i) {
			return getRuleContext(QueryContext.class,i);
		}
		public TerminalNode AND() { return getToken(LvLQLParser.AND, 0); }
		public TerminalNode OR() { return getToken(LvLQLParser.OR, 0); }
		public RelationQueryContext(QueryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterRelationQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitRelationQuery(this);
		}
	}
	public static class SimpleQueryContext extends QueryContext {
		public Atomic_queryContext atomic_query() {
			return getRuleContext(Atomic_queryContext.class,0);
		}
		public SimpleQueryContext(QueryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterSimpleQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitSimpleQuery(this);
		}
	}
	public static class EmbeddedQueryContext extends QueryContext {
		public TerminalNode OPEN_PAREN() { return getToken(LvLQLParser.OPEN_PAREN, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode CLOSE_PAREN() { return getToken(LvLQLParser.CLOSE_PAREN, 0); }
		public EmbeddedQueryContext(QueryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterEmbeddedQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitEmbeddedQuery(this);
		}
	}

	public final QueryContext query() throws RecognitionException {
		return query(0);
	}

	private QueryContext query(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		QueryContext _localctx = new QueryContext(_ctx, _parentState);
		QueryContext _prevctx = _localctx;
		int _startState = 0;
		enterRecursionRule(_localctx, 0, RULE_query, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(28);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				_localctx = new NegationQueryContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(21);
				match(NOT);
				setState(22);
				query(2);
				}
				break;
			case 2:
				{
				_localctx = new EmbeddedQueryContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(23);
				match(OPEN_PAREN);
				setState(24);
				query(0);
				setState(25);
				match(CLOSE_PAREN);
				}
				break;
			case 3:
				{
				_localctx = new SimpleQueryContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(27);
				atomic_query();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(35);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new RelationQueryContext(new QueryContext(_parentctx, _parentState));
					pushNewRecursionContext(_localctx, _startState, RULE_query);
					setState(30);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(31);
					_la = _input.LA(1);
					if ( !(_la==AND || _la==OR) ) {
					_errHandler.recoverInline(this);
					} else {
						consume();
					}
					setState(32);
					query(2);
					}
					} 
				}
				setState(37);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class Atomic_queryContext extends ParserRuleContext {
		public Atomic_queryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atomic_query; }
	 
		public Atomic_queryContext() { }
		public void copyFrom(Atomic_queryContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ComparisonSimpleQueryContext extends Atomic_queryContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public Comparison_operatorContext comparison_operator() {
			return getRuleContext(Comparison_operatorContext.class,0);
		}
		public ComparisonSimpleQueryContext(Atomic_queryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterComparisonSimpleQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitComparisonSimpleQuery(this);
		}
	}
	public static class TermSimpleQueryContext extends Atomic_queryContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public TermSimpleQueryContext(Atomic_queryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterTermSimpleQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitTermSimpleQuery(this);
		}
	}

	public final Atomic_queryContext atomic_query() throws RecognitionException {
		Atomic_queryContext _localctx = new Atomic_queryContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_atomic_query);
		try {
			setState(43);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				_localctx = new ComparisonSimpleQueryContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(38);
				expression();
				setState(39);
				comparison_operator();
				setState(40);
				expression();
				}
				break;
			case 2:
				_localctx = new TermSimpleQueryContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(42);
				term();
				}
				break;
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

	public static class Comparison_operatorContext extends ParserRuleContext {
		public Comparison_operatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparison_operator; }
	 
		public Comparison_operatorContext() { }
		public void copyFrom(Comparison_operatorContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ComparisonEqualityOpContext extends Comparison_operatorContext {
		public TerminalNode GTEQUALS() { return getToken(LvLQLParser.GTEQUALS, 0); }
		public TerminalNode LTEQUALS() { return getToken(LvLQLParser.LTEQUALS, 0); }
		public ComparisonEqualityOpContext(Comparison_operatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterComparisonEqualityOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitComparisonEqualityOp(this);
		}
	}
	public static class ComparisonOpContext extends Comparison_operatorContext {
		public TerminalNode GT() { return getToken(LvLQLParser.GT, 0); }
		public TerminalNode LT() { return getToken(LvLQLParser.LT, 0); }
		public ComparisonOpContext(Comparison_operatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterComparisonOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitComparisonOp(this);
		}
	}
	public static class InequalityOpContext extends Comparison_operatorContext {
		public TerminalNode NEQUALS() { return getToken(LvLQLParser.NEQUALS, 0); }
		public InequalityOpContext(Comparison_operatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterInequalityOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitInequalityOp(this);
		}
	}
	public static class EqualityOpContext extends Comparison_operatorContext {
		public TerminalNode EQUALS() { return getToken(LvLQLParser.EQUALS, 0); }
		public EqualityOpContext(Comparison_operatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterEqualityOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitEqualityOp(this);
		}
	}

	public final Comparison_operatorContext comparison_operator() throws RecognitionException {
		Comparison_operatorContext _localctx = new Comparison_operatorContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_comparison_operator);
		int _la;
		try {
			setState(49);
			switch (_input.LA(1)) {
			case EQUALS:
				_localctx = new EqualityOpContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(45);
				match(EQUALS);
				}
				break;
			case NEQUALS:
				_localctx = new InequalityOpContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(46);
				match(NEQUALS);
				}
				break;
			case GT:
			case LT:
				_localctx = new ComparisonOpContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(47);
				_la = _input.LA(1);
				if ( !(_la==GT || _la==LT) ) {
				_errHandler.recoverInline(this);
				} else {
					consume();
				}
				}
				break;
			case GTEQUALS:
			case LTEQUALS:
				_localctx = new ComparisonEqualityOpContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(48);
				_la = _input.LA(1);
				if ( !(_la==GTEQUALS || _la==LTEQUALS) ) {
				_errHandler.recoverInline(this);
				} else {
					consume();
				}
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

	public static class ExpressionContext extends ParserRuleContext {
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
	 
		public ExpressionContext() { }
		public void copyFrom(ExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class SimpleExContext extends ExpressionContext {
		public Atomic_expressionContext atomic_expression() {
			return getRuleContext(Atomic_expressionContext.class,0);
		}
		public SimpleExContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterSimpleEx(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitSimpleEx(this);
		}
	}
	public static class EmbeddedExContext extends ExpressionContext {
		public TerminalNode OPEN_PAREN() { return getToken(LvLQLParser.OPEN_PAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode CLOSE_PAREN() { return getToken(LvLQLParser.CLOSE_PAREN, 0); }
		public EmbeddedExContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterEmbeddedEx(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitEmbeddedEx(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_expression);
		try {
			setState(56);
			switch (_input.LA(1)) {
			case OPEN_PAREN:
				_localctx = new EmbeddedExContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(51);
				match(OPEN_PAREN);
				setState(52);
				expression();
				setState(53);
				match(CLOSE_PAREN);
				}
				break;
			case SEQUENCE_SIMILARITY_SEARCH:
			case NEAR_SEARCH:
			case NUMERIC:
			case STRING:
				_localctx = new SimpleExContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(55);
				atomic_expression();
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

	public static class Atomic_expressionContext extends ParserRuleContext {
		public Atomic_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atomic_expression; }
	 
		public Atomic_expressionContext() { }
		public void copyFrom(Atomic_expressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class NumericSimpleExContext extends Atomic_expressionContext {
		public TerminalNode NUMERIC() { return getToken(LvLQLParser.NUMERIC, 0); }
		public NumericSimpleExContext(Atomic_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterNumericSimpleEx(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitNumericSimpleEx(this);
		}
	}
	public static class SSSSimpleExContext extends Atomic_expressionContext {
		public TerminalNode SEQUENCE_SIMILARITY_SEARCH() { return getToken(LvLQLParser.SEQUENCE_SIMILARITY_SEARCH, 0); }
		public TerminalNode OPEN_PAREN() { return getToken(LvLQLParser.OPEN_PAREN, 0); }
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public TerminalNode CLOSE_PAREN() { return getToken(LvLQLParser.CLOSE_PAREN, 0); }
		public TerminalNode COMMA() { return getToken(LvLQLParser.COMMA, 0); }
		public TerminalNode STRING() { return getToken(LvLQLParser.STRING, 0); }
		public SSSSimpleExContext(Atomic_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterSSSSimpleEx(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitSSSSimpleEx(this);
		}
	}
	public static class TermSimpleExContext extends Atomic_expressionContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public TermSimpleExContext(Atomic_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterTermSimpleEx(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitTermSimpleEx(this);
		}
	}
	public static class NearSearchExContext extends Atomic_expressionContext {
		public TerminalNode NEAR_SEARCH() { return getToken(LvLQLParser.NEAR_SEARCH, 0); }
		public TerminalNode OPEN_PAREN() { return getToken(LvLQLParser.OPEN_PAREN, 0); }
		public CoordinatesContext coordinates() {
			return getRuleContext(CoordinatesContext.class,0);
		}
		public TerminalNode CLOSE_PAREN() { return getToken(LvLQLParser.CLOSE_PAREN, 0); }
		public NearSearchExContext(Atomic_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterNearSearchEx(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitNearSearchEx(this);
		}
	}

	public final Atomic_expressionContext atomic_expression() throws RecognitionException {
		Atomic_expressionContext _localctx = new Atomic_expressionContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_atomic_expression);
		int _la;
		try {
			setState(74);
			switch (_input.LA(1)) {
			case SEQUENCE_SIMILARITY_SEARCH:
				_localctx = new SSSSimpleExContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(58);
				match(SEQUENCE_SIMILARITY_SEARCH);
				setState(59);
				match(OPEN_PAREN);
				setState(60);
				term();
				setState(63);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(61);
					match(COMMA);
					setState(62);
					match(STRING);
					}
				}

				setState(65);
				match(CLOSE_PAREN);
				}
				break;
			case NEAR_SEARCH:
				_localctx = new NearSearchExContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(67);
				match(NEAR_SEARCH);
				setState(68);
				match(OPEN_PAREN);
				setState(69);
				coordinates();
				setState(70);
				match(CLOSE_PAREN);
				}
				break;
			case STRING:
				_localctx = new TermSimpleExContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(72);
				term();
				}
				break;
			case NUMERIC:
				_localctx = new NumericSimpleExContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(73);
				match(NUMERIC);
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

	public static class CoordinatesContext extends ParserRuleContext {
		public LongitudeContext longitude() {
			return getRuleContext(LongitudeContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(LvLQLParser.COMMA, 0); }
		public LatitudeContext latitude() {
			return getRuleContext(LatitudeContext.class,0);
		}
		public CoordinatesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_coordinates; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterCoordinates(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitCoordinates(this);
		}
	}

	public final CoordinatesContext coordinates() throws RecognitionException {
		CoordinatesContext _localctx = new CoordinatesContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_coordinates);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(76);
			longitude();
			setState(77);
			match(COMMA);
			setState(78);
			latitude();
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

	public static class LatitudeContext extends ParserRuleContext {
		public TerminalNode NUMERIC() { return getToken(LvLQLParser.NUMERIC, 0); }
		public LatitudeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_latitude; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterLatitude(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitLatitude(this);
		}
	}

	public final LatitudeContext latitude() throws RecognitionException {
		LatitudeContext _localctx = new LatitudeContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_latitude);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(80);
			match(NUMERIC);
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

	public static class LongitudeContext extends ParserRuleContext {
		public TerminalNode NUMERIC() { return getToken(LvLQLParser.NUMERIC, 0); }
		public LongitudeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_longitude; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterLongitude(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitLongitude(this);
		}
	}

	public final LongitudeContext longitude() throws RecognitionException {
		LongitudeContext _localctx = new LongitudeContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_longitude);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(82);
			match(NUMERIC);
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

	public static class TermContext extends ParserRuleContext {
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
	 
		public TermContext() { }
		public void copyFrom(TermContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class FieldTermContext extends TermContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public FieldTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterFieldTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitFieldTerm(this);
		}
	}
	public static class StringTermContext extends TermContext {
		public TerminalNode STRING() { return getToken(LvLQLParser.STRING, 0); }
		public StringTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterStringTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitStringTerm(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_term);
		try {
			setState(86);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				_localctx = new FieldTermContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(84);
				field();
				}
				break;
			case 2:
				_localctx = new StringTermContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(85);
				match(STRING);
				}
				break;
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

	public static class FieldContext extends ParserRuleContext {
		public List<TerminalNode> STRING() { return getTokens(LvLQLParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(LvLQLParser.STRING, i);
		}
		public TerminalNode FIELD_SEP() { return getToken(LvLQLParser.FIELD_SEP, 0); }
		public FieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_field; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).enterField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LvLQLParserListener ) ((LvLQLParserListener)listener).exitField(this);
		}
	}

	public final FieldContext field() throws RecognitionException {
		FieldContext _localctx = new FieldContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_field);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(88);
			match(STRING);
			setState(89);
			match(FIELD_SEP);
			setState(90);
			match(STRING);
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 0:
			return query_sempred((QueryContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean query_sempred(QueryContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\34_\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\3"+
		"\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\5\2\37\n\2\3\2\3\2\3\2\7\2$\n\2\f\2\16"+
		"\2\'\13\2\3\3\3\3\3\3\3\3\3\3\5\3.\n\3\3\4\3\4\3\4\3\4\5\4\64\n\4\3\5"+
		"\3\5\3\5\3\5\3\5\5\5;\n\5\3\6\3\6\3\6\3\6\3\6\5\6B\n\6\3\6\3\6\3\6\3\6"+
		"\3\6\3\6\3\6\3\6\3\6\5\6M\n\6\3\7\3\7\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n"+
		"\5\nY\n\n\3\13\3\13\3\13\3\13\3\13\2\3\2\f\2\4\6\b\n\f\16\20\22\24\2\5"+
		"\4\2\21\21\23\23\3\2\17\20\3\2\r\16a\2\36\3\2\2\2\4-\3\2\2\2\6\63\3\2"+
		"\2\2\b:\3\2\2\2\nL\3\2\2\2\fN\3\2\2\2\16R\3\2\2\2\20T\3\2\2\2\22X\3\2"+
		"\2\2\24Z\3\2\2\2\26\27\b\2\1\2\27\30\7\22\2\2\30\37\5\2\2\4\31\32\7\t"+
		"\2\2\32\33\5\2\2\2\33\34\7\n\2\2\34\37\3\2\2\2\35\37\5\4\3\2\36\26\3\2"+
		"\2\2\36\31\3\2\2\2\36\35\3\2\2\2\37%\3\2\2\2 !\f\3\2\2!\"\t\2\2\2\"$\5"+
		"\2\2\4# \3\2\2\2$\'\3\2\2\2%#\3\2\2\2%&\3\2\2\2&\3\3\2\2\2\'%\3\2\2\2"+
		"()\5\b\5\2)*\5\6\4\2*+\5\b\5\2+.\3\2\2\2,.\5\22\n\2-(\3\2\2\2-,\3\2\2"+
		"\2.\5\3\2\2\2/\64\7\13\2\2\60\64\7\f\2\2\61\64\t\3\2\2\62\64\t\4\2\2\63"+
		"/\3\2\2\2\63\60\3\2\2\2\63\61\3\2\2\2\63\62\3\2\2\2\64\7\3\2\2\2\65\66"+
		"\7\t\2\2\66\67\5\b\5\2\678\7\n\2\28;\3\2\2\29;\5\n\6\2:\65\3\2\2\2:9\3"+
		"\2\2\2;\t\3\2\2\2<=\7\3\2\2=>\7\t\2\2>A\5\22\n\2?@\7\6\2\2@B\7\31\2\2"+
		"A?\3\2\2\2AB\3\2\2\2BC\3\2\2\2CD\7\n\2\2DM\3\2\2\2EF\7\4\2\2FG\7\t\2\2"+
		"GH\5\f\7\2HI\7\n\2\2IM\3\2\2\2JM\5\22\n\2KM\7\24\2\2L<\3\2\2\2LE\3\2\2"+
		"\2LJ\3\2\2\2LK\3\2\2\2M\13\3\2\2\2NO\5\20\t\2OP\7\6\2\2PQ\5\16\b\2Q\r"+
		"\3\2\2\2RS\7\24\2\2S\17\3\2\2\2TU\7\24\2\2U\21\3\2\2\2VY\5\24\13\2WY\7"+
		"\31\2\2XV\3\2\2\2XW\3\2\2\2Y\23\3\2\2\2Z[\7\31\2\2[\\\7\5\2\2\\]\7\31"+
		"\2\2]\25\3\2\2\2\n\36%-\63:ALX";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}