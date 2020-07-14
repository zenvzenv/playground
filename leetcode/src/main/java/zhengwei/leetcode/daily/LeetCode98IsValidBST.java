package zhengwei.leetcode.daily;

/**
 * LeetCode第98题：验证是否是搜索二叉树
 *
 * @author zhengwei AKA Awei
 * @since 2020/5/5 9:23
 */
public class LeetCode98IsValidBST {
    private static boolean isValidBST(TreeNode root) {
        return helper(root, null, null);
    }

    private static boolean helper(TreeNode node, Integer lower, Integer upper) {
        //如果该节点是叶子节点则返回true
        if (node == null) return true;
        int val = node.val;
        if (lower != null && val <= lower) return false;
        if (upper != null && val >= upper) return false;
        //查看右子树
        if (!helper(node.right, val, upper)) return false;
        //查看左子树
        if (!helper(node.left, lower, val)) return false;
        return true;
    }

    public static void main(String[] args) {
        /*TreeNode root = new TreeNode(5);
        TreeNode rootLeft = new TreeNode(1);
        TreeNode rootRight = new TreeNode(4);
        TreeNode rootRL = new TreeNode(3);
        TreeNode rootRR = new TreeNode(6);
        root.left = rootLeft;
        root.right = rootRight;
        rootRight.left = rootRL;
        rootRight.right = rootRR;*/
        TreeNode root = new TreeNode(2);
        root.left = new TreeNode(1);
        root.right = new TreeNode(3);
        System.out.println(isValidBST(root));
    }
}
