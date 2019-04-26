package zhengwei.LeetCode.Day03;

/**
 * @author zhengwei
 * LeetCode https://leetcode-cn.com/explore/interview/card/top-interview-questions-easy/1/array/27/
 */
public class PlusOne {
    public int[] plusOne(int[] digits) {
        for (int i=digits.length-1;i>=0;i--){
            if (digits[i]<9){
                digits[i]=digits[i]++;
                return digits;
            }
            digits[i]=0;
        }
        int[] result=new int[digits.length+1];
        result[0]=1;
        return result;
    }
}
