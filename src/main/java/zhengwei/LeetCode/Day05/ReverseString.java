package zhengwei.LeetCode.Day05;

/**
 * @author zhengwei AKA DG
 * @since 2019/4/2 9:28
 * LeetCode https://leetcode-cn.com/explore/interview/card/top-interview-questions-easy/5/strings/32/
 */
public class ReverseString {
    public void reverseString(char[] s) {
        int length=s.length;
        int middle=length>>1;
        for (int i=0;i<middle;i++){
            char temp=s[i];
            s[i]=s[length-1-i];
            s[length-1-i]=temp;
        }
    }

    public static void main(String[] args) {
        System.out.println(2>>1);
    }
}
