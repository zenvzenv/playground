package zhengwei.antlr.csv;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhengwei AKA zenv
 * @since 2021/5/13 13:11
 */
public class Loader extends CSVBaseListener {
    public static final String EMPTY = "";
    //每一行数据
    List<Map<String, String>> rows = new ArrayList<>();
    //列名
    List<String> header;
    //构造一个存每一行的数据
    List<String> currentRowFieldValues;

    @Override
    public void exitString(CSVParser.StringContext ctx) {
        currentRowFieldValues.add(ctx.STRING().getText());
    }

    @Override
    public void exitText(CSVParser.TextContext ctx) {
        currentRowFieldValues.add(ctx.TEXT().getText());
    }

    @Override
    public void exitEmpty(CSVParser.EmptyContext ctx) {
        currentRowFieldValues.add(EMPTY);
    }

    @Override
    public void exitHdr(CSVParser.HdrContext ctx) {
        header = new ArrayList<>();
        header.addAll(currentRowFieldValues);
    }

    @Override
    public void enterRow(CSVParser.RowContext ctx) {
        currentRowFieldValues = new ArrayList<>();
    }

    @Override
    public void exitRow(CSVParser.RowContext ctx) {
        if (ctx.getParent().getRuleIndex() == CSVParser.RULE_hdr) return;
        Map<String, String> line = new LinkedHashMap<>();
        int i = 0;
        for (String currentRowFieldValue : currentRowFieldValues) {
            line.put(header.get(i), currentRowFieldValue);
            i++;
        }
        rows.add(line);
    }
}
