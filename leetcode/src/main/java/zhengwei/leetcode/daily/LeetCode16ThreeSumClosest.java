package zhengwei.leetcode.daily;

import java.util.Arrays;

/**
 * 给定一个包括 n 个整数的数组 nums 和 一个目标值 target。找出 nums 中的三个整数，使得它们的和与 target 最接近。返回这三个数的和。假定每组输入只存在唯一答案。
 * <p>
 * 例如，给定数组 nums = [-1，2，1，-4], 和 target = 1.
 * <p>
 * 与 target 最接近的三个数的和为 2. (-1 + 2 + 1 = 2).
 * <p>
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode-cn.com/problems/3sum-closest
 * 著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/27 16:58
 */
public class LeetCode16ThreeSumClosest {
	static int threeSumClosest(int[] nums, int target) {
		if (nums == null || nums.length < 3) return 0;
		Arrays.sort(nums);
		int distance = nums[0] + nums[1] + nums[2] - target;
		for (int base = 0; base < nums.length - 2; base++) {
			int left = base + 1;
			int right = nums.length - 1;
			while (left < right) {
				int newDistance = nums[base] + nums[left] + nums[right] - target;
				if (newDistance == 0) return target;
				else if (Math.abs(newDistance) < Math.abs(distance)) {
					distance = newDistance;
				}
				if (newDistance < 0) {
					left++;
				} else {
					right--;
				}
			}
		}
		return distance + target;
	}
}
