package zhengwei.antlr.calculator;

public class MyCalculatorVisitor extends CalculatorBaseVisitor<Object> {
    @Override
    public Object visitParentExpr(CalculatorParser.ParentExprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Object visitMultOrDiv(CalculatorParser.MultOrDivContext ctx) {
        final Object obj0 = ctx.expr(0).accept(this);
        final Object obj1 = ctx.expr(1).accept(this);

        if ("*".equals(ctx.getChild(1).getText())) {
            return (float) obj0 * (float) obj1;
        } else if ("/".equals(ctx.getChild(1).getText())) {
            return (float) obj0 / (float) obj1;
        }
        return 0f;
    }

    @Override
    public Object visitAddOrSub(CalculatorParser.AddOrSubContext ctx) {
        final Object obj0 = ctx.expr(0).accept(this);
        final Object obj1 = ctx.expr(1).accept(this);

        if ("+".equals(ctx.getChild(1).getText())) {
            return (float) obj0 + (float) obj1;
        } else if ("-".equals(ctx.getChild(1).getText())) {
            return (float) obj0 - (float) obj1;
        }
        return 0f;
    }

    @Override
    public Object visitFloat(CalculatorParser.FloatContext ctx) {
        return Float.parseFloat(ctx.getText());
    }
}
