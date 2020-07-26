package zhengwei.leetcode.nowcoder;

import java.util.Scanner;
import java.util.Stack;

/**
 * 颠倒字符串
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/23 19:44
 */
public class Main4 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()) {
            final String line = sc.nextLine();
            Stack<Character> stack = new Stack<>();
            for (int i = 0; i < line.length(); i++) {
                stack.push(line.charAt(i));
            }
            StringBuilder res = new StringBuilder();
            while (!stack.isEmpty()) {
                res.append(stack.pop());
            }
            System.out.println(res);
        }
    }
}
