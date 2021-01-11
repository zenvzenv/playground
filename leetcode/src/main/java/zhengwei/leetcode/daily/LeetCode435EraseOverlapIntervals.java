package zhengwei.leetcode.daily;

import java.util.Arrays;
import java.util.Comparator;

/**
 * 435. 无重叠区间435. 无重叠区间
 *
 * @author zhengwei AKA zenv
 * @since 2020/12/31 9:16
 */
public class LeetCode435EraseOverlapIntervals {
    public static int eraseOverlapIntervals(int[][] intervals) {
        if (intervals.length == 0) return 0;
        final int len = intervals.length;
        //用结束节点排序
        Arrays.sort(intervals, Comparator.comparing(o -> o[1]));
        //至少存在一个不相交的区间
        int result = 1;
        int end = intervals[0][1];
        for (int[] interval : intervals) {
            if (interval[0] >= end) {
                result++;
                end = interval[1];
            }
        }
        return len - result;
    }

    public static void main(String[] args) {
        eraseOverlapIntervals(new int[][]{{1, 2}, {1, 2}, {1, 2}});
    }
}
