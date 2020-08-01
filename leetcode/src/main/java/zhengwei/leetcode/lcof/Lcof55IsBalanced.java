package zhengwei.leetcode.lcof;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 剑指offer第55题：二叉树的最大深度和平衡二叉树
 *
 * @author zhengwei AKA Awei
 * @since 2020/8/1 15:36
 */
public class Lcof55IsBalanced {
    //dfs
    public boolean isBalanced(TreeNode root) {
        return helper(root) != -1;
    }

    private static int helper(TreeNode root) {
        if (root == null) return 0;
        int left = helper(root.left);
        if (left == -1) return -1;
        int right = helper(root.right);
        if (right == -1) return -1;
        return Math.abs(left - right) < 2 ? Math.max(left, right) + 1 : -1;
    }

    //层序遍历
    public int maxDepth(TreeNode root) {
        if (root == null) return 0;
        int res = 0;
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                final TreeNode node = queue.poll();
                if (node != null && node.left != null) queue.add(node.left);
                if (node != null && node.right != null) queue.add(node.right);
            }
            res++;
        }
        return res;
    }
}
