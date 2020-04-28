package zhengwei.leetcode.daily;

import java.util.ArrayList;
import java.util.List;

/**
 * LeetCode第22题：括号生成
 *
 * 使用深度优先遍历进行集体
 * https://leetcode-cn.com/problems/generate-parentheses/
 *
 * @author zhengwei AKA Awei
 * @since 2020/4/9 20:00
 */
public class LeetCode22GenerateParenthesis {
    static List<String> res = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println(generateParenthesis(3));
    }

    public static List<String> generateParenthesis(int n) {
        dfs(n, n, "");
        return res;
    }

    private static void dfs(int left, int right, String currStr) {
        if (left == 0 && right == 0) {
            res.add(currStr);
            return;
        }
        if (left > 0) {
            dfs(left - 1, right, currStr + "(");
        }
        if (right > left) {
            dfs(left, right - 1, currStr + ")");
        }
    }
}
