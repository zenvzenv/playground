// Generated from C:/Users/zw305/Desktop/MyWorkspace/playground/antlr/src/main/java/zhengwei/antlr/g4\UserViolationRule.g4 by ANTLR 4.9.1
package zhengwei.antlr.userviolation;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link UserViolationRuleParser}.
 */
public interface UserViolationRuleListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link UserViolationRuleParser#rule}.
	 * @param ctx the parse tree
	 */
	void enterRule(UserViolationRuleParser.RuleContext ctx);
	/**
	 * Exit a parse tree produced by {@link UserViolationRuleParser#rule}.
	 * @param ctx the parse tree
	 */
	void exitRule(UserViolationRuleParser.RuleContext ctx);
	/**
	 * Enter a parse tree produced by {@link UserViolationRuleParser#who}.
	 * @param ctx the parse tree
	 */
	void enterWho(UserViolationRuleParser.WhoContext ctx);
	/**
	 * Exit a parse tree produced by {@link UserViolationRuleParser#who}.
	 * @param ctx the parse tree
	 */
	void exitWho(UserViolationRuleParser.WhoContext ctx);
	/**
	 * Enter a parse tree produced by the {@code rangeTime}
	 * labeled alternative in {@link UserViolationRuleParser#when}.
	 * @param ctx the parse tree
	 */
	void enterRangeTime(UserViolationRuleParser.RangeTimeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code rangeTime}
	 * labeled alternative in {@link UserViolationRuleParser#when}.
	 * @param ctx the parse tree
	 */
	void exitRangeTime(UserViolationRuleParser.RangeTimeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code windowTime}
	 * labeled alternative in {@link UserViolationRuleParser#when}.
	 * @param ctx the parse tree
	 */
	void enterWindowTime(UserViolationRuleParser.WindowTimeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code windowTime}
	 * labeled alternative in {@link UserViolationRuleParser#when}.
	 * @param ctx the parse tree
	 */
	void exitWindowTime(UserViolationRuleParser.WindowTimeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code single}
	 * labeled alternative in {@link UserViolationRuleParser#when}.
	 * @param ctx the parse tree
	 */
	void enterSingle(UserViolationRuleParser.SingleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code single}
	 * labeled alternative in {@link UserViolationRuleParser#when}.
	 * @param ctx the parse tree
	 */
	void exitSingle(UserViolationRuleParser.SingleContext ctx);
	/**
	 * Enter a parse tree produced by the {@code allDay}
	 * labeled alternative in {@link UserViolationRuleParser#when}.
	 * @param ctx the parse tree
	 */
	void enterAllDay(UserViolationRuleParser.AllDayContext ctx);
	/**
	 * Exit a parse tree produced by the {@code allDay}
	 * labeled alternative in {@link UserViolationRuleParser#when}.
	 * @param ctx the parse tree
	 */
	void exitAllDay(UserViolationRuleParser.AllDayContext ctx);
	/**
	 * Enter a parse tree produced by {@link UserViolationRuleParser#what}.
	 * @param ctx the parse tree
	 */
	void enterWhat(UserViolationRuleParser.WhatContext ctx);
	/**
	 * Exit a parse tree produced by {@link UserViolationRuleParser#what}.
	 * @param ctx the parse tree
	 */
	void exitWhat(UserViolationRuleParser.WhatContext ctx);
	/**
	 * Enter a parse tree produced by {@link UserViolationRuleParser#frequency}.
	 * @param ctx the parse tree
	 */
	void enterFrequency(UserViolationRuleParser.FrequencyContext ctx);
	/**
	 * Exit a parse tree produced by {@link UserViolationRuleParser#frequency}.
	 * @param ctx the parse tree
	 */
	void exitFrequency(UserViolationRuleParser.FrequencyContext ctx);
}