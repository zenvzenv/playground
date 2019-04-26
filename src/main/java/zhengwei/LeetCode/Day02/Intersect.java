package zhengwei.LeetCode.Day02;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhengwei
 * LeetCode https://leetcode-cn.com/explore/interview/card/top-interview-questions-easy/1/array/26/
 */
public class Intersect {
    public int[] intersect(int[] nums1, int[] nums2) {
        Map<Integer,Integer> map=new HashMap<>();
        List<Integer> list=new ArrayList<>();
        for (int i=0;i<nums1.length;i++){
            if(map.containsKey(nums1[i])){
                map.put(nums1[i],map.get(nums1[i])+1);
            }else{
                map.put(nums1[i],1);
            }
        }
        for (int i=0;i<nums2.length;i++){
            if (map.containsKey(nums2[i])){
                if(map.get(nums2[i])>0){
                    map.put(nums2[i],map.get(nums2[i])-1);
                    list.add(nums2[i]);
                }
            }
        }
        int[] result=new int[list.size()];
        for (int i=0;i<result.length;i++){
            result[i]=list.get(i);
        }
        return result;
    }
}
