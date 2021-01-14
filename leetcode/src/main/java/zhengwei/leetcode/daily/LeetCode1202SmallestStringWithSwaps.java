package zhengwei.leetcode.daily;

import java.util.*;

/**
 * 1202. 交换字符串中的元素
 */
public class LeetCode1202SmallestStringWithSwaps {
    public String smallestStringWithSwaps(String s, List<List<Integer>> pairs) {
        final int length = s.length();
        //记录每个节点的父节点，在初始化时，每个节点的父节点都是自己
        int[] parent = new int[length];
        for (int i = 0; i < length; i++) {
            parent[i] = i;
        }

        //初始化各个节点的祖先节点，因为每个 pair 中的元素可以互相调换，所以它们在一个连通分量中，也就是有同一个祖先
        for (List<Integer> pair : pairs) {
            final int ancestry1 = find(pair.get(0), parent);
            final int ancestry2 = find(pair.get(1), parent);
            //设置祖先节点，祖先节点并无先后关系，哪个节点做祖先都是可以的
            parent[ancestry2] = ancestry1;
        }

        //[父节点， 相同父节点的元素]
        Map<Integer, Queue<Character>> map = new HashMap<>();
        for (int i = 0; i < length; i++) {
            //找寻同一个连通分量的节点，把他们放到同一个队列中
            final int ancestry = find(i, parent);
            map.computeIfAbsent(ancestry, k -> new PriorityQueue<>()).offer(s.charAt(i));
        }

        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            //找到 i 所在的队列，然后队列依次出队即可
            //因为使用的是小顶堆，所以每次出队的都是最小的元素
            final Queue<Character> queue = map.get(find(i, parent));
            sb.append(queue.poll());
        }
        return sb.toString();
    }

    //查找祖先节点，只有当前的值等于父节点值的时候才是祖先节点
    private int find(int i, int[] parent) {
        if (parent[i] != i) {
            parent[i] = find(parent[i], parent);
        }
        return parent[i];
    }
}
