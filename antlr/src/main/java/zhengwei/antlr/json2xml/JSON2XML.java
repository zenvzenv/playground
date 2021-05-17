package zhengwei.antlr.json2xml;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

public class JSON2XML {
    private static final class XMLEmitter extends JSONBaseListener {
        private final ParseTreeProperty<String> xml = new ParseTreeProperty<>();

        private static String stripQuotes(String s) {
            if (s == null || s.charAt(0) != '"') return s;
            return s.substring(1, s.length() - 1);
        }

        /**
         * 对于 Atom 字数，只需要获取字面值即可，将值存放到父节点，
         * 父节点是 Atom，子树是 number,true,false,null
         *
         * @param ctx Atom 上下文
         */
        @Override
        public void exitAtom(JSONParser.AtomContext ctx) {
            xml.put(ctx, ctx.getText());
        }

        @Override
        public void exitString(JSONParser.StringContext ctx) {
            xml.put(ctx, stripQuotes(ctx.getText()));
        }

        @Override
        public void exitObjectValue(JSONParser.ObjectValueContext ctx) {
            xml.put(ctx, xml.get(ctx.object()));
        }

        @Override
        public void exitArrayValue(JSONParser.ArrayValueContext ctx) {
            xml.put(ctx, xml.get(ctx.array()));
        }

        @Override
        public void exitPair(JSONParser.PairContext ctx) {
            final String tag = stripQuotes(ctx.STRING().getText());
            final JSONParser.ValueContext vctx = ctx.value();
            final String x = String.format("<%s>%s</%s>\n", tag, xml.get(vctx), tag);
            xml.put(ctx, x);
        }

        @Override
        public void exitAnObject(JSONParser.AnObjectContext ctx) {
            final StringBuffer buf = new StringBuffer();
            buf.append("\n");
            ctx.pair().forEach(pctx -> buf.append(xml.get(pctx)));
            xml.put(ctx, buf.toString());
        }

        @Override
        public void exitEmptyObject(JSONParser.EmptyObjectContext ctx) {
            xml.put(ctx, "");
        }

        @Override
        public void exitArrayOfValues(JSONParser.ArrayOfValuesContext ctx) {
            final StringBuffer buf = new StringBuffer();
            buf.append("\n");
            ctx.value().forEach(vctx -> {
                buf.append("<element>");
                buf.append(xml.get(vctx));
                buf.append("</element>");
                buf.append("\n");
            });
            xml.put(ctx, buf.toString());
        }

        @Override
        public void exitEmptyArray(JSONParser.EmptyArrayContext ctx) {
            xml.put(ctx, "");
        }

        @Override
        public void exitJson(JSONParser.JsonContext ctx) {
            xml.put(ctx, xml.get(ctx.getChild(0)));
        }
    }
}
