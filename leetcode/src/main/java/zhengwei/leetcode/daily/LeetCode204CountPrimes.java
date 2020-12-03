package zhengwei.leetcode.daily;

/**
 * 204. 计数质数
 *
 * @author zhengwei AKA zenv
 * @since 2020/12/3 14:37
 */
public class LeetCode204CountPrimes {
    public int countPrimes(int n) {
        int ans = 0;
        for (int i = 2; i < n; i++) {
            ans += isPrime(i) ? 1 : 0;
        }
        return ans;
    }

    private boolean isPrime(int n) {
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

}
