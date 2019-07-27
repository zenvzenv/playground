package zhengwei.LeetCode.Daily;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 给定一个包含 n 个整数的数组 nums，判断 nums 中是否存在三个元素 a，b，c ，使得 a + b + c = 0 ？找出所有满足条件且不重复的三元组。
 * <p>
 * 注意：答案中不可以包含重复的三元组。
 * <p>
 * 例如, 给定数组 nums = [-1, 0, 1, 2, -1, -4]，
 * <p>
 * 满足要求的三元组集合为：
 * [
 * [-1, 0, 1],
 * [-1, -1, 2]
 * ]
 * <p>
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode-cn.com/problems/3sum
 * 著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/26 20:21
 */
public class LeetCode13ThreeSum {
	public List<List<Integer>> threeSum(int[] num) {
		List<List<Integer>> result = new ArrayList<>();
		if (num == null || num.length < 3) return result;
		int baseIndex = 0;
		//因为是无序数组，需要先对数据进行排序
		Arrays.sort(num);
		while (baseIndex < num.length - 2) {
			int base = num[baseIndex];
			int left = baseIndex + 1;
			int right = num.length - 1;
			while (left < right) {
				int sum = base + num[left] + num[right];
				//如果三个元素的和等于0，即满足要求，加到list集合中
				if (sum == 0) {
					List<Integer> list = new ArrayList<>();
					list.add(base);
					list.add(num[left]);
					list.add(num[right]);
					result.add(list);
					//需要注意，这里传的是左索引的下一个索引，因为如果传left索引的话，如果判断满足条件了，那么返回的还是left索引值，没有变化
					left = moveRight(num, left + 1);
					//需要注意，这里传的是右索引的上一个索引，原因同上
					right = moveLeft(num, right - 1);
				} else if (sum > 0) {
					right = moveLeft(num, right - 1);
				} else {
					left = moveRight(num, left + 1);
				}
			}
			baseIndex = moveRight(num, baseIndex + 1);
		}
		return result;
	}

	/**
	 * 左指针向右移动
	 *
	 * @param num  数组
	 * @param left 左指针
	 * @return 左指针满足条件的索引位置
	 */
	private int moveRight(int[] num, int left) {
		while (left == 0 || (left < num.length && num[left] == num[left - 1])) {
			left++;
		}
		return left;
	}

	/**
	 * 右指针向左移动
	 *
	 * @param num   数组
	 * @param right 右指针
	 * @return 右指针满足条件的索引位置
	 */
	private int moveLeft(int[] num, int right) {
		while (right == num.length - 1 || (right >= 0 && num[right] == num[right + 1])) {
			right--;
		}
		return right;
	}
}
