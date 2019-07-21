package zhengwei.LeetCode.Daily;

/**
 * 判断一个整数是否是回文数。回文数是指正序（从左向右）和倒序（从右向左）读都是一样的整数。
 * <p>
 * 示例 1:
 * <p>
 * 输入: 121
 * 输出: true
 * 示例 2:
 * <p>
 * 输入: -121
 * 输出: false
 * 解释: 从左向右读, 为 -121 。 从右向左读, 为 121- 。因此它不是一个回文数。
 * 示例 3:
 * <p>
 * 输入: 10
 * 输出: false
 * 解释: 从右向左读, 为 01 。因此它不是一个回文数。
 * 进阶:
 * <p>
 * 你能不将整数转为字符串来解决这个问题吗？
 * <p>
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode-cn.com/problems/palindrome-number
 * 著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/21 17:25
 */
public class LeetCode09PalindromeNumber {
	public static void main(String[] args) {
		System.out.println(isPalindrome2(12344321));
	}

	public static boolean isPalindrome(int x) {
		int flag = x;
		if (x < 0) return false;
		if (x < 10) return true;
		int result = 0;
		while (x != 0) {
			int temp = result * 10 + x % 10;
			if ((temp - x % 10) / 10 != result) return false;
			result = temp;
			x /= 10;
		}
		return result == flag;
	}

	static boolean isPalindrome2(int x) {
		if (x < 0) return false;
		int div = 1;
		//注意临界值，如果判断的条件是>0的话，会多乘一次
		while (x / div >= 10) {
			div *= 10;
		}
		while (x != 0) {
			int high = x / div;//最高位
			int low = x % 10;//最低位
			if (high != low) return false;
			x = (x - high * div) / 10;//去掉最高位和最低位
			div /= 100;
		}
		return true;
	}
}
