package zhengwei.leetcode.dfsbfs;

/**
 * LeetCode的常用数据结构：二叉树节点
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/7 19:27
 */
public class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;

    TreeNode(int x) {
        val = x;
    }

    public TreeNode(int val, TreeNode left, TreeNode right) {
        this.val = val;
        this.left = left;
        this.right = right;
    }
}
