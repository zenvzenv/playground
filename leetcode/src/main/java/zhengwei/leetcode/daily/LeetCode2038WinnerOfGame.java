package zhengwei.leetcode.daily;

/**
 * 2038. 如果相邻两个颜色均相同则删除当前颜色
 *
 * @author zhengwei AKA zenv
 * @since 2022/3/22
 */
public class LeetCode2038WinnerOfGame {
    /**
     * 其实在删除 A 和删除 B 是互相独立不影响的，因为能够删除前提是前后都是相同的字符，
     * 只需要统计能够删除 A 和删除 B 的个数即可
     */
    public boolean winnerOfGame(String colors) {
        final char[] chars = colors.toCharArray();
        int a = 0, b = 0;
        for (int i = 1; i < chars.length - 1; i++) {
            if (chars[i] =='A' && chars[i - 1] == 'A' && chars[i + 1] == 'A') a++;
            if (chars[i] =='B' && chars[i - 1] == 'B' && chars[i + 1] == 'B') b++;
        }
        return a > b;
    }
}
