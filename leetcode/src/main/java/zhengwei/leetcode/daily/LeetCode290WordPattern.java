package zhengwei.leetcode.daily;

import java.util.HashMap;
import java.util.Map;

/**
 * 290. 单词规律
 *
 * @author zhengwei AKA zenv
 * @since 2020/12/16 9:08
 */
public class LeetCode290WordPattern {
    public boolean wordPattern(String pattern, String s) {
        final String[] words = s.split("[ ]");
        if (words.length != pattern.length()) return false;
        final Map<String, Character> scMap = new HashMap<>();
        final Map<Character, String> csMap = new HashMap<>();
        for (int i = 0; i < pattern.length(); i++) {
            final char c = pattern.charAt(i);
            final String word = words[i];
            if (csMap.containsKey(c) && !csMap.get(c).equals(word)) {
                return false;
            }
            if (scMap.containsKey(word) && !scMap.get(word).equals(c)) {
                return false;
            }
            csMap.put(c, word);
            scMap.put(word, c);
        }
        return true;
    }
}
