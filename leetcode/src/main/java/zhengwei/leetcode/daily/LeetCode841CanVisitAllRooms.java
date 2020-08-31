package zhengwei.leetcode.daily;

import java.util.*;

/**
 * 841. 钥匙和房间
 *
 * @author zhengwei AKA Awei
 * @since 2020/8/31 10:11
 */
public class LeetCode841CanVisitAllRooms {
    private static int count = 0;
    private static boolean[] vis;

    //dfs
    public boolean canVisitAllRoomsDFS(List<List<Integer>> rooms) {
        final int size = rooms.size();
        vis = new boolean[size];
        dfs(rooms, 0);
        return count == size;
    }

    private void dfs(List<List<Integer>> rooms, int i) {
        vis[i] = true;
        count++;
        for (Integer integer : rooms.get(i)) {
            if (!vis[integer]) {
                dfs(rooms, integer);
            }
        }
    }

    //bfs
    public static boolean canVisitAllRoomsBFS(List<List<Integer>> rooms) {
        Queue<Integer> queue = new LinkedList<>();
        final int size = rooms.size();
        vis = new boolean[size];
        int c = 0;
        queue.add(0);
        vis[0] = true;
        while (!queue.isEmpty()) {
            final int i = queue.poll();
            c++;
            for (int x : rooms.get(i)) {
                if (!vis[x]) {
                    vis[x] = true;
                    queue.add(x);
                }
            }
        }
        return size == c;
    }

    public static void main(String[] args) {
        System.out.println(canVisitAllRoomsBFS(Arrays.asList(
                Arrays.asList(1, 3),
                Arrays.asList(3, 0, 1),
                Collections.singletonList(2),
                Collections.singletonList(0)
        )));
    }
}
