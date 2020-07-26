package zhengwei.leetcode.nowcoder;

import java.util.Scanner;

/**
 * 最小公约数
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/23 19:09
 */
public class Main1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()) {
            int a = sc.nextInt();
            int b = sc.nextInt();
            int res = a * b;
            res = res / gcd(a, b);
            System.out.println(res);
        }
    }

    //最大公约数
    public static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }
}
