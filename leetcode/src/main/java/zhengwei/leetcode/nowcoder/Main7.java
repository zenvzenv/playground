package zhengwei.leetcode.nowcoder;

import java.util.Scanner;

/**
 * 取近似值
 * @author zhengwei AKA Awei
 * @since 2020/7/24 21:53
 */
public class Main7 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()) {
            double d = sc.nextDouble();
            d += 0.5;
            System.out.println((int) d);
        }
    }
}
