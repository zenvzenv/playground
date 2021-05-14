// Generated from C:/Users/zw305/Desktop/MyWorkspace/playground/antlr/src/main/java/zhengwei/antlr/g4\UserViolationRule.g4 by ANTLR 4.9.1
package zhengwei.antlr.userviolation;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link UserViolationRuleParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface UserViolationRuleVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link UserViolationRuleParser#rule}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRule(UserViolationRuleParser.RuleContext ctx);
	/**
	 * Visit a parse tree produced by {@link UserViolationRuleParser#who}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWho(UserViolationRuleParser.WhoContext ctx);
	/**
	 * Visit a parse tree produced by the {@code rangeTime}
	 * labeled alternative in {@link UserViolationRuleParser#when}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRangeTime(UserViolationRuleParser.RangeTimeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code windowTime}
	 * labeled alternative in {@link UserViolationRuleParser#when}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowTime(UserViolationRuleParser.WindowTimeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code single}
	 * labeled alternative in {@link UserViolationRuleParser#when}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingle(UserViolationRuleParser.SingleContext ctx);
	/**
	 * Visit a parse tree produced by the {@code allDay}
	 * labeled alternative in {@link UserViolationRuleParser#when}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllDay(UserViolationRuleParser.AllDayContext ctx);
	/**
	 * Visit a parse tree produced by {@link UserViolationRuleParser#what}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhat(UserViolationRuleParser.WhatContext ctx);
	/**
	 * Visit a parse tree produced by {@link UserViolationRuleParser#frequency}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrequency(UserViolationRuleParser.FrequencyContext ctx);
}