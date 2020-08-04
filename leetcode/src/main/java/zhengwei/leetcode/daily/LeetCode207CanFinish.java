package zhengwei.leetcode.daily;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 207. 课程表
 * <p>
 * 拓扑排序原理： 对 DAG 的顶点进行排序，使得对每一条有向边 (u, v)(u,v)，均有 u（在排序记录中）比 v 先出现。
 * 亦可理解为对某点 v 而言，只有当 v 的所有源点均出现了，v 才能出现。
 */
public class LeetCode207CanFinish {
    //bfs
    public static boolean canFinishBFS(int numCourses, int[][] prerequisites) {
        //入度
        int[] inDegrees = new int[numCourses];
        //邻接表
        List<List<Integer>> adjacency = new ArrayList<>();
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < numCourses; i++) {
            adjacency.add(new ArrayList<>());
        }
        //得到每个课程的入度和边的关系
        for (int[] cp : prerequisites) {
            inDegrees[cp[0]]++;
            adjacency.get(cp[1]).add(cp[0]);
        }
        //得到入度为0的点
        for (int i = 0; i < numCourses; i++) {
            if (inDegrees[i] == 0) queue.add(i);
        }
        while (!queue.isEmpty()) {
            int pre = queue.poll();
            numCourses--;
            for (int cur : adjacency.get(pre)) {
                if (--inDegrees[cur] == 0) queue.add(cur);
            }
        }
        return numCourses == 0;
    }

    //dfs
    public static boolean canFinishDFS(int numCourses, int[][] prerequisites) {
        //邻接表
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            adj.add(new ArrayList<>());
        }
        for (int[] cp : prerequisites) {
            adj.get(cp[1]).add(cp[0]);
        }
        int[] flag = new int[numCourses];
        for (int i = 0; i < numCourses; i++) {
            if (!dfs(adj, flag, i)) return false;
        }
        return true;
    }

    private static boolean dfs(List<List<Integer>> adj, int[] flag, int i) {
        if (flag[i] == 1) return false;
        if (flag[i] == -1) return true;
        flag[i] = 1;
        for (int j : adj.get(i)) {
            if (!dfs(adj, flag, j)) return false;
        }
        flag[i] = -1;
        return true;
    }

    public static void main(String[] args) {
//        System.out.println(canFinish(2, new int[][]{{1, 0}, {0, 1}}));
        System.out.println(canFinishBFS(3, new int[][]{{0, 1}, {1, 2}, {2, 0}}));
    }
}
