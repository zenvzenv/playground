package zhengwei.leetcode.nowcoder;

import java.util.Scanner;

/**
 * 字符串分割
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/25 9:55
 */
public class Main10 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()) {
            final int n = sc.nextInt();
            for (int i = 0; i < n; i++) {
                StringBuilder line = new StringBuilder(sc.nextLine());
                while (line.length() % 8 != 0) {
                    line.append("0");
                }
                for (int j = 0; j < line.length(); j += 8) {
                    System.out.println(line.substring(j, j + 8));
                }
            }
        }
    }
}

