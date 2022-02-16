package zhengwei.leetcode.lcof;

import zhengwei.leetcode.common.ListNode;

import java.util.Stack;

/**
 * 剑指offer第24题：反转链表
 *
 * @author zhengwei AKA Awei
 * @since 2020/8/27 16:08
 */
public class Lcof24ReverseList {
    public static ListNode reverseList1(ListNode head) {
        Stack<ListNode> stack = new Stack<>();
        while (head != null) {
            stack.push(head);
            head = head.next;
        }
        ListNode res = new ListNode(-1);
        ListNode dummy = res;
        while (!stack.isEmpty()) {
            dummy.next = stack.pop();
            dummy = dummy.next;
        }
        dummy.next = null;
        return res.next;
    }

    public static ListNode reverseList2(ListNode head) {
        if (head == null) return null;
        ListNode pre = null;
        while (head != null) {
            final ListNode next = head.next;
            head.next = pre;
            pre = head;
            head = next;
        }
        return pre;
    }

    public static void main(String[] args) {
//        System.out.println(reverseList1(ListNode.genListNode(new int[]{1, 2, 3, 4, 5})));
        System.out.println(reverseList2(ListNode.genListNode(new int[]{1, 2, 3, 4, 5})));
    }
}
