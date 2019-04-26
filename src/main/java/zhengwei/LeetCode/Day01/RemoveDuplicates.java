package zhengwei.LeetCode.Day01;

/**
 * @author zhengwei
 * LeetCode https://leetcode-cn.com/explore/interview/card/top-interview-questions-easy/1/array/21/
 */
public class RemoveDuplicates {
    public int removeDuplicates(int[] nums) {
        int count=0;
        if (null==nums){
            return 0;
        }
        if (nums.length==1){
            return 1;
        }
        for(int i=0;i<nums.length;i++){
            if (i>0){
                if (nums[i]!=nums[count]){
                    count ++;
                    nums[count]=nums[i];
                }
            }
        }
        return count+1;
    }
}
