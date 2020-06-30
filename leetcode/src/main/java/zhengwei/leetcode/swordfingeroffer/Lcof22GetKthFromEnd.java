package zhengwei.leetcode.swordfingeroffer;

/**
 * LeetCode第22题：链表中倒数第k个节点
 *
 * @author zhengwei AKA Awei
 * @since 2020/6/30 19:15
 */
public class Lcof22GetKthFromEnd {
    public static ListNode getKthFromEnd(ListNode head, int k) {
        if (null == head) return null;
        ListNode fast = head;
        ListNode slow = head;
        for (int i = 0; i < k; i++) {
            fast = fast.next;
        }
        while (fast != null) {
            fast = fast.next;
            slow = slow.next;
        }
        return slow;
    }

    public static void main(String[] args) {
        final ListNode root = ListNode.genListNode(new int[]{1, 2, 3, 4, 5});
        System.out.println(getKthFromEnd(root, 2));
    }
}
