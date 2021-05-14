package zhengwei.antlr.userviolation;

import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.util.concurrent.TimeUnit;

/**
 * @author zhengwei AKA zenv
 * @since 2021/5/10 19:58
 */
public class TestUserViolationWithListener extends UserViolationRuleBaseListener {
    //antlr 提供的辅助类，用于各个分支共享数据使用
    private final ParseTreeProperty<Object> prop = new ParseTreeProperty<>();

    private Pattern<Event, Event> pattern;

    @Override
    public void exitWho(UserViolationRuleParser.WhoContext ctx) {
        final String who = ctx.WHO().getText();
        prop.put(ctx, who);
        pattern = Pattern.begin("rule", AfterMatchSkipStrategy.skipPastLastEvent());
    }

    @Override
    public void exitRangeTime(UserViolationRuleParser.RangeTimeContext ctx) {
        final String startTime = ctx.getChild(1).getText();
        final String endTime = ctx.getChild(2).getText();
        prop.put(ctx, new RangeTime(startTime, endTime));
        pattern = pattern.where(new RangeTime(startTime, endTime));
    }

    @Override
    public void exitWindowTime(UserViolationRuleParser.WindowTimeContext ctx) {
        final int size = Integer.parseInt(ctx.getChild(1).getText());
        final String timeUnit = ctx.getChild(2).getText();
        prop.put(ctx, new WindowTime(size, timeUnit));
        pattern = pattern.within(Time.of(size, TimeUnit.valueOf(timeUnit)));
    }

    @Override
    public void exitFrequency(UserViolationRuleParser.FrequencyContext ctx) {
        final int frequency = Integer.parseInt(ctx.INT().getText());
        prop.put(ctx, frequency);
        pattern = pattern.times(frequency);
    }

    @Override
    public void exitWhat(UserViolationRuleParser.WhatContext ctx) {
        final String verb = ctx.getChild(0).getText();
        final String noun = ctx.getChild(1).getText();
        prop.put(ctx, new DoSomething(verb, noun));
        pattern = pattern.where(new DoSomething(verb, noun));
    }

    public Pattern<Event, Event> getPattern() {
        return this.pattern;
    }
}
