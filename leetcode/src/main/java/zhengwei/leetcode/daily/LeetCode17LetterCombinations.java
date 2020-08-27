package zhengwei.leetcode.daily;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 17. 电话号码的字母组合
 *
 * @author zhengwei AKA Awei
 * @since 2020/8/26 9:08
 */
public class LeetCode17LetterCombinations {
    private static final String[] letter = {"", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"};

    public static List<String> letterCombinations(String digits) {
        if (digits.length() == 0) return new ArrayList<>();
        List<String> res = new LinkedList<>();
        res.add("");
        for (int i = 0; i < digits.length(); i++) {
            final String str = letter[digits.charAt(i) - '0'];
            final int size = res.size();
            for (int j = 0; j < size; j++) {
                final String temp = res.remove(0);
                final int length = str.length();
                for (int k = 0; k < length; k++) {
                    res.add(temp + str.charAt(k));
                }
            }
        }
        return res;
    }

    public static void main(String[] args) {
        System.out.println(letterCombinations("234"));
    }
}
