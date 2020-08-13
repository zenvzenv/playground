package zhengwei.leetcode.daily;

/**
 * 43. 字符串相乘
 *
 * @author zhengwei AKA Awei
 * @since 2020/8/13 12:49
 */
public class LeetCode43Multiply {
    public static String multiply(String num1, String num2) {
        StringBuilder sum = new StringBuilder("0");
        StringBuilder temp;
        for (int i = num1.length() - 1; i >= 0; i--) {
            temp = getProduct(num1.charAt(i), num2, num1.length() - 1 - i);
            sum = addTwoStrSum(sum, temp);
        }
        String res = sum.toString();
        return res.charAt(0) == '0' ? "0" : res;
    }

    private static StringBuilder addTwoStrSum(StringBuilder s1, StringBuilder s2) {
        StringBuilder res = new StringBuilder();
        int i = s1.length() - 1, j = s2.length() - 1;
        int carry = 0;
        while (i >= 0 || j >= 0) {
            int n1 = i >= 0 ? (s1.charAt(i) - '0') : 0;
            int n2 = j >= 0 ? (s2.charAt(j) - '0') : 0;
            int sum = n1 + n2 + carry;
            res.append(sum % 10);
            carry = sum / 10;
            i--;
            j--;
        }
        if (carry != 0) res.append(carry);
        return res.reverse();
    }

    private static StringBuilder getProduct(char c, String num2, int flag) {
        int n1 = c - '0';
        int carry = 0;
        StringBuilder res = new StringBuilder();
        for (int i = num2.length() - 1; i >= 0; i--) {
            int n2 = num2.charAt(i) - '0';
            int product = n1 * n2 + carry;
            res.append((product) % 10);
            carry = product / 10;
        }
        if (carry != 0) res.append(carry);
        res = res.reverse();
        for (int i = 0; i < flag; i++) {
            res.append(0);
        }
        return res;
    }

    public static void main(String[] args) {
//        System.out.println(getProduct('3', "234", 0));
        System.out.println(multiply("123", "456"));
    }
}
