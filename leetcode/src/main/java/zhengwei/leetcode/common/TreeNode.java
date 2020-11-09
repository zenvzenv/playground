package zhengwei.leetcode.common;

/**
 * LeetCode 二叉树节点
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/7 19:27
 */
public final class TreeNode {
    public int val;
    public TreeNode left;
    public TreeNode right;

    public TreeNode(int x) {
        val = x;
    }

    public TreeNode(int val, TreeNode left, TreeNode right) {
        this.val = val;
        this.left = left;
        this.right = right;
    }
}
