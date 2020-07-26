package zhengwei.leetcode.nowcoder;

import java.util.Scanner;

/**
 * 质数因子
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/24 21:55
 */
public class Main8 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            final int n = scanner.nextInt();
            check(n);
        }
    }

    private static void check(int n) {
        for (int i = 2; i < n; i++) {
            if (n % i == 0) {
                System.out.print(i + " ");
                n /= i;
                check(n);
                return;
            }
        }
    }
}
