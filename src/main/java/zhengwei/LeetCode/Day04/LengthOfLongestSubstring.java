package zhengwei.LeetCode.Day04;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhengwei
 * @since 2019/3/4 16:31
 * LeetCode https://leetcode-cn.com/problems/longest-substring-without-repeating-characters/
 */
public class LengthOfLongestSubstring {
    public int lengthOfLongestSubstring(String s) {
        int n=s.length(),ans=0;
        Map<Character,Integer> map=new HashMap<>();
        for(int j=0,i=0;j<n;j++){
            if(map.containsKey(s.charAt(j))){
                i=Math.max(map.get(s.charAt(j)),i);
            }
            ans=Math.max(ans,j-i+1);
            map.put(s.charAt(j),j+1);
        }
        return ans;
    }
}
