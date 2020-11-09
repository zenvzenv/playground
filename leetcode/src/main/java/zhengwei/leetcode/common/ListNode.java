package zhengwei.leetcode.common;

/**
 * LeetCode 单向链表节点
 *
 * @author zhengwei AKA Awei
 * @since 2020/6/30 19:16
 */
public final class ListNode {
    public final int val;
    public ListNode next;

    public ListNode(int val) {
        this.val = val;
    }

    public ListNode(int val, ListNode next) {
        this.val = val;
        this.next = next;
    }

    @Override
    public String toString() {
        return "ListNode{" +
                "val=" + val +
                ", next=" + next +
                '}';
    }

    /**
     * 将数组转成单向链表
     *
     * @param arr 指定数组
     * @return 单向链表
     */
    public static ListNode genListNode(int[] arr) {
        ListNode root = new ListNode(arr[0]);
        ListNode temp = root;
        for (int i = 1; i < arr.length; i++) {
            temp.next = new ListNode(arr[i]);
            temp = temp.next;
        }
        return root;
    }
}
