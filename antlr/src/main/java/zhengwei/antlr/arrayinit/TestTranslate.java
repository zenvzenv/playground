package zhengwei.antlr.arrayinit;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;

public class TestTranslate {
    public static void main(String[] args) throws IOException {
        final CharStream input = CharStreams.fromStream(System.in);
        final ArrayInitLexer lexer = new ArrayInitLexer(input);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final ArrayInitParser parser = new ArrayInitParser(tokens);
        final ParseTree tree = parser.init();
        final ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new ShortToUnicodeString(),tree);
        System.out.println();
    }
}
