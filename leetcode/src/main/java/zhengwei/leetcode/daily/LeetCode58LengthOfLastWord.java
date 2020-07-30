package zhengwei.leetcode.daily;

/**
 * LeetCode第58题：最后一个单词的长度
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/30 19:25
 */
public class LeetCode58LengthOfLastWord {
    //从后向前遍历，需要先将最后的空格忽略掉
    public static int lengthOfLastWord(String s) {
        int res = 0;
        int a = s.length() - 1;
        while (a > 0 && s.charAt(a) == ' ') a--;
        if (a < 0) return 0;
        while (a >= 0 && s.charAt(a) != ' ') {
            res++;
            a--;
        }
        return res;
    }

    public static void main(String[] args) {
        System.out.println(lengthOfLastWord(""));
    }
}
