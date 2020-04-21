package zhengwei.LeetCode.Daily;

/**
 * LeetCode第200题：岛屿数量
 *
 * @author zhengwei AKA Awei
 * @since 2020/4/21 19:30
 */
public class LeetCode200NumIslands {
    //方向数组，表示当前为止的4个方向横纵坐标的位移量，这是一个常见的技巧
    private static final int[][] directions = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}};
    //标记数组，标记该坐标是否被标记过
    private boolean[][] isMarked;
    //行数
    private int rows;
    //列数
    private int cols;
    //全局grid二维数组
    private char[][] grid;

    public static void main(String[] args) {
        char[][] grid1 = {
                {'1', '1', '1', '1', '0'},
                {'1', '1', '0', '1', '0'},
                {'1', '1', '0', '0', '0'},
                {'0', '0', '0', '0', '0'}};
        System.out.println(new LeetCode200NumIslands().numIslands(grid1));
    }

    private int numIslands(char[][] grid) {
        rows = grid.length;
        if (rows == 0) {
            return 0;
        }
        cols = grid[0].length;
        this.grid = grid;
        isMarked = new boolean[rows][cols];
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                //没有被标记过并且该坐标是陆地
                if (!isMarked[i][j] && grid[i][j] == '1') {
                    count++;
                    dfs(i, j);
                }
            }
        }
        return count;
    }

    private void dfs(int i, int j) {
        isMarked[i][j] = true;
        //去遍历该坐标的上下左右四个坐标
        for (int k = 0; k < 4; k++) {
            int newX = i + directions[k][0];
            int newY = j + directions[k][1];
            //数组没有越界，并且是陆地，并且没有标记过
            if (inArea(newX, newY) && grid[newX][newY] == '1' && !isMarked[newX][newY]) {
                dfs(newX, newY);
            }
        }
    }

    //坐标点是否还在二维网格中，判断该坐标是否越界
    private boolean inArea(int x, int y) {
        return x >= 0 && x < rows && y >= 0 && y < cols;
    }
}
