package zhengwei.antlr.jsontoxml;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

/**
 * @author zhengwei AKA zenv
 * @since 2021/5/13 13:42
 */
public class JSONToXML {
    public static class XMLEmitter extends JSONBaseListener {
        ParseTreeProperty<String> xml = new ParseTreeProperty<>();

        String getXml(ParseTree ctx) {
            return xml.get(ctx);
        }

        void setXml(ParseTree ctx, String s) {
            xml.put(ctx, s);
        }

        @Override
        public void exitAtom(JSONParser.AtomContext ctx) {
            setXml(ctx, ctx.getText());
        }

        @Override
        public void exitString(JSONParser.StringContext ctx) {
            setXml(ctx, stripQuotes(ctx.getText()));
        }

        @Override
        public void exitObjectValue(JSONParser.ObjectValueContext ctx) {
            setXml(ctx, getXml(ctx.object()));
        }

        @Override
        public void exitPair(JSONParser.PairContext ctx) {
            final String tag = stripQuotes(ctx.STRING().getText());
            JSONParser.ValueContext vctx = ctx.value();
            final String x = String.format("<%s>%s</%s>\n", tag, getXml(vctx), tag);
            setXml(ctx, x);
        }

        public static String stripQuotes(String s) {
            if (s == null || s.charAt(0) != '"') return s;
            return s.substring(1, s.length() - 1);
        }
    }
}
