package zhengwei.LeetCode.Day01;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhengwei
 * LeetCode https://leetcode-cn.com/explore/interview/card/top-interview-questions-easy/1/array/29/
 */
public class TwoSum {
    public int[] twoSum(int[] nums, int target) {
        Map<Integer,Integer> map=new HashMap<>();
        for (int i=0;i<nums.length;i++){
            int c=target-nums[i];
            if (map.containsKey(c)){
                return new int[] {map.get(c),i};
            }
            map.put(nums[i],i);
        }
        throw new IllegalArgumentException("No two sum solution");
    }
}
