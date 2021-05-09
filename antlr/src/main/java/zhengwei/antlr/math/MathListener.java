// Generated from I:/MyWorkspace/playground/antlr/src/main/resources\Math.g4 by ANTLR 4.9.1
package zhengwei.antlr.math;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MathParser}.
 */
public interface MathListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MathParser#prog}.
	 * @param ctx the parse tree
	 */
	void enterProg(MathParser.ProgContext ctx);
	/**
	 * Exit a parse tree produced by {@link MathParser#prog}.
	 * @param ctx the parse tree
	 */
	void exitProg(MathParser.ProgContext ctx);
	/**
	 * Enter a parse tree produced by {@link MathParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStat(MathParser.StatContext ctx);
	/**
	 * Exit a parse tree produced by {@link MathParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStat(MathParser.StatContext ctx);
	/**
	 * Enter a parse tree produced by {@link MathParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(MathParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link MathParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(MathParser.ExprContext ctx);
}