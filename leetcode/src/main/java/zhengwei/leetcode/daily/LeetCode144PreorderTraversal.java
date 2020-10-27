package zhengwei.leetcode.daily;

import zhengwei.leetcode.common.TreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 144. 二叉树的前序遍历
 *
 * @author zhengwei AKA Awei
 * @since 2020/10/27 9:08
 */
public class LeetCode144PreorderTraversal {
    public List<Integer> preorderTraversal(TreeNode root) {
        List<Integer> ans = new ArrayList<>();
        dfs(ans, root);
        return ans;
    }

    public void dfs(List<Integer> ans, TreeNode node) {
        if (null == node) return;
        ans.add(node.val);
        dfs(ans, node.left);
        dfs(ans, node.right);
    }

    public List<Integer> preorderTraversalStack(TreeNode root) {
        if (null == root) return new ArrayList<>(0);
        List<Integer> ans = new ArrayList<>();
        Stack<TreeNode> stack = new Stack<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            final TreeNode node = stack.pop();
            ans.add(node.val);
            if (null != node.right) {
                stack.push(node.right);
            }
            if (null != node.left) {
                stack.push(node.left);
            }
        }
        return ans;
    }
}
