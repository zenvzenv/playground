package zhengwei.leetcode.daily;

import java.util.PriorityQueue;

/**
 * 1046. 最后一块石头的重量
 *
 * @author zhengwei AKA zenv
 * @since 2020/12/30 9:22
 */
public class LeetCode1046LastStoneWeight {
    public int lastStoneWeight(int[] stones) {
        //构建大顶堆
        final PriorityQueue<Integer> pq = new PriorityQueue<>((a, b) -> b - a);
        for (int stone : stones) {
            pq.offer(stone);
        }
        while (pq.size() > 1) {
            final int b = pq.poll();
            final int a = pq.poll();
            pq.offer(b - a);
        }
        return pq.isEmpty() ? 0 : pq.poll();
    }
}
