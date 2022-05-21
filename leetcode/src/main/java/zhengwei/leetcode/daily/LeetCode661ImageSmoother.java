package zhengwei.leetcode.daily;

/**
 * 661. 图片平滑器
 *
 * @author zhengwei AKA zenv
 * @since 2022/3/24
 */
public class LeetCode661ImageSmoother {
    public int[][] imageSmoother(int[][] img) {
        final int rows = img.length, cols = img[0].length;
        final int[][] ret = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // 前缀和思想
                int sum = 0, cnt = 0;
                // 当前点的左右
                for (int x = i - 1; x <= i + 1; x++) {
                    // 当前点的上下
                    for (int y = j - 1; y <= j + 1; y++) {
                        if (x >= 0 && y >= 0 && x < rows && y < cols) {
                            sum += img[x][y];
                            cnt++;
                        }
                    }
                }
                ret[i][j] = sum / cnt;
            }
        }
        return ret;
    }
}
