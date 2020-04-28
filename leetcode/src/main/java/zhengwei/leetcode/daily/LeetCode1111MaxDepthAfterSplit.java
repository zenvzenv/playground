package zhengwei.leetcode.daily;

import java.util.Arrays;
import java.util.Stack;

/**
 * @author zhengwei AKA Awei
 * @since 2020/4/1 16:48
 */
public class LeetCode1111MaxDepthAfterSplit {
    private static int[] maxDepthAfterSplit(String seq) {
        int[] ans = new int[seq.length()];
        Stack<Integer> stack = new Stack<>();
        final char[] chars = seq.toCharArray();
        int index = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == ')') {
                ans[i] = stack.pop();
                index--;
            } else if (chars[i] == '(') {
                index += 1;
                stack.push(index & 1);
                ans[i] = index & 1;
            }
        }
        return ans;
    }

    private static int[] maxDepthAfterSplit2(String seq) {
        int[] ans = new int[seq.length()];
        int index = 0;
        final char[] chars = seq.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '(') {
                index += 1;
                ans[i] = index & 1;
            } else {
                ans[i] = index & 1;
                index -= 1;
            }
        }
        return ans;
    }

    private static int[] maxDepthAfterSplit3(String seq) {
        int[] ans = new int[seq.length()];
        int idx = 0;
        for (char c : seq.toCharArray()) {
            ans[idx++] = c == '(' ? idx & 1 : ((idx + 1) & 1);
        }
        return ans;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(maxDepthAfterSplit("((()))")));
        System.out.println(Arrays.toString(maxDepthAfterSplit2("((()))")));
        System.out.println(Arrays.toString(maxDepthAfterSplit3("((()))")));
    }
}
