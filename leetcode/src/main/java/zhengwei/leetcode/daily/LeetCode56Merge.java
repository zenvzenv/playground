package zhengwei.leetcode.daily;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author zhengwei AKA Awei
 * @since 2020/4/16 20:30
 */
public class LeetCode56Merge {
    private static int[][] merge(int[][] intervals) {
        int len = intervals.length;
        if (len < 2) {
            return intervals;
        }
        //按照起点排序
        Arrays.sort(intervals, Comparator.comparingInt(o -> o[0]));
        List<int[]> res = new ArrayList<>();
        res.add(intervals[0]);
        for (int i = 1; i < len; i++) {
            final int[] currInterval = intervals[i];
            final int[] peek = res.get(res.size() - 1);
            if (currInterval[0] > peek[1]) {
                res.add(currInterval);
            } else {
                peek[1] = Math.max(currInterval[1], peek[1]);
            }
        }
        return res.toArray(new int[res.size()][]);
    }

    public static void main(String[] args) {
        int[][] arr = {{5, 7, 9}, {12, 14, 16, 18}, {23, 25, 36, 47}, {22, 54, 65, 15}, {22, 34}};

        for (int[] value : arr) {
            System.out.println(Arrays.toString(value));
        }
        System.out.println("================");
        Arrays.sort(arr, Comparator.comparingInt(o -> o[0]));
        for (int[] ints : arr) {
            System.out.println(Arrays.toString(ints));
        }
    }
}
