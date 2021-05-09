package zhengwei.antlr.expr;

import java.util.HashMap;
import java.util.Map;

public class EvalVisitor extends ExprBaseVisitor<Integer> {
    private static final Map<String, Integer> memory = new HashMap<>();

    //ID '=' expr NEWLINE
    @Override
    public Integer visitAssign(ExprParser.AssignContext ctx) {
        //id 在 '=' 左侧
        final String id = ctx.ID().getText();
        //计算右侧表达式的值
        final Integer value = visit(ctx.expr());
        memory.put(id, value);
        return value;
    }

    //expr NEWLINE
    @Override
    public Integer visitPrintExpr(ExprParser.PrintExprContext ctx) {
        final Integer value = visit(ctx.expr());
        System.out.println(value);
        return 0;
    }

    //INT
    @Override
    public Integer visitInt(ExprParser.IntContext ctx) {
        return Integer.parseInt(ctx.INT().getText());
    }

    //ID
    @Override
    public Integer visitId(ExprParser.IdContext ctx) {
        final String id = ctx.ID().getText();
        if (memory.containsKey(id)) return memory.get(id);
        return 0;
    }

    //expr op=(MUL|DIV) expr
    @Override
    public Integer visitMulDiv(ExprParser.MulDivContext ctx) {
        //计算左侧表达式的值
        final Integer left = visit(ctx.expr(0));
        //计算右侧表达式的值
        final Integer right = visit(ctx.expr(1));
        if (ctx.op.getType() == ExprLexer.MUL) return left * right;
        else return left / right;
    }

    //expr op=(ADD|SUB) expr
    @Override
    public Integer visitAddSub(ExprParser.AddSubContext ctx) {
        final Integer left = visit(ctx.expr(0));
        final Integer right = visit(ctx.expr(1));
        if (ctx.op.getType() == ExprLexer.ADD) return left + right;
        else return left - right;
    }

    //'(' expr ')'
    @Override
    public Integer visitParents(ExprParser.ParentsContext ctx) {
        return visit(ctx.expr());
    }

    //清空没存
    @Override
    public Integer visitClear(ExprParser.ClearContext ctx) {
        memory.clear();
        return super.visitClear(ctx);
    }
}
