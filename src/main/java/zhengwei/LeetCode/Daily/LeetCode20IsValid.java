package zhengwei.LeetCode.Daily;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * LeetCode第20题：有效括号
 * <p>利用栈辅助求解
 *
 * @author zhengwei AKA Awei
 * @since 2020/4/1 14:12
 */
public class LeetCode20IsValid {
    private static final Map<Character, Character> map = new HashMap<>(3);

    static {
        map.put(')', '(');
        map.put(']', '[');
        map.put('}', '{');
    }

    public static void main(String[] args) {
        System.out.println(isValid("([]"));
    }

    private static boolean isValid(String s) {
        Stack<Character> stack = new Stack<>();
        final char[] chars = s.toCharArray();
        for (char c : chars) {
            if (map.containsKey(c)) {
                if (stack.isEmpty()) return false;
                final Character top = stack.pop();
                final Character should = map.get(c);
                if (top != should) return false;
            } else {
                stack.push(c);
            }
        }
        return stack.isEmpty();
    }
}
