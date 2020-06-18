package zhengwei.leetcode.daily;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * LeetCode第187题：重复的DNA序列
 *
 * @author zhengwei AKA Awei
 * @since 2020/6/18 19:26
 */
public class LeetCode187FindRepeatedDnaSequences {
    public static List<String> findRepeatedDnaSequences(String s) {
        Set<String> set = new HashSet<>();
        Set<String> res = new HashSet<>();
        int left = 0;
        while (left + 10 <= s.length()) {
            //substring左闭右开
            String temp = s.substring(left, left + 10);
            if (!set.contains(temp)) {
                set.add(temp);
            } else {
                res.add(temp);
            }
            left++;
        }
        return new ArrayList<>(res);
    }

    public static void main(String[] args) {
        System.out.println(findRepeatedDnaSequences("AAAAAAAAAAA"));
    }
}
