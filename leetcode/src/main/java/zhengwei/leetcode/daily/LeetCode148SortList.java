package zhengwei.leetcode.daily;

import zhengwei.leetcode.common.ListNode;

/**
 * 148. 排序链表
 *
 * @author zhengwei AKA zenv
 * @since 2020/11/22 8:54
 */
public class LeetCode148SortList {
    //使用第 147 题的解法再解一遍
    public ListNode sortList147(ListNode head) {
        if (null == head) return null;
        ListNode dummyNode = new ListNode(Integer.MIN_VALUE);
        dummyNode.next = head;
        //排序好的元素的最后一个
        ListNode lastSorted = head;
        //当前需要插入到有序链表中的元素
        ListNode currentInsert = lastSorted.next;
        while (currentInsert != null) {
            if (currentInsert.val >= lastSorted.val) {
                lastSorted = lastSorted.next;
            } else {
                ListNode prev = dummyNode;
                while (prev.next.val <= currentInsert.val) {
                    prev = prev.next;
                }
                lastSorted.next = currentInsert.next;
                currentInsert.next = prev.next;
                prev.next = currentInsert;
            }
            currentInsert = lastSorted.next;
        }
        return dummyNode.next;
    }

    //归并排序
    public ListNode sortListMerge(ListNode head) {
        return sortListMerge(head,null);
    }

    private ListNode sortListMerge(ListNode head, ListNode tail) {
        if (null == head) return null;
        if (head.next == tail) return head;
        //从中间位置切分链表
        //利用快慢指针寻找链表的中间节点
        //如果链表的节点个数是偶数个，那么 middle 的位置是 length / 2 的位置
        //如果链表的节点个数为奇数个，那么 middle 的位置是 length / 2 + 1 的位置
        ListNode slow = head, fast = head;
        while (fast != tail && fast.next != tail) {
            slow = slow.next;
            fast = fast.next.next;
        }
        ListNode middle = slow;
        final ListNode list1 = sortListMerge(head, middle);
        final ListNode list2 = sortListMerge(middle, tail);
        return merge(list1, list2);
    }

    //合并两个链表，使合并之后的链表有序
    private ListNode merge(ListNode list1, ListNode list2) {
        ListNode temp = new ListNode(Integer.MIN_VALUE);
        while (null != list1 && null != list2) {
            if (list1.val < list2.val) {
                temp.next = list1;
                list1 = list1.next;
            } else {
                temp.next = list2;
                list2 = list2.next;
            }
        }
        temp.next = list1 == null ? list2 : list1;
        return temp.next;
    }
}
