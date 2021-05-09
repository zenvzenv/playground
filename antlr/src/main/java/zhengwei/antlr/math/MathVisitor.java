// Generated from I:/MyWorkspace/playground/antlr/src/main/resources\Math.g4 by ANTLR 4.9.1
package zhengwei.antlr.math;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MathParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MathVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MathParser#prog}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProg(MathParser.ProgContext ctx);
	/**
	 * Visit a parse tree produced by {@link MathParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStat(MathParser.StatContext ctx);
	/**
	 * Visit a parse tree produced by {@link MathParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(MathParser.ExprContext ctx);
}