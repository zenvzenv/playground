package zhengwei.leetcode.daily;

import zhengwei.leetcode.common.TreeNode;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 222. 完全二叉树的节点个数
 *
 * @author zhengwei AKA zenv
 * @since 2020/11/24 8:59
 */
public class LeetCode222CountNodes {
    public int countNodesBFS(TreeNode root) {
        if (null == root) return 0;
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        int ans = 1;
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                final TreeNode poll = queue.poll();
                if (poll != null && poll.left != null) {
                    queue.add(poll.left);
                    ans++;
                }
                if (poll != null && poll.right != null) {
                    queue.add(poll.right);
                    ans++;
                }
            }
        }
        return ans;
    }

    public int countNodesDFS(TreeNode root) {
        if (null==root) return 0;
        return 1+countNodesDFS(root.left)+countNodesDFS(root.right);
    }
}
