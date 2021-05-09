package zhengwei.antlr.arrayinit;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        //{1,2,3}
        final CharStream input = CharStreams.fromStream(System.in);
        final ArrayInitLexer lexer = new ArrayInitLexer(input);
        final CommonTokenStream token = new CommonTokenStream(lexer);
        final ArrayInitParser parser = new ArrayInitParser(token);
        final ParseTree tree = parser.init();
        //打印 LISP 风格的语法树
        System.out.println(tree.toStringTree(parser));
    }
}
