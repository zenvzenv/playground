package zhengwei.leetcode.daily;

/**
 * 529. 扫雷游戏
 *
 * @author zhengwei AKA Awei
 * @since 2020/8/20 20:06
 */
public class LeetCode529UpdateBoard {
    private static final int[] vx = {0, 0, -1, 1, -1, 1, -1, 1};
    private static final int[] vy = {1, -1, 0, 0, 1, 1, -1, -1};

    public char[][] updateBoard(char[][] board, int[] click) {
        int r = click[0], c = click[1];
        if (board[r][c] == 'M') {
            board[r][c] = 'X';
        } else {
            dfs(board, r, c);
        }
        return board;
    }

    private void dfs(char[][] board, int r, int c) {
        //首先统计上、下、左、右、左上、右上、左下、右下8个方向是否有雷
        int count = 0;
        for (int i = 0; i < 8; i++) {
            int newR = r + vx[i];
            int newC = c + vy[i];
            if (inArea(board, newR, newC)) continue;
            if (board[newR][newC] == 'M') count++;
        }
        if (count > 0) {
            board[r][c] = (char) (count + '0');
            return;
        }
        //8个方向没有类的话，将当前节点更新为B
        board[r][c] = 'B';
        //继续遍历该点的上、下、左、右、左上、右上、左下、右下8个方向
        for (int i = 0; i < 8; i++) {
            int newR = r + vx[i];
            int newC = c + vy[i];
            if (inArea(board, newR, newC) || board[newR][newC] != 'E') continue;
            dfs(board, newR, newC);
        }
    }

    private boolean inArea(char[][] board, int r, int c) {
        return r < 0 || r >= board.length || c < 0 || c >= board[0].length;
    }
}
