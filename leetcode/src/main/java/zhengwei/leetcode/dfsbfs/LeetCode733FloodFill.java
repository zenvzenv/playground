package zhengwei.leetcode.dfsbfs;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 733. 图像渲染
 *
 * @author zhengwei AKA Awei
 * @since 2020/8/3 19:47
 */
public class LeetCode733FloodFill {
    //dfs
    public static int[][] floodFillDFS(int[][] image, int sr, int sc, int newColor) {
        int color = image[sr][sc];
        if (color != newColor) dfs(image, sr, sc, color, newColor);
        return image;
    }

    private static boolean inArea(int[][] image, int i, int j) {
        return i >= 0 && i < image.length && j >= 0 && j < image[0].length;
    }

    private static void dfs(int[][] image, int sr, int sc, int color, int newColor) {
        if (!inArea(image, sr, sc)) return;
        if (image[sr][sc] != color) return;
        image[sr][sc] = newColor;
        //上下右左
        int[] vx = {0, 0, 1, -1};
        int[] vy = {1, -1, 0, 0};
        for (int i = 0; i < 4; i++) {
            int newSr = sr + vx[i];
            int newSc = sc + vy[i];
            dfs(image, newSr, newSc, color, newColor);
        }
    }

    //bfs
    public static int[][] floodFillBFS(int[][] image, int sr, int sc, int newColor) {
        int color = image[sr][sc];
        int[] vx = {0, 0, 1, -1};
        int[] vy = {1, -1, 0, 0};
        if (color != newColor) {
            image[sr][sc] = newColor;
            Queue<int[]> queue = new LinkedList<>();
            queue.add(new int[]{sr, sc});
            while (!queue.isEmpty()) {
                final int[] poll = queue.poll();
                for (int i = 0; i < 4; i++) {
                    int newSr = poll[0] + vx[i];
                    int newSc = poll[1] + vy[i];
                    if (!inArea(image, newSr, newSc)) continue;
                    if (image[newSr][newSc] == color) {
                        image[newSr][newSc] = newColor;
                        queue.add(new int[]{newSr, newSc});
                    }
                }
            }
        }
        return image;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.deepToString(floodFillDFS(new int[][]{{1, 1, 1}, {1, 1, 0}, {1, 0, 1}}, 1, 1, 2)));
    }
}
