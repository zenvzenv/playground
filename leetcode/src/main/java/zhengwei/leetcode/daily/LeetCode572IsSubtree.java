package zhengwei.leetcode.daily;

/**
 * LeetCode第572题：另一个数的子树
 *
 * @author zhengwei AKA Awei
 * @since 2020/5/7 14:09
 */
public class LeetCode572IsSubtree {
    private static final class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        public TreeNode(int val) {
            this.val = val;
        }
    }

    private static boolean isSubtree(TreeNode s, TreeNode t) {
        //如果两个树相同，那么t肯定是s的子树
        //s的左右子树是否和t相同
        return isSameTree(s, t) || isSubtree(s.left, t) || isSubtree(s.right, t);
    }

    //判断两个数是否相同
    private static boolean isSameTree(TreeNode s, TreeNode t) {
        if (s == null && t == null) return true;
        if (s == null || t == null) return false;
        return s.val == t.val && isSameTree(s.left, t.left) && isSameTree(s.right, s.left);
    }
}
