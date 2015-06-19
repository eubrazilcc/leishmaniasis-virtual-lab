// Generated from eu/eubrazilcc/lvl/storage/antlr4/ql/LvLQLParser.g4 by ANTLR 4.5
package eu.eubrazilcc.lvl.storage.antlr4.ql;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link LvLQLParser}.
 */
public interface LvLQLParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by the {@code NegationQuery}
	 * labeled alternative in {@link LvLQLParser#query}.
	 * @param ctx the parse tree
	 */
	void enterNegationQuery(LvLQLParser.NegationQueryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NegationQuery}
	 * labeled alternative in {@link LvLQLParser#query}.
	 * @param ctx the parse tree
	 */
	void exitNegationQuery(LvLQLParser.NegationQueryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code RelationQuery}
	 * labeled alternative in {@link LvLQLParser#query}.
	 * @param ctx the parse tree
	 */
	void enterRelationQuery(LvLQLParser.RelationQueryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code RelationQuery}
	 * labeled alternative in {@link LvLQLParser#query}.
	 * @param ctx the parse tree
	 */
	void exitRelationQuery(LvLQLParser.RelationQueryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SimpleQuery}
	 * labeled alternative in {@link LvLQLParser#query}.
	 * @param ctx the parse tree
	 */
	void enterSimpleQuery(LvLQLParser.SimpleQueryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SimpleQuery}
	 * labeled alternative in {@link LvLQLParser#query}.
	 * @param ctx the parse tree
	 */
	void exitSimpleQuery(LvLQLParser.SimpleQueryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EmbeddedQuery}
	 * labeled alternative in {@link LvLQLParser#query}.
	 * @param ctx the parse tree
	 */
	void enterEmbeddedQuery(LvLQLParser.EmbeddedQueryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EmbeddedQuery}
	 * labeled alternative in {@link LvLQLParser#query}.
	 * @param ctx the parse tree
	 */
	void exitEmbeddedQuery(LvLQLParser.EmbeddedQueryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ComparisonSimpleQuery}
	 * labeled alternative in {@link LvLQLParser#atomic_query}.
	 * @param ctx the parse tree
	 */
	void enterComparisonSimpleQuery(LvLQLParser.ComparisonSimpleQueryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ComparisonSimpleQuery}
	 * labeled alternative in {@link LvLQLParser#atomic_query}.
	 * @param ctx the parse tree
	 */
	void exitComparisonSimpleQuery(LvLQLParser.ComparisonSimpleQueryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code TermSimpleQuery}
	 * labeled alternative in {@link LvLQLParser#atomic_query}.
	 * @param ctx the parse tree
	 */
	void enterTermSimpleQuery(LvLQLParser.TermSimpleQueryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code TermSimpleQuery}
	 * labeled alternative in {@link LvLQLParser#atomic_query}.
	 * @param ctx the parse tree
	 */
	void exitTermSimpleQuery(LvLQLParser.TermSimpleQueryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EqualityOp}
	 * labeled alternative in {@link LvLQLParser#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void enterEqualityOp(LvLQLParser.EqualityOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EqualityOp}
	 * labeled alternative in {@link LvLQLParser#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void exitEqualityOp(LvLQLParser.EqualityOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code InequalityOp}
	 * labeled alternative in {@link LvLQLParser#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void enterInequalityOp(LvLQLParser.InequalityOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code InequalityOp}
	 * labeled alternative in {@link LvLQLParser#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void exitInequalityOp(LvLQLParser.InequalityOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ComparisonOp}
	 * labeled alternative in {@link LvLQLParser#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOp(LvLQLParser.ComparisonOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ComparisonOp}
	 * labeled alternative in {@link LvLQLParser#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOp(LvLQLParser.ComparisonOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ComparisonEqualityOp}
	 * labeled alternative in {@link LvLQLParser#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonEqualityOp(LvLQLParser.ComparisonEqualityOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ComparisonEqualityOp}
	 * labeled alternative in {@link LvLQLParser#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonEqualityOp(LvLQLParser.ComparisonEqualityOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EmbeddedEx}
	 * labeled alternative in {@link LvLQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterEmbeddedEx(LvLQLParser.EmbeddedExContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EmbeddedEx}
	 * labeled alternative in {@link LvLQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitEmbeddedEx(LvLQLParser.EmbeddedExContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SimpleEx}
	 * labeled alternative in {@link LvLQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterSimpleEx(LvLQLParser.SimpleExContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SimpleEx}
	 * labeled alternative in {@link LvLQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitSimpleEx(LvLQLParser.SimpleExContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SSSSimpleEx}
	 * labeled alternative in {@link LvLQLParser#atomic_expression}.
	 * @param ctx the parse tree
	 */
	void enterSSSSimpleEx(LvLQLParser.SSSSimpleExContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SSSSimpleEx}
	 * labeled alternative in {@link LvLQLParser#atomic_expression}.
	 * @param ctx the parse tree
	 */
	void exitSSSSimpleEx(LvLQLParser.SSSSimpleExContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NearSearchEx}
	 * labeled alternative in {@link LvLQLParser#atomic_expression}.
	 * @param ctx the parse tree
	 */
	void enterNearSearchEx(LvLQLParser.NearSearchExContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NearSearchEx}
	 * labeled alternative in {@link LvLQLParser#atomic_expression}.
	 * @param ctx the parse tree
	 */
	void exitNearSearchEx(LvLQLParser.NearSearchExContext ctx);
	/**
	 * Enter a parse tree produced by the {@code TermSimpleEx}
	 * labeled alternative in {@link LvLQLParser#atomic_expression}.
	 * @param ctx the parse tree
	 */
	void enterTermSimpleEx(LvLQLParser.TermSimpleExContext ctx);
	/**
	 * Exit a parse tree produced by the {@code TermSimpleEx}
	 * labeled alternative in {@link LvLQLParser#atomic_expression}.
	 * @param ctx the parse tree
	 */
	void exitTermSimpleEx(LvLQLParser.TermSimpleExContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NumericSimpleEx}
	 * labeled alternative in {@link LvLQLParser#atomic_expression}.
	 * @param ctx the parse tree
	 */
	void enterNumericSimpleEx(LvLQLParser.NumericSimpleExContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NumericSimpleEx}
	 * labeled alternative in {@link LvLQLParser#atomic_expression}.
	 * @param ctx the parse tree
	 */
	void exitNumericSimpleEx(LvLQLParser.NumericSimpleExContext ctx);
	/**
	 * Enter a parse tree produced by {@link LvLQLParser#coordinates}.
	 * @param ctx the parse tree
	 */
	void enterCoordinates(LvLQLParser.CoordinatesContext ctx);
	/**
	 * Exit a parse tree produced by {@link LvLQLParser#coordinates}.
	 * @param ctx the parse tree
	 */
	void exitCoordinates(LvLQLParser.CoordinatesContext ctx);
	/**
	 * Enter a parse tree produced by {@link LvLQLParser#latitude}.
	 * @param ctx the parse tree
	 */
	void enterLatitude(LvLQLParser.LatitudeContext ctx);
	/**
	 * Exit a parse tree produced by {@link LvLQLParser#latitude}.
	 * @param ctx the parse tree
	 */
	void exitLatitude(LvLQLParser.LatitudeContext ctx);
	/**
	 * Enter a parse tree produced by {@link LvLQLParser#longitude}.
	 * @param ctx the parse tree
	 */
	void enterLongitude(LvLQLParser.LongitudeContext ctx);
	/**
	 * Exit a parse tree produced by {@link LvLQLParser#longitude}.
	 * @param ctx the parse tree
	 */
	void exitLongitude(LvLQLParser.LongitudeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FieldTerm}
	 * labeled alternative in {@link LvLQLParser#term}.
	 * @param ctx the parse tree
	 */
	void enterFieldTerm(LvLQLParser.FieldTermContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FieldTerm}
	 * labeled alternative in {@link LvLQLParser#term}.
	 * @param ctx the parse tree
	 */
	void exitFieldTerm(LvLQLParser.FieldTermContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StringTerm}
	 * labeled alternative in {@link LvLQLParser#term}.
	 * @param ctx the parse tree
	 */
	void enterStringTerm(LvLQLParser.StringTermContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StringTerm}
	 * labeled alternative in {@link LvLQLParser#term}.
	 * @param ctx the parse tree
	 */
	void exitStringTerm(LvLQLParser.StringTermContext ctx);
	/**
	 * Enter a parse tree produced by {@link LvLQLParser#field}.
	 * @param ctx the parse tree
	 */
	void enterField(LvLQLParser.FieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link LvLQLParser#field}.
	 * @param ctx the parse tree
	 */
	void exitField(LvLQLParser.FieldContext ctx);
}