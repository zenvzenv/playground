package zhengwei.leetcode.daily;

import java.util.LinkedList;

/**
 * LeetCode第112题：路径总和
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/7 19:27
 */
public class LeetCode112HasPathSum {
    //recursive
    private static boolean hasPathSum(TreeNode root, int sum) {
        if (root == null) {
            return false;
        }
        //如果是叶子节点
        if (root.left == null && root.right == null) {
            return sum == root.val;
        }
        return hasPathSum(root.left, sum - root.val) || hasPathSum(root.right, sum - root.val);
    }

    public static void main(String[] args) {
        TreeNode root = new TreeNode(1);
        root.left = new TreeNode(2);
        System.out.println(hasPathSum2(root, 2));
    }

    //层序遍历
    private static boolean hasPathSum2(TreeNode root, int sum) {
        if (root == null) {
            return false;
        }
        LinkedList<TreeNode> nodeLinkedList = new LinkedList<>();
        LinkedList<Integer> sumLinkedList = new LinkedList<>();
        nodeLinkedList.add(root);
        sumLinkedList.add(root.val);
        while (!nodeLinkedList.isEmpty()) {
            final TreeNode node = nodeLinkedList.poll();
            final int temp = sumLinkedList.poll();
            //因为是要求根节点到叶子节点
            if (node.left == null && node.right == null) {
                if (temp == sum) {
                    return true;
                }
                continue;
            }
            if (node.left != null) {
                nodeLinkedList.add(node.left);
                sumLinkedList.add(temp + node.left.val);
            }
            if (node.right != null) {
                nodeLinkedList.add(node.right);
                sumLinkedList.add(temp + node.right.val);
            }
        }
        return false;
    }
}
