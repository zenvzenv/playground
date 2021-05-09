package zhengwei.antlr.calculator;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

public class Run {
    public static void main(String[] args) {
        String expression="3.1 * (6.3 - 4.51) + 5 * 4";
        final CodePointCharStream input = CharStreams.fromString(expression);
        final CalculatorLexer lexer = new CalculatorLexer(input);
        final CommonTokenStream token = new CommonTokenStream(lexer);
        final CalculatorParser parser = new CalculatorParser(token);
        final MyCalculatorVisitor visitor = new MyCalculatorVisitor();
        System.out.println(visitor.visit(parser.expr()));
    }
}
