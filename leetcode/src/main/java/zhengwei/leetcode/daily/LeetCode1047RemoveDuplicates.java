package zhengwei.leetcode.daily;

/**
 * 1047. 删除字符串中的所有相邻重复项
 *
 * @author zhengwei AKA zenv
 * @since 2021/3/9 17:01
 */
public class LeetCode1047RemoveDuplicates {
    public String removeDuplicates(String S) {
        StringBuilder ans = new StringBuilder();
        for (int i = 0; i < S.length(); i++) {
            if (ans.length() == 0) {
                ans.append(S.charAt(i));
            } else if (S.charAt(i) == ans.charAt(ans.length() - 1)) {
                ans.deleteCharAt(ans.length() - 1);
            } else {
                ans.append(S.charAt(i));
            }
        }
        return ans.toString();
    }
}
