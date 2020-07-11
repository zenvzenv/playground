package zhengwei.leetcode.signalweek195;

/**
 * @author zhengwei AKA Awei
 * @since 2020/7/11 12:00
 */
public class LeetCode1497CanArrange {
    public static boolean canArrange(int[] arr, int k) {
        int[] remainder = new int[k];
        for (int n : arr) {
            //余数有可能是负数，将负数的情况屏蔽掉
            remainder[(n % k + k) % k]++;
        }
        if ((remainder[0] & 1) != 0) return false;
        for (int i = 1; i < k; i++) {
            while (remainder[i] > 0) {
                remainder[i]--;
                if (remainder[k - i] <= 0) return false;
                remainder[k - i]--;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(-1 % 5);
    }
}
