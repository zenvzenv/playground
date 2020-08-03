package zhengwei.leetcode.daily;

/**
 * LeetCode第415题：字符串相加
 *
 * @author zhengwei AKA Awei
 * @since 2020/8/3 10:15
 */
public class LeetCode415AddStrings {
    public static String addStrings(String num1, String num2) {
        int i = num1.length() - 1, j = num2.length() - 1, carry = 0;
        StringBuilder res = new StringBuilder();
        while (i >= 0 || j >= 0) {
            int a = i >= 0 ? num1.charAt(i) - '0' : 0;
            int b = j >= 0 ? num2.charAt(j) - '0' : 0;
            int sum = a + b + carry;
            carry = sum / 10;
            res.append(sum % 10);
            i--;
            j--;
        }
        if (carry != 0) res.append(carry);
        return res.reverse().toString();
    }

    public static void main(String[] args) {
        System.out.println(addStrings("434324", "3424"));
    }
}
