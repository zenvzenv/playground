package zhengwei.leetcode.lcof;

import java.util.HashMap;
import java.util.Map;

/**
 * 剑指offer第50题：第一个不重复的字符
 *
 * @author zhengwei AKA Awei
 * @since 2020/6/22 14:59
 */
public class Lcof50FirstUniqChar {
    public static char firstUniqChar(String s) {
        Map<Character, Boolean> map = new HashMap<>();
        for (int i = 0; i < s.length(); i++) {
            map.put(s.charAt(i), !map.containsKey(s.charAt(i)));
        }
        for (int i = 0; i < s.length(); i++) {
            if (map.get(s.charAt(i))) return s.charAt(i);
        }
        return ' ';
    }

    public static void main(String[] args) {
        System.out.println(firstUniqChar("leetcode"));
    }
}
