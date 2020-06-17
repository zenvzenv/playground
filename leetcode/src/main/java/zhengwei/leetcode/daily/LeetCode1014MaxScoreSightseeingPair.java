package zhengwei.leetcode.daily;

/**
 * LeetCode第1014题：最佳观光组合
 * <p>
 * 最佳观光组合的计算公式为：A[i] + A[j] + i - j
 * </p>
 *
 * @author zhengwei AKA Awei
 * @since 2020/6/17 9:53
 */
public class LeetCode1014MaxScoreSightseeingPair {
    /**
     * 暴力法
     */
    private static int maxScoreSightseeingPair1(int[] A) {
        int max = 0;
        for (int i = 0; i < A.length; i++) {
            for (int j = i + 1; j < A.length; j++) {
                max = Math.max(max, A[i] + A[j] + i - j);
            }
        }
        return max;
    }

    /**
     * 将公式拆分成A[i] + i和A[j] - j;
     * 因为在遍历A数组的时候，A[j] - j是固定不变的，实际只需要计算最大的A[i] + i即可
     */
    private static int maxScoreSightseeingPair2(int[] A) {
        int res = 0;
        int max = A[0];
        for (int j = 0; j < A.length; j++) {
            //计算A[i] + A[j] + i - j
            res = Math.max(res, max + A[j] - j);
            //计算最大的A[i] + i
            max = Math.max(max, A[j] + j);
        }
        return res;
    }
}
