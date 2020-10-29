package zhengwei.leetcode.daily;

import zhengwei.leetcode.common.TreeNode;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 129. 求根到叶子节点数字之和
 *
 * @author zhengwei AKA Awei
 * @since 2020/10/29 9:04
 */
public class LeetCode129SumNumbers {
    public int sumNumbersBFS(TreeNode root) {
        if (null == root) return 0;
        final Queue<TreeNode> nodeQueue = new LinkedList<>();
        final Queue<Integer> numQueue = new LinkedList<>();
        nodeQueue.add(root);
        numQueue.add(root.val);
        int ans = 0;
        while (!nodeQueue.isEmpty()) {
            final TreeNode node = nodeQueue.poll();
            final Integer num = numQueue.poll();
            if (node.left == null && node.right == null) {
                ans += num;
            } else {
                if (node.left != null) {
                    nodeQueue.add(node.left);
                    numQueue.add(num * 10 + node.left.val);
                }
                if (node.right != null) {
                    nodeQueue.add(node.right);
                    numQueue.add(num * 10 + node.right.val);
                }
            }
        }
        return ans;
    }

    public int sumNumbersDFS(TreeNode root) {
        return dfs(root, 0);
    }

    public int dfs(TreeNode node, int preSum) {
        if (null == node) return 0;
        int tempSum = preSum * 10 + node.val;
        if (node.left == null && node.right == null) {
            return tempSum;
        } else {
            return dfs(node.left, tempSum) + dfs(node.right, tempSum);
        }
    }
}
