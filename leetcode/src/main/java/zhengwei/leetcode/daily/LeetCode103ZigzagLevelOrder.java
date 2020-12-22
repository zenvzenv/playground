package zhengwei.leetcode.daily;

import zhengwei.leetcode.common.TreeNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 103. 二叉树的锯齿形层序遍历
 *
 * @author zhengwei AKA zenv
 * @since 2020/12/22 9:04
 */
public class LeetCode103ZigzagLevelOrder {
    public List<List<Integer>> zigzagLevelOrder(TreeNode root) {
        if (null == root) return new ArrayList<>(0);
        final List<List<Integer>> result = new ArrayList<>();
        final Deque<TreeNode> deque = new ArrayDeque<>();
        deque.offerLast(root);
        //true-从左往右，false-从右往左
        boolean flag = true;
        while (!deque.isEmpty()) {
            final int size = deque.size();
            List<Integer> list = new ArrayList<>();
            if (flag) {
                for (int i = 0; i < size; i++) {
                    final TreeNode last = deque.pollLast();
                    if (null == last) continue;
                    list.add(last.val);
                    if (last.left != null) deque.offerFirst(last.left);
                    if (last.right != null) deque.offerFirst(last.right);
                }
                flag = false;
            } else {
                for (int i = 0; i < size; i++) {
                    final TreeNode first = deque.pollFirst();
                    if (null == first) continue;
                    list.add(first.val);
                    if (first.right != null) deque.offerLast(first.right);
                    if (first.left != null) deque.offerLast(first.left);
                }
                flag = true;
            }
            result.add(list);
        }
        return result;
    }
}
