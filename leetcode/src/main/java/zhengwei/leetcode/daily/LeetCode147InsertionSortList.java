package zhengwei.leetcode.daily;

import zhengwei.leetcode.common.ListNode;

/**
 * 147. 对链表进行插入排序
 *
 * @author zhengwei AKA zenv
 * @since 2020/11/20 9:05
 */
public class LeetCode147InsertionSortList {
    public ListNode insertionSortList(ListNode head) {
        if (null == head) return null;
        ListNode dummyNode = new ListNode(Integer.MIN_VALUE);
        dummyNode.next = head;
        //排好序列表中最后一个节点
        ListNode lastSorted = head;
        //待插入的节点
        ListNode curr = head.next;
        while (curr != null) {
            if (lastSorted.val <= curr.val) {
                lastSorted = lastSorted.next;
            } else {
                ListNode prev = dummyNode;
                //查找已排序链表中小于待插入的一个节点
                while (prev.next.val <= curr.val) {
                    prev = prev.next;
                }
                //交换节点
                lastSorted.next = curr.next;
                curr.next = prev.next;
                prev.next = curr;
            }
            curr = lastSorted.next;
        }
        return dummyNode.next;
    }
}
