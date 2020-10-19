package zhengwei.leetcode.daily;

import zhengwei.leetcode.common.TreeNode;

import java.util.*;

/**
 * LeetCode第199题：二叉树的右视图
 *
 * @author zhengwei AKA Awei
 * @since 2020/4/22 9:23
 */
public class LeetCode199RightSideView {
    /**
     * bfs
     * 广度优先遍历，记录每一层的最后一个元素</p>
     * 基本思路：首先将根节点入队，遍历该节点下所有的子节点并且将子节点入队，
     */
    private static List<Integer> rightSideViewBFS(TreeNode root) {
        List<Integer> res = new ArrayList<>();
        if (root == null) {
            return res;
        }
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            //父节点有多少个子节点
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                final TreeNode node = queue.poll();
                //总是先查看当前节点的左节点，然后入队，这样排在队列后面的就是右节点了
                if (Objects.requireNonNull(node).left != null) {
                    queue.offer(node.left);
                }
                //然后再查看该节点的右节点，
                if (node.right != null) {
                    queue.offer(node.right);
                }
                //这个判断比较关键，用来判断一层中最右边的节点
                //size是父节点的所有子节点的个数
                //i->代表父节点的的所有子节点的最大索引值，即最右边的节点
                if (i == size - 1) {
                    //将当前的最后一个元素加入到结果列表中
                    res.add(node.val);
                }
            }
        }
        return res;
    }

    /**
     * dfs
     * 深度优先遍历
     */
    private static List<Integer> rightSideViewDFS(TreeNode root) {
        List<Integer> res = new ArrayList<>();
        if (null == root) return res;
        dfs(root, 0, res);
        return res;
    }

    private static void dfs(TreeNode root, int depth, List<Integer> res) {
        //递归结束条件，当该节点为null时，说明此时已经到了这条分支的最后位置了，返回即可
        if (null == root) return;
        //res的size可以侧面反映出树的深度
        //因为总是先访问右节点，只要该深度没有没有节点就把当前节点加入即可
        if (depth == res.size()) res.add(root.val);
        depth++;
        //总是先访问右子节点
        dfs(root.right, depth, res);
        dfs(root.left, depth, res);
    }

    public static void main(String[] args) {
        TreeNode root = new TreeNode(1);
        final TreeNode left1 = new TreeNode(2);
        root.left = left1;
        final TreeNode right1 = new TreeNode(3);
        root.right = right1;
        left1.right = new TreeNode(5);
        right1.right = new TreeNode(4);
        System.out.println(rightSideViewBFS(root));
    }
}
