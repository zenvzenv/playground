package zhengwei.leetcode.daily;

/**
 * 1128. 等价多米诺骨牌对的数量
 *
 * @author zhengwei AKA zenv
 * @since 2021/1/26 18:46
 */
public class LeetCode1128NumEquivDominoPairs {
    public int numEquivDominoPairs(int[][] dominoes) {
        final int[] val = new int[100];
        int result = 0;
        for (int[] domino : dominoes) {
            final int n = domino[0] > domino[1] ? domino[0] * 10 + domino[1] : domino[1] * 10 + domino[0];
            result += val[n];
            val[n]++;
        }
        return result;
    }
}
