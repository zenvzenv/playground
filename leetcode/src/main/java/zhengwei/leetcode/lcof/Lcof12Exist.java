package zhengwei.leetcode.lcof;

/**
 * 剑指offer第12题：矩阵中的路径
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/14 13:28
 */
public class Lcof12Exist {
    private static final int[][] direct = {{1, 0}, {-1, 0}, {0, -1}, {0, 1}};

    public static boolean exist(char[][] board, String word) {
        int rows = board.length;
        int cols = board[0].length;
        final char[] words = word.toCharArray();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (dfs(board, words, i, j, 0)) return true;
            }
        }
        return false;
    }

    /*rivate static boolean dfs(char[][] board, char[] word, int i, int j, int k) {
        if (i >= board.length || j > board[0].length || i < 0 || j < 0 || board[i][j] != word[k]) return false;
        if (k == word.length - 1) return true;
        char temp = board[i][j];
        board[i][j] = '/';
        boolean res = dfs(board, word, i + 1, j, k + 1) ||
                dfs(board, word, i - 1, j, k + 1) ||
                dfs(board, word, i, j - 1, k + 1) ||
                dfs(board, word, i, j + 1, k + 1);
        board[i][j] = temp;
        return res;
    }*/

    private static boolean dfs(char[][] board, char[] word, int i, int j, int index) {
        if (!isArea(i, j, board.length, board[0].length) || board[i][j] != word[index]) return false;
        if (index == word.length - 1) return true;
        char temp = board[i][j];
        board[i][j] = '/';
        boolean res = dfs(board, word, i + 1, j, index + 1) ||
                dfs(board, word, i - 1, j, index + 1) ||
                dfs(board, word, i, j - 1, index + 1) ||
                dfs(board, word, i, j + 1, index + 1);
        board[i][j] = temp;
        return res;
    }

    private static boolean isArea(int newRow, int newCol, int rows, int cols) {
        return newRow >= 0 && newCol >= 0 && newRow < rows && newCol < cols;
    }

    public static void main(String[] args) {
        System.out.println(exist(new char[][]{{'A', 'B', 'C', 'E'}, {'S', 'F', 'C', 'S'}, {'A', 'D', 'E', 'E'}}, "ABCCED"));
    }
}
