package zhengwei.leetcode.lcof;

/**
 * 剑指offer第12题：矩阵中的路径
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/14 13:28
 */
public class Lcof12Exist {
    private static final int[][] direct = {{1, 0}, {-1, 0}, {0, -1}, {0, 1}};

    public static boolean exist1(char[][] board, String word) {
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

    /*private static boolean dfs(char[][] board, char[] word, int i, int j, int k) {
        if (i >= board.length || j >= board[0].length || i < 0 || j < 0 || board[i][j] != word[k]) return false;
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
        if (!isArea(i, j, board.length, board[0].length) || board[i][j] != word[index])
            return false;
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
        System.out.println(exist1(new char[][]{{'a', 'b'}}, "ba"));
        System.out.println(exist2(new char[][]{{'a', 'b'}}, "ba"));
    }

    public static boolean exist2(char[][] board, String word) {
        int rows = board.length;
        int cols = board[0].length;
        char[] words = word.toCharArray();
        boolean[][] visited = new boolean[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (dfs(board, visited, i, j, words, 0)) return true;
            }
        }
        return false;
    }

    private static boolean dfs(char[][] board, boolean[][] visited, int i, int j, char[] words, int index) {
        if (!inArea(i, j, board.length, board[0].length) || visited[i][j] || words[index] != board[i][j])
            return false;
        if (index == words.length - 1) return true;
        visited[i][j] = true;
        boolean res = dfs(board, visited, i, j + 1, words, index + 1) ||
                dfs(board, visited, i, j - 1, words, index + 1) ||
                dfs(board, visited, i - 1, j, words, index + 1) ||
                dfs(board, visited, i + 1, j, words, index + 1);
        visited[i][j] = false;
        return res;
    }

    private static boolean inArea(int i, int j, int rows, int cols) {
        return i >= 0 && j >= 0 && i < rows && j < cols;
    }
}
