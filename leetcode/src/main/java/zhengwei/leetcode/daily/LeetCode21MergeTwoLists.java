package zhengwei.leetcode.daily;

import zhengwei.leetcode.common.ListNode;

/**
 * LeetCode21题：合并两个有序链表
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/29 16:49
 */
public class LeetCode21MergeTwoLists {
    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        ListNode result = new ListNode(0);
        ListNode temp = result;
        while (l1 != null && l2 != null) {
            if (l1.val > l2.val) {
                temp.next = new ListNode(l2.val);
                l2 = l2.next;
            } else {
                temp.next = new ListNode(l1.val);
                l1 = l1.next;
            }
            temp = temp.next;
        }
        temp.next = l1 == null ? l2 : l1;
        return result.next;
    }
}
