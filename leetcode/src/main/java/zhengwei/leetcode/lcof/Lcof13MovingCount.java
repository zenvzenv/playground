package zhengwei.leetcode.lcof;

/**
 * 剑指offer第13题：机器人的运动范围
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/14 14:37
 */
public class Lcof13MovingCount {
    public static int movingCount(int m, int n, int k) {
        boolean[][] visited = new boolean[m][n];
        return dfs(0, 0, m, n, k, visited);
    }

    private static int dfs(int i, int j, int m, int n, int k, boolean[][] visited) {
        if (!inArea(i, j, m, n) || visited[i][j] || !isSmallThanK(i, j, k)) return 0;
        //不需要重置是否访问标志，否则会重复计数
        visited[i][j] = true;
        return 1 + dfs(i + 1, j, m, n, k, visited) + dfs(i, j + 1, m, n, k, visited);
    }

    private static boolean inArea(int i, int j, int m, int n) {
        return i >= 0 && j >= 0 && i < m && j < n;
    }

    private static boolean isSmallThanK(int i, int j, int k) {
        int sum = 0;
        while (i > 0) {
            sum += i % 10;
            i /= 10;
        }
        while (j > 0) {
            sum += j % 10;
            j /= 10;
        }
        return sum <= k;
    }

    public static void main(String[] args) {
        System.out.println(movingCount(3, 2, 17));
    }
}
