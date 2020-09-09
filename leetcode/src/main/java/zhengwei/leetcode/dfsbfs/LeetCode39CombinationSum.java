package zhengwei.leetcode.dfsbfs;

import java.util.ArrayList;
import java.util.List;

/**
 * 39. 组合总和
 *
 * @author zhengwei AKA Awei
 * @since 2020/9/9 19:31
 */
public class LeetCode39CombinationSum {
    public static List<List<Integer>> combinationSum(int[] candidates, int target) {
        List<List<Integer>> res = new ArrayList<>();
        List<Integer> combine = new ArrayList<>();
        dfs(candidates, target, res, combine, 0);
        return res;
    }

    static void dfs(int[] candidates, int target, List<List<Integer>> res, List<Integer> combine, int i) {
        if (i == candidates.length) {
            return;
        }
        if (target == 0) {
            res.add(new ArrayList<>(combine));
            return;
        }
        dfs(candidates, target, res, combine, i + 1);
        if (target - candidates[i] >= 0) {
            combine.add(candidates[i]);
            dfs(candidates, target - candidates[i], res, combine, i);
            combine.remove(combine.size() - 1);
        }
    }

    public static void main(String[] args) {
        System.out.println(combinationSum(new int[]{2, 3, 6, 7}, 7));
    }
}
