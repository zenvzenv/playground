package zhengwei.leetcode.daily;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author zhengwei AKA Awei
 * @since 2020/8/16 11:04
 */
public class LeetCode733FloodFill {
    private static final int[] vx = {0, 0, -1, 1};
    private static final int[] vy = {1, -1, 0, 0};

    public static int[][] floodFill(int[][] image, int sr, int sc, int newColor) {
        int oldColor = image[sr][sc];
        if (oldColor == newColor) return image;
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{sr, sc});
        while (!queue.isEmpty()) {
            int[] temp = queue.poll();
            image[temp[0]][temp[1]] = newColor;
            for (int i = 0; i < 4; i++) {
                int newR = temp[0] + vx[i];
                int newC = temp[1] + vy[i];
                if (inArea(newR, newC, image) && image[newR][newC] == oldColor) {
                    image[newR][newC] = newColor;
                    queue.add(new int[]{newR, newC});
                }
            }
        }
        return image;
    }

    private static boolean inArea(int r, int c, int[][] image) {
        return r >= 0 && r < image.length && c >= 0 && c < image[0].length;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.deepToString(floodFill(new int[][]{{0, 0, 0}, {0, 1, 0}}, 1, 1, 2)));
    }
}
