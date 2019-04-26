package zhengwei.LeetCode.Day05;

/**
 * LeetCode 找到子字符串索引 https://leetcode-cn.com/explore/interview/card/top-interview-questions-easy/5/strings/38/
 * @author zhengwei AKA DG
 * @since 2019/4/3 15:42
 */
public class StrStr {
    public int strStr(String haystack, String needle) {
        char[] chars1 = haystack.toCharArray();
        char[] chars2 = needle.toCharArray();
        int result;
        for (int i=0;i<chars1.length-chars2.length;i++){
            if (chars1[i]!=chars2[0]) continue;
            else {
                for (int j=1;j<chars2.length;j++){
                    boolean b = chars2[j] == chars1[i + j];
                    if (!b) break;
                }
            }
        }
        return 0;
    }
}
