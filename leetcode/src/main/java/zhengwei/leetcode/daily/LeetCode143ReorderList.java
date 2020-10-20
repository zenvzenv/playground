package zhengwei.leetcode.daily;

import zhengwei.leetcode.common.ListNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 143. 重排链表
 *
 * @author zhengwei AKA Awei
 * @since 2020/10/20 10:14
 */
public class LeetCode143ReorderList {
    public void reorderList(ListNode head) {
        if (null == head) return;
        List<ListNode> listNodes = new ArrayList<>();
        ListNode node = head;
        while (node != null) {
            listNodes.add(node);
            node = node.next;
        }
        int left = 0, right = listNodes.size() - 1;
        while (left < right) {
            listNodes.get(left).next = listNodes.get(right);
            left++;
            if (left == right) {
                break;
            }
            listNodes.get(right).next = listNodes.get(left);
            right--;
        }
        listNodes.get(left).next = null;
    }
}
