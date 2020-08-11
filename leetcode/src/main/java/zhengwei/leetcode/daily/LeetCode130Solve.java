package zhengwei.leetcode.daily;

/**
 * 130. 被围绕的区域
 *
 * @author zhengwei AKA Awei
 * @since 2020/8/11 13:10
 */
public class LeetCode130Solve {
    private static final int[] vx = {0, 0, -1, 1};
    private static final int[] vy = {1, -1, 0, 0};
    private static final char place = '#';

    public void solve(char[][] board) {
        if (board.length == 0 || board[0].length == 0) return;
        int rows = board.length, cols = board[0].length;
        //因为题目中要求边界的O不能修改，需要先对边界的O进行处理
        //将边界的O全部改为#占位
        //第一行
        for (int i = 0; i < rows; i++) {
            dfs(board, 0, i);
        }
        //最后一列
        for (int i = 0; i < rows; i++) {
            dfs(board, i, cols - 1);
        }
        //最后一行
        for (int i = cols - 1; i >= 0; i--) {
            dfs(board, rows - 1, i);
        }
        //第一列
        for (int i = rows - 1; i >= 1; i--) {
            dfs(board, i, 0);
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j] == '#') {
                    board[i][j] = 'O';
                } else if (board[i][j] == 'O') {
                    board[i][j] = 'X';
                }
            }
        }
    }

    private void dfs(char[][] board, int row, int col) {
        if (!inArea(board, row, col)) return;
        if (board[row][col] != 'O') return;
        board[row][col] = place;
        for (int i = 0; i < 4; i++) {
            int newRow = row + vx[i];
            int newCol = col + vy[i];
            dfs(board, newRow, newCol);
        }
    }

    private boolean inArea(char[][] board, int row, int col) {
        return row >= 0 && row < board.length && col >= 0 && col < board[0].length;
    }
}
