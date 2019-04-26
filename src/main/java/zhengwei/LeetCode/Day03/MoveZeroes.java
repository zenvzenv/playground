package zhengwei.LeetCode.Day03;

/**
 * @author zhengwei
 * LeetCode https://leetcode-cn.com/explore/interview/card/top-interview-questions-easy/1/array/28/
 */
public class MoveZeroes {
    public void moveZeroes(int[] nums) {
        if (nums==null || nums.length==0) return;
        //记录非0元素开始位置
        int k=0;
        for (int i=0;i<nums.length;i++){
            if (nums[i]!=0){
                nums[k++]=nums[i];
            }
        }
        while (k<nums.length){
            nums[k]=0;
            k++;
        }
    }
}
