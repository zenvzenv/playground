package zhengwei.leetcode.daily;

import zhengwei.leetcode.common.ListNode;

/**
 * 239. 滑动窗口最大值
 *
 * @author zhengwei AKA zenv
 * @since 2021/1/3 11:08
 */
public class LeetCode86Partition {
    public ListNode partition(ListNode head, int x) {
        if (null == head) return null;
        ListNode small = new ListNode(-1);
        ListNode smallHead = small;
        ListNode large = new ListNode(-1);
        ListNode largeHead = large;
        while (null != head) {
            if (head.val < x) {
                small.next = head;
                small = small.next;
            } else {
                large.next = head;
                large = large.next;
            }
            head = head.next;
        }
        //防止循环链表
        large.next = null;
        small.next = largeHead.next;
        return smallHead.next;
    }
}
