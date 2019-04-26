package zhengwei.LeetCode.Day04;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhengwei
 * @since 2019/3/1 15:56
 * LeetCode https://leetcode-cn.com/explore/featured/card/top-interview-quesitons-in-2018/261/before-you-start/1107/
 */
public class MajorityElement {
    public static int majorityElement(int[] nums) {
        Map<Integer,Integer> map=new HashMap<>();
        int flag=nums.length/2;
        int result=0;
        for(int i=0;i<nums.length;i++){
            if(null==map.get(nums[i])){
                map.put(nums[i],1);
            }else{
                int count=map.get(nums[i]);
                map.put(nums[i],count+1);
            }
        }
        for (Map.Entry<Integer,Integer> entry:map.entrySet()){
            if (entry.getValue()>flag) result=entry.getKey();
        }
        return result;
    }

    public static void main(String[] args) {
        int i = majorityElement(new int[]{3, 2, 3});
        System.out.println(i);
    }
}

