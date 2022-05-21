package zhengwei.leetcode.daily;

import zhengwei.leetcode.common.TreeNode;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * 653. 两数之和 IV - 输入 BST
 *
 * @author zhengwei AKA zenv
 * @since 2022/3/22
 */
public class LeetCode653FindTarget {
    final Set<Integer> set = new HashSet<>();

    /**
     * DFS遍历二叉树，如果 set 中包含 k - root.val 的值则表示已经找到两个数，
     * 如果没找到则将当前值加入到 set 中以供后续查找。
     */
    public boolean findTargetDFS(TreeNode root, int k) {
        if (null == root) {
            return false;
        }
        if (set.contains(k - root.val)) {
            return true;
        }
        set.add(root.val);
        return findTargetDFS(root.left, k) || findTargetDFS(root.right, k);
    }



    /**
     * BFS遍历二叉树
     */
    public boolean findTargetBFS(TreeNode root, int k) {
        final Queue<TreeNode> queue = new LinkedList<>();
        final Set<Integer> set = new HashSet<>();
        if (null == root) return false;
        queue.add(root);
        while (!queue.isEmpty()) {
            final TreeNode current = queue.poll();
            if (set.contains(k - current.val)) {
                return true;
            }
            set.add(current.val);
            if (current.left != null) queue.add(current.left);
            if (current.right != null) queue.add(current.right);
        }
        return false;
    }
}
