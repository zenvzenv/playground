package zhengwei.leetcode.swordfingeroffer;

/**
 * 刷题需要用到的公用对象
 *
 * @author zhengwei AKA Awei
 * @since 2020/6/30 19:16
 */
public final class ListNode {
    final int val;
    ListNode next;

    public ListNode(int val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return "ListNode{" +
                "val=" + val +
                ", next=" + next +
                '}';
    }

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
