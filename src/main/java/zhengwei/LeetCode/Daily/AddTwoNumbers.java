package zhengwei.LeetCode.Daily;

import java.util.Stack;

/**
 * LeetCode第445题：两数相加</p>
 * 利用栈来辅助解题，因为栈是先进后出，所以一个数字ListNode链表先入栈的是高位，后入站的是低位，然后依次出栈就能实现由低位向高位相加
 * 需要注意的是两个数字相加有进位的情况.
 *
 * @author zhengwei AKA Awei
 * @since 2020/4/14 13:08
 */
public class AddTwoNumbers {
    private static final class ListNode {
        int val;
        ListNode next;

        ListNode(int x) {
            this.val = x;
        }
    }

    private static ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        Stack<Integer> stack1 = new Stack<>();
        Stack<Integer> stack2 = new Stack<>();
        while (l1 != null) {
            stack1.push(l1.val);
            l1 = l1.next;
        }
        while (l2 != null) {
            stack2.push(l2.val);
            l2 = l2.next;
        }
        //相加之后进位的数
        int carry = 0;
        ListNode head = null;
        while (!stack1.isEmpty() || !stack2.isEmpty() || carry > 0) {
            int sum = carry;
            final int a = stack1.isEmpty() ? 0 : stack1.pop();
            final int b = stack2.isEmpty() ? 0 : stack2.pop();
            sum += a;
            sum += b;
            final int currNum = sum % 10;
            ListNode node = new ListNode(currNum);
            node.next = head;
            head = node;
            carry = sum / 10;
        }
        return head;
    }
}
