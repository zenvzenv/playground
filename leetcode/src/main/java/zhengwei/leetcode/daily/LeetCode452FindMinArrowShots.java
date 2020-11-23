package zhengwei.leetcode.daily;

import java.util.Arrays;
import java.util.Comparator;

/**
 * 452. 用最少数量的箭引爆气球
 *
 * @author zhengwei AKA zenv
 * @since 2020/11/23 14:03
 */
public class LeetCode452FindMinArrowShots {
    public int findMinArrowShots(int[][] points) {
        if (points.length == 0) return 0;
        //按二维数组的第二列进行升序排序
        Arrays.sort(points, Comparator.comparingInt(o -> o[1]));
        int pos = points[0][1];
        int ans = 1;
        for (int[] point : points) {
            if (point[0] > pos) {
                ans++;
                pos = point[1];
            }
        }
        return ans;
    }
}
