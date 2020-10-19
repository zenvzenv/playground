package zhengwei.leetcode.daily;

import zhengwei.leetcode.common.TreeNode;

import java.util.LinkedList;
import java.util.Queue;

/**
 * LeetCode第104题：二叉树的最大深度
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/28 9:28
 */
public class LeetCode104MaxDepth {
    //bfs
    public int maxDepth1(TreeNode root) {
        if (root == null) return 0;
        int res = 0;
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            int size = queue.size();
            res++;
            for (int i = 0; i < size; i++) {
                TreeNode node = queue.poll();
                if (node != null && node.left != null) queue.add(node.left);
                if (node != null && node.right != null) queue.add(node.right);
            }
        }
        return res;
    }

    //dfs
    public int maxDepth2(TreeNode root) {
        //递归左右子树得到最大树深度
        return root == null ? 0 : Math.max(maxDepth2(root.left), maxDepth2(root.right)) + 1;
    }
}
