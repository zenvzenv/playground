package zhengwei.antlr.arrayinit;

public class ShortToUnicodeString extends ArrayInitBaseListener {
    //将 { 翻译成 "
    @Override
    public void enterInit(ArrayInitParser.InitContext ctx) {
        System.out.print('"');
    }

    //将 { 翻译成 "
    @Override
    public void exitInit(ArrayInitParser.InitContext ctx) {
        System.out.print('"');
    }

    //将 int 翻译成16进制数
    @Override
    public void enterValue(ArrayInitParser.ValueContext ctx) {
        final int value = Integer.parseInt(ctx.INT().getText());
        System.out.printf("\\u%04x", value);
    }
}
