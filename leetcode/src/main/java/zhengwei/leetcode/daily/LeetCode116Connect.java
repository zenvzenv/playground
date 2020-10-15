package zhengwei.leetcode.daily;

import zhengwei.leetcode.common.Node;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 116. 填充每个节点的下一个右侧节点指针
 *
 * @author zhengwei AKA Awei
 * @since 2020/10/15 9:02
 */
public class LeetCode116Connect {
    public Node connect(Node root) {
        if (null == root) return null;
        Queue<Node> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            final int size = queue.size();
            for (int i = 0; i < size; i++) {
                final Node node = queue.poll();
                if (i < size - 1 && null != node) {
                    node.next = queue.peek();
                }
                if (null != node && null != node.left) {
                    queue.add(node.left);
                }
                if (null != node && null != node.right) {
                    queue.add(node.right);
                }
            }
        }
        return root;
    }
}
