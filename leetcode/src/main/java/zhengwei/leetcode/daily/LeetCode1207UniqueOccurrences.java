package zhengwei.leetcode.daily;

/**
 * 1207. 独一无二的出现次数
 *
 * @author zhengwei AKA Awei
 * @since 2020/10/28 9:20
 */
public class LeetCode1207UniqueOccurrences {
    public static boolean uniqueOccurrences(int[] arr) {
        int[] cnt = new int[2002];
        for (int i : arr) {
            cnt[i + 1000]++;
        }
        boolean[] b = new boolean[2002];
        for (int i : cnt) {
            if (i > 0) {
                if (!b[i]) b[i] = true;
                else return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(uniqueOccurrences(new int[]{1, 2, 2, 1, 1, 3}));
    }
}
