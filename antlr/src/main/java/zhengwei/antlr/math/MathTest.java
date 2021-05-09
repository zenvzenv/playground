package zhengwei.antlr.math;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class MathTest {
    public static void main(String[] args) {
        final CodePointCharStream input = CharStreams.fromString("12*2+12\r\n");
        final MathLexer mathLexer = new MathLexer(input);
        final CommonTokenStream token = new CommonTokenStream(mathLexer);
        final MathParser parser = new MathParser(token);
        ParseTree tree = parser.prog(); // parse
        MathBaseVisitor vt = new MathBaseVisitor();
        System.out.println(vt.visit(tree));
    }
}
