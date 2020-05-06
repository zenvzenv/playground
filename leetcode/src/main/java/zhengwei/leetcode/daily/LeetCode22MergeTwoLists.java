package zhengwei.leetcode.daily;

/**
 * LeetCode第22题：合并两个链表
 *
 * @author zhengwei AKA Awei
 * @since 2020/5/1 7:24
 */
public class LeetCode22MergeTwoLists {
    private static final class ListNode {
        final int val;
        ListNode next;

        public ListNode(int val) {
            this.val = val;
        }
    }

    private static ListNode mergeTowLists(ListNode l1, ListNode l2) {
        //记录最开始节点
        ListNode head = new ListNode(-1);
        ListNode pre = head;
        while (l1 != null && l2 != null) {
            if (l1.val < l2.val) {
                pre.next = l1;
                l1 = l1.next;
            } else {
                pre.next = l2;
                l2 = l2.next;
            }
            pre = pre.next;
        }
        //最终会剩余一个ListNode中的部分数据，直接加到后面即可
        pre.next = l1 == null ? l2 : l1;
        return head.next;
    }
}
