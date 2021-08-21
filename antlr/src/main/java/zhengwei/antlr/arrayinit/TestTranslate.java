package zhengwei.antlr.arrayinit;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;

public class TestTranslate {
    public static void main(String[] args) throws IOException {
        //新建一个 CharStream 从标准标准输入读取数据
        final CharStream input = CharStreams.fromStream(System.in);
        //新建一个词法分析器，处理输入的 CharStream
        final ArrayInitLexer lexer = new ArrayInitLexer(input);
        //新建一个词法符号缓冲区，用于存储词法分析器将要生成的词法符号
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        //新建一个语法分析器，处理词法符号缓冲区中的内容
        final ArrayInitParser parser = new ArrayInitParser(tokens);
        //针对 init 规则开始分析语法
        final ParseTree tree = parser.init();
        //新建一个通用的、能够触发回调函数的语法分析器
        final ParseTreeWalker walker = new ParseTreeWalker();
        //遍历语法分析过程中生成的语法分析树，触发回调
        walker.walk(new ShortToUnicodeString(), tree);
        //翻译完成
        System.out.println();
    }
}
