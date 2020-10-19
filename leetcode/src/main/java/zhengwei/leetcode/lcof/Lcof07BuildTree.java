package zhengwei.leetcode.lcof;

import zhengwei.leetcode.common.TreeNode;

import java.util.Arrays;

/**
 * 剑指offer第7题：重建二叉树
 * <p>给定二叉树的前序遍历和中序遍历序列来重构二叉树<p/>
 *
 * @author zhengwei AKA Awei
 * @since 2020/6/6 14:01
 */
public class Lcof07BuildTree {
    public TreeNode buildTree1(int[] preorder, int[] inorder) {
        int length = inorder.length;
        if (preorder.length == 0 || length == 0) {
            return null;
        }
        TreeNode root = new TreeNode(preorder[0]);
        for (int i = 0; i < length; i++) {
            if (root.val == inorder[i]) {
                //Arrays.copyOfRange是左闭右开的复制数组
                int[] leftInorder = Arrays.copyOfRange(inorder, 0, i);
                int[] rightInorder = Arrays.copyOfRange(inorder, i, length);

                int[] leftPreorder = Arrays.copyOfRange(preorder, 1, i + 1);
                int[] rightPreorder = Arrays.copyOfRange(preorder, i + 1, preorder.length);

                root.left = buildTree1(leftPreorder, leftInorder);
                root.right = buildTree1(rightPreorder, rightInorder);
                break;
            }
        }
        return root;
    }
}
