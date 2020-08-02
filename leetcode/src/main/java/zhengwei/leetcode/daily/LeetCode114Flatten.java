package zhengwei.leetcode.daily;

import java.util.Stack;

/**
 * LeetCode第114题：二叉树展开为链表
 *
 * @author zhengwei AKA Awei
 * @since 2020/8/2 16:39
 */
public class LeetCode114Flatten {
    //前序遍历
    public void flatten(TreeNode root) {
        if (null == root) return;
        Stack<TreeNode> stack = new Stack<>();
        stack.push(root);
        TreeNode pre = null;
        while (!stack.isEmpty()) {
            final TreeNode curr = stack.pop();
            if (pre != null) {
                pre.left = null;
                pre.right = curr;
            }
            if (curr.right != null) stack.push(curr.right);
            if (curr.left != null) stack.push(curr.left);
            pre = curr;
        }
    }
}
