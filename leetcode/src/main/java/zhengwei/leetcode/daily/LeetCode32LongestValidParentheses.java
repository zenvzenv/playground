package zhengwei.leetcode.daily;

import java.util.Stack;

/**
 * LeetCode第32题：最长有效括号
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/4 8:40
 */
public class LeetCode32LongestValidParentheses {
    public static int longestValidParentheses(String s) {
        Stack<Integer> stack = new Stack<>();
        stack.push(-1);
        int max = 0;
        final char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '(') {
                stack.push(i);
            } else {
                stack.pop();
                if (stack.isEmpty()) {
                    stack.push(i);
                } else {
                    max = Math.max(max, i - stack.peek());
                }
            }
        }
        return max;
    }

    public static void main(String[] args) {
        System.out.println(longestValidParentheses(")()())"));
    }
}
