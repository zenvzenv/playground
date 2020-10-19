package zhengwei.leetcode.daily;

import zhengwei.leetcode.common.ListNode;

/**
 * LeetCode第二题-20190603
 * https://leetcode-cn.com/problems/add-two-numbers/
 * 给出两个 非空 的链表用来表示两个非负的整数。其中，它们各自的位数是按照 逆序 的方式存储的，并且它们的每个节点只能存储 一位 数字。
 * 如果，我们将这两个数相加起来，则会返回一个新的链表来表示它们的和。
 * 您可以假设除了数字 0 之外，这两个数都不会以 0 开头。
 * 输入：(2 -> 4 -> 3) + (5 -> 6 -> 4)
 * 输出：7 -> 0 -> 8
 * 原因：342 + 465 = 807
 * @author zhengwei AKA Sherlock
 * @since 2019/6/3 9:00
 */
public class LeetCode02AddTwoNumbers {
    public static void main(String[] args) {
        ListNode l1=new ListNode(2);
        l1.next=new ListNode(4);
        l1=l1.next;
        l1.next=new ListNode(3);
        ListNode l2=new ListNode(5);
        l2.next=new ListNode(6);
        l2=l2.next;
        l2.next=new ListNode(4);
        System.out.println(addTwoNumbers(l1,l2));
    }

    /**
     * 思想：
     * 一个公式：当前节点的值是(l1.val+l2.val)/10,进位的值是(l1.val+l2.val)%10
     * 当两个ListNode都不为空的时候，分别取到l1和l2的当前的val，两个数和初始化的进位相加，相加之后更新进位和当前节点的值
     * l1和l2其中一个不为空的话，对其中一个ListNode进行操作，采用同样的公式
     * @param l1 第一个要相加的ListNode
     * @param l2 第二个要相加的ListNode
     * @return 两个ListNode的和
     */
    static ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        if (null==l1) return l2;
        if (null==l2) return l1;
        ListNode dummy=new ListNode(0);
        ListNode current=dummy;
        int carry = 0;
        while (l1!=null&&l2!=null){
            int sum = l1.val + l2.val + carry;//当前两个节点的值的和
            carry = sum / 10;//进位
            int val=sum % 10;//该节点应该存储的值
            current.next= new ListNode(val);
            current=current.next;
            l1 = l1.next;
            l2 = l2.next;
        }
        while (l1!=null){
            int sum=l1.val+carry;//listNode1节点上加上进位的值
            carry=sum / 10;//进位
            int val=sum %10;//该节点应该存储的值
            current.next=new ListNode(val);
            current=current.next;
            l1=l1.next;
        }
        while (l2!=null){
            int sum=l2.val+carry;
            carry=sum/10;
            int val=sum%10;
            current.next=new ListNode(val);
            current=current.next;
            l2=l2.next;
        }
        if (carry!=0) current.next=new ListNode(carry);
        return dummy.next;
    }
}