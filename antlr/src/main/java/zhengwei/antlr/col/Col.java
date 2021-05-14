package zhengwei.antlr.col;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;

/**
 * @author zhengwei AKA zenv
 * @since 2021/5/10 10:41
 */
public class Col {
    public static void main(String[] args) throws IOException {
        final CharStream input = CharStreams.fromStream(System.in);
        RowsLexer lexer = new RowsLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        int col = Integer.parseInt(args[0]);
        RowsParser parser = new RowsParser(tokens, col); // pass column number!
        parser.setBuildParseTree(false); // don't waste time building a tree
        parser.file(); // parse
    }
}
