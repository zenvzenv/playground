package zhengwei.leetcode.daily;

import zhengwei.leetcode.common.TreeNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 257. 二叉树的所有路径
 *
 * @author zhengwei AKA Awei
 * @since 2020/9/4 9:18
 */
public class LeetCode257BinaryTreePaths {
    public static List<String> binaryTreePathsBFS(TreeNode root) {
        List<String> res = new ArrayList<>();
        if (root == null) return res;
        Queue<TreeNode> queue = new LinkedList<>();
        Queue<String> paths = new LinkedList<>();
        queue.offer(root);
        paths.offer(String.valueOf(root.val));
        while (!queue.isEmpty()) {
            final TreeNode node = queue.poll();
            final String path = paths.poll();
            if (node.left == null && node.right == null) {
                res.add(path);
            }
            if (node.left != null) {
                queue.offer(node.left);
                paths.offer(path + "->" + node.left.val);
            }
            if (node.right != null) {
                queue.offer(node.right);
                paths.offer(path + "->" + node.right.val);
            }
        }
        return res;
    }

    public static void main(String[] args) {
        TreeNode root = new TreeNode(1);
        TreeNode r = new TreeNode(2);
        r.right = new TreeNode(4);
        TreeNode l = new TreeNode(3);
        l.left = new TreeNode(5);
        root.right = r;
        root.left = l;
        System.out.println(binaryTreePathsBFS(root));
    }
}
