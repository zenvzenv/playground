package zhengwei.leetcode.nowcoder;

import java.util.Scanner;

/**
 * 记负均正
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/25 9:34
 */
public class Main9 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int nCount = 0;
        int pCount = 0;
        int sum = 0;
        final String num = sc.nextLine();
        final String[] nums = num.split(" ");
        for (String n : nums) {
            if (Integer.parseInt(n) < 0) {
                nCount++;
            } else {
                pCount++;
                sum += Integer.parseInt(n);
            }
        }
        System.out.println(nCount);
        if (pCount > 0) {
            System.out.println(String.format("%.1f", (double) sum / pCount));
        } else {
            System.out.println("0.0");
        }
    }
}
