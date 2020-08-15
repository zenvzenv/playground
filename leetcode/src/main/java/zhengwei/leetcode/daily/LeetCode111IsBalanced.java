package zhengwei.leetcode.daily;

/**
 * LeetCode第111题：平衡二叉树
 *
 * @author zhengwei AKA Awei
 * @since 2020/8/11 18:57
 */
public class LeetCode111IsBalanced {
    public static boolean isBalanced(TreeNode root) {
        return dfs(root) != -1;
    }

    private static int dfs(TreeNode node) {
        if (null == node) return 0;
        int left = dfs(node.left);
        if (left == -1) return -1;
        int right = dfs(node.right);
        if (right == -1) return -1;
        return Math.abs(left - right) < 2 ? Math.max(left, right) : -1;
    }

    public static void main(String[] args) {
        TreeNode root = new TreeNode(1);
        TreeNode l = new TreeNode(2);
        TreeNode r = new TreeNode(2);
        root.left = l;
        root.right = r;
        TreeNode ll = new TreeNode(3);
        TreeNode lll = new TreeNode(4);
        l.left = ll;
        ll.left = lll;
        TreeNode rr = new TreeNode(3);
        TreeNode rrr = new TreeNode(4);
        r.right = rr;
        rr.right = rrr;
        System.out.println(isBalanced(root));
    }
}
