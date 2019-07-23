package zhengwei.LeetCode.Daily;

/**
 * Given n non-negative integers a1, a2, ..., an , where each represents a point at coordinate (i, ai).
 * n vertical lines are drawn such that the two endpoints of line i is at (i, ai) and (i, 0).
 * Find two lines, which together with x-axis forms a container, such that the container contains the most water.
 * <p>
 * Note: You may not slant the container and n is at least 2.
 * <p>
 * Example:
 * <p>
 * Input: [1,8,6,2,5,4,8,3,7]
 * Output: 49
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/23 19:38
 */
public class LeetCode11ContainerWithMostWater {
	static int maxArea(int[] height) {
		if (height == null || height.length < 2) return 0;
		int area = 0;
		int left = 0;
		int right = height.length - 1;
		while (left < right) {
			area = Math.max(area, (right - left) * Math.min(height[left], height[right]));
			if (height[left] < height[right]) {
				left++;
			} else if (height[left] > height[right]) {
				right--;
			} else {
				left++;
				right--;
			}
		}
		return area;
	}
}
