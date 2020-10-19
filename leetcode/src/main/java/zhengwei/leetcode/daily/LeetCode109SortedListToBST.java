package zhengwei.leetcode.daily;

import zhengwei.leetcode.common.ListNode;
import zhengwei.leetcode.common.TreeNode;

/**
 * 109. 有序链表转换二叉搜索树
 *
 * @author zhengwei AKA Awei
 * @since 2020/8/18 17:08
 */
public class LeetCode109SortedListToBST {
    public static TreeNode sortedListToBST(ListNode head) {
        return buildBST(head);
    }

    private static ListNode middleNode(ListNode head) {
        ListNode slow = head, fast = head, pre = null;
        while (null != fast && null != fast.next) {
            pre = slow;
            slow = slow.next;
            fast = fast.next.next;
        }
        //截断链表
        if (pre != null) {
            pre.next = null;
        }
        return slow;
    }

    private static TreeNode buildBST(ListNode head) {
        if (null == head) return null;
        //如果是最后一个节点，直接返回一个树节点
        if (head.next == null) return new TreeNode(head.val);
        final ListNode middleNode = middleNode(head);
        TreeNode root = new TreeNode(middleNode.val);
        root.left = buildBST(head);
        root.right = buildBST(middleNode.next);
        return root;
    }

    public static void main(String[] args) {
        System.out.println(sortedListToBST(ListNode.genListNode(new int[]{-10, -3, 0, 5, 9})));
    }
}
