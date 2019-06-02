package zhengwei.LeetCode.Daily;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * LeetCode第一题-20190602-每日一题正式启动-加油！！！
 * https://leetcode-cn.com/problems/two-sum/
 * 给定一个整数数组 nums 和一个目标值 target，请你在该数组中找出和为目标值的那两个整数，并返回他们的数组下标。
 * 你可以假设每种输入只会对应一个答案。但是，你不能重复利用这个数组中同样的元素。
 * 示例：
 *      给定 nums = [2, 7, 11, 15], target = 9
 *      因为 nums[0] + nums[1] = 2 + 7 = 9
 *      所以返回 [0, 1]
 * @author zhengwei AKA Sherlock
 * @since 2019/6/2 8:16
 */
public class LeetCode01TwoSum {
    public static void main(String[] args) {
        int[] nums=new int[]{2,7,11,15};
        int[] result = twoSum(nums, 9);
        System.out.println(Arrays.toString(result));
    }
    /**
     * 思想：利用map来存储数组中的数和索引位置
     * 遍历数组，取出第一个数firstNum，就用target减去firstNum得到第二个数secondNum，然后再去map里面看secondNum存不存在
     * 如果存在则去除对应的索引值，若不存在则把firstNum放入map，以便下一次的遍历
     * 时间复杂度是O(n),空间复杂度是O(n)
     * @param nums 初始数组
     * @param target 目标数
     * @return 两个数的索引位置
     */
    static int[] twoSum(int[] nums,int target){
        int[] result=new int[2];
        if (nums.length==0) return result;
        Map<Integer,Integer> map=new HashMap<>();
        for (int i=0;i<nums.length;i++){
            int firstNum = nums[i];
            int secondNum = target - firstNum;
            if (map.containsKey(secondNum)){
                result[0]=i;
                result[1]=map.get(secondNum);
            } else map.put(firstNum,i);
        }
        return result;
    }
}
