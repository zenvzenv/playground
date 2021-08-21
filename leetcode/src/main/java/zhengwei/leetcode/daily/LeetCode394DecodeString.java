package zhengwei.leetcode.daily;

import java.util.Stack;

/**
 * @author zhengwei AKA Awei
 * @since 2020/5/28 13:39
 */
public class LeetCode394DecodeString {
    public static String decodeString(String s) {
        StringBuilder res = new StringBuilder();
        int multi = 0;
        Stack<Integer> multiStack = new Stack<>();
        Stack<String> resStack = new Stack<>();
        char[] chars = s.toCharArray();
        for (char c : chars) {
            if (c == '[') {
                multiStack.push(multi);
                resStack.push(res.toString());
                multi = 0;
                res = new StringBuilder();
            } else if (c == ']') {
                StringBuilder temp = new StringBuilder();
                int curMulti = multiStack.pop();
                for (int i = 0; i < curMulti; i++) {
                    temp.append(res);
                }
                res = new StringBuilder(resStack.pop() + temp);
            } else if ('0' <= c && c <= '9') {
                multi = multi * 10 + Integer.parseInt(c + "");
            } else {
                res.append(c);
            }
        }
        return res.toString();
    }

    public static void main(String[] args) {
        System.out.println(decodeString("3[a]2[bc]"));
    }
}
