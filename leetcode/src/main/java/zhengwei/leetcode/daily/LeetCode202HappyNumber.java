package zhengwei.leetcode.daily;

import java.util.HashSet;
import java.util.Set;

/**
 * LeetCode第202题：快乐数
 *
 * @author zhengwei AKA Awei
 * @since 2020/4/30 9:14
 */
public class LeetCode202HappyNumber {
    //1.要么最终收敛到1
    //2.要么最终收敛到一个不为1的循环链表
    //3.无限膨胀，不过这种情况不可能，最终都会收敛
    private static boolean isHappy1(int n) {
        Set<Integer> set = new HashSet<>();
        while (n != 1 && !set.contains(n)) {
            n = getNext(n);
            set.add(n);
        }
        return n == 1;
    }

    //快慢指针
    private static boolean isHappy2(int n) {
        int slow = n;
        int fast = getNext(n);
        while (slow != fast) {
            slow = getNext(slow);
            fast = getNext(getNext(fast));
        }
        return slow == 1;
    }

    //获取下一个数字
    private static int getNext(int n) {
        int next = 0;
        while (n != 0) {
            int low = n % 10;
            next += (low * low);
            n /= 10;
        }
        return next;
    }

    public static void main(String[] args) {
        System.out.println(isHappy2(116));
    }
}
