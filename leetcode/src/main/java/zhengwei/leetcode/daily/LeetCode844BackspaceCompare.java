package zhengwei.leetcode.daily;

import java.util.Stack;

/**
 * 844. 比较含退格的字符串
 *
 * @author zhengwei AKA Awei
 * @since 2020/10/19 9:23
 */
public class LeetCode844BackspaceCompare {
    public static boolean backspaceCompare(String S, String T) {
        return build(S).equals(build(T));
    }

    private static String build(String s) {
        StringBuilder sb = new StringBuilder();
        Stack<Character> stack = new Stack<>();
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (!stack.isEmpty() && '#' == c) {
                stack.pop();
            } else if ('#' != c) {
                stack.push(c);
            }
        }
        while (!stack.isEmpty()) {
            sb.append(stack.pop());
        }
        System.out.println(sb);
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(backspaceCompare("y#fo##f", "y#f#o##f"));
    }
}
