package zhengwei.LeetCode.Day05;

import java.util.HashMap;
import java.util.Map;

/**
 * LeetCode 是否是回文 https://leetcode-cn.com/explore/interview/card/top-interview-questions-easy/5/strings/35/
 * @author zhengwei AKA DG
 * @since 2019/4/3 15:11
 */
public class IsAnagram {
    public boolean isAnagram(String s, String t) {
        if ("".equals(s)&&"".equals(t)) return true;
        char[] chars1 = s.toCharArray();
        char[] chars2 = t.toCharArray();
        int flag=0;
        Map<Character,Integer> map=new HashMap<>();
        for (Character character:chars1){
            if (!map.containsKey(character)) map.put(character,1);
            else {
                Integer count = map.get(character);
                count+=1;
                map.put(character,count);
            }
        }
        for (Character character:chars2){
            if (map.containsKey(character)) {
                Integer count = map.get(character);
                count-=1;
                map.put(character,count);
            } else {
                map.put(character,1);
            }
        }
        for (Map.Entry<Character,Integer> entry:map.entrySet()) {
            if (entry.getValue()!=0) return false;
        }
        return true;
    }
}
