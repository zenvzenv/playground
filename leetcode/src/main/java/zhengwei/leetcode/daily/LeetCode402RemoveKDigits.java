package zhengwei.leetcode.daily;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 402. 移掉K位数字
 *
 * @author zhengwei AKA zenv
 * @since 2020/12/2 11:04
 */
public class LeetCode402RemoveKDigits {
    public static String removeKdigits(String num, int k) {
        final Deque<Character> deque = new LinkedList<>();
        for (int i = 0; i < num.length(); i++) {
            final char c = num.charAt(i);
            while (!deque.isEmpty() && deque.peekLast() > c && k > 0) {
                deque.pollLast();
                k--;
            }
            deque.offerLast(c);
        }

        //防止原数字是单调递增的，在第一步过滤中过滤不掉数据
        //使用此方式来过滤数字
        for (int i = 0; i < k; i++) {
            deque.pollLast();
        }

        final StringBuilder ans = new StringBuilder();
        boolean zero = true;
        while (!deque.isEmpty()) {
            final Character first = deque.pollFirst();
            if (zero && first == '0') {
                continue;
            }
            zero = false;
            ans.append(first);
        }
        return ans.length() == 0 ? "0" : ans.toString();
    }

    public static void main(String[] args) {
        System.out.println(removeKdigits("9", 1));
    }
}
