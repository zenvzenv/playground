package zhengwei.leetcode.dfsbfs;


import zhengwei.leetcode.common.TreeNode;

/**
 * 111. 二叉树的最小深度
 *
 * @author zhengwei AKA Awei
 * @since 2020/8/2 16:46
 */
public class LeetCode111MinDepth {
    public int minDepth(TreeNode root) {
        if (root == null) return 0;
        int left = minDepth(root.left);
        int right = minDepth(root.right);
        //如果左子树或右子树为空，直接返回left + right + 1因为其中有一个值为0，再加上1得到当前节点所在深度
        if (root.left == null || root.right == null) return left + right + 1;
        return Math.min(left, right) + 1;
    }
}
