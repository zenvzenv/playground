package zhengwei.leetcode.daily;

/**
 * LeetCode第七题
 * 给出一个 32 位的有符号整数，你需要将这个整数中每位上的数字进行反转。
 * <p>
 * 示例 1:
 * <p>
 * 输入: 123
 * 输出: 321
 *  示例 2:
 * <p>
 * 输入: -123
 * 输出: -321
 * 示例 3:
 * <p>
 * 输入: 120
 * 输出: 21
 * 注意:
 * <p>
 * 假设我们的环境只能存储得下 32 位的有符号整数，则其数值范围为 [−2^31,  2^31 − 1]。请根据这个假设，如果反转后整数溢出那么就返回 0。
 * <p>
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode-cn.com/problems/reverse-integer
 * 著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/20 20:17
 */
public class LeetCode07ReverseInteger {
	public int reverse(int x) {
		int result = 0;
		while (x != 0) {
			int newResult = result * 10 + x % 10;
			//校验是否Integer越界
			if ((newResult - x % 10) / 10 != result) return 0;
			//更新result的值
			result = newResult;
			x /= 10;
		}
		return result;
	}
}
