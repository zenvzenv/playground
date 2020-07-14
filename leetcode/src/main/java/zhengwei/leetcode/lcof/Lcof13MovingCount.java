package zhengwei.leetcode.lcof;

/**
 * 剑指offer第13题：机器人的运动范围
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/14 14:37
 */
public class Lcof13MovingCount {
    public static int movingCount(int m, int n, int k) {
        //标记m,n坐标是否被访问过
        boolean[][] visited = new boolean[m][n];
        return dfs(visited, k, 0, 0);
    }

    private static int dfs(boolean[][] visited, int k, int i, int j) {
        if (!inArea(i, j, visited.length, visited[0].length) || visited[i][j] || !isSmallThanK(i, j, k)) return 0;
        visited[i][j] = true;
        return 1 + dfs(visited, k, i + 1, j) + dfs(visited, k, i, j + 1);
    }

    private static boolean inArea(int i, int j, int m, int n) {
        return i >= 0 && j >= 0 && i < m && j < n;
    }

    private static boolean isSmallThanK(int m, int n, int k) {
        int sum = 0;
        while (m != 0) {
            sum += m % 10;
            m /= 10;
        }
        while (n != 0) {
            sum += n % 10;
            n /= 10;
        }
        return sum <= k;
    }

    public static void main(String[] args) {
        System.out.println(movingCount(11, 8, 16));
    }
}
