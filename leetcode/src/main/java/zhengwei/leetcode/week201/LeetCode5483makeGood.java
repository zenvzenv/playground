package zhengwei.leetcode.week201;

import java.util.Stack;

public class LeetCode5483makeGood {
    public static String makeGood(String s) {
        char[] chars = s.toCharArray();
        Stack<Character> stack = new Stack<>();
        for (char aChar : chars) {
            if (stack.isEmpty() || Math.abs(aChar - stack.peek()) != 32) {
                stack.push(aChar);
            } else {
                stack.pop();
            }
        }
        StringBuilder res = new StringBuilder();
        for (char c : stack) {
            res.append(c);
        }
        return res.toString();
    }

    public static void main(String[] args) {
        System.out.println(makeGood("leEeetcode"));
    }
}
