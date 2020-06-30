package zhengwei.leetcode.swordfingeroffer;

/**
 * 剑指offer第18题：删除链表的节点
 *
 * @author zhengwei AKA Awei
 * @since 2020/6/30 18:37
 */
public class Lcof18DeleteNode {
    public static ListNode deleteNode(ListNode head, int val) {
        if (null == head) return null;
        if (head.val == val) return head.next;
        ListNode cur = head;
        while (cur.next != null && cur.next.val != val) {
            cur = cur.next;
        }
        if (cur.next != null) {
            cur.next = cur.next.next;
        }
        return head;
    }

    public static void main(String[] args) {
        ListNode root = ListNode.genListNode(new int[]{4, 5, 1, 9});
        System.out.println(deleteNode(root, 5));
    }
}
