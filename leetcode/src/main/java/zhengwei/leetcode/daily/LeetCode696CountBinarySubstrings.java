package zhengwei.leetcode.daily;

import java.util.ArrayList;
import java.util.List;

/**
 * LeetCode第696题：计数二进制子串
 *
 * @author zhengwei AKA Awei
 * @since 2020/8/10 9:21
 */
public class LeetCode696CountBinarySubstrings {
    public static int countBinarySubstrings(String s) {
        final List<Integer> list = new ArrayList<>();
        int len = s.length(), i = 0;
        while (i < len) {
            final char c = s.charAt(i);
            int count = 0;
            //对字符串中前后相同的字符进行分组
            while (i < len && s.charAt(i) == c) {
                i++;
                count++;
            }
            list.add(count);
        }
        int res = 0;
        for (int j = 0; j < list.size() - 1; j++) {
            res += Math.min(list.get(j), list.get(j + 1));
        }
        return res;
    }

    public static void main(String[] args) {
        System.out.println(countBinarySubstrings("00110"));
    }
}
