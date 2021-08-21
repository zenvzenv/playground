package zhengwei.leetcode.signalweek200;

/**
 * @author zhengwei AKA Awei
 * @since 2020/8/2 10:52
 */
public class LeetCode5476GetWinner {
    public static int getWinner(int[] arr, int k) {
        if (k >= arr.length) {
            int max = arr[0];
            for (int value : arr) {
                max = Math.max(max, value);
            }
            return max;
        }
        int index = 0;
        while (index < arr.length) {
            int win = 0;
            for (int i = index + 1; i < arr.length; i++) {
                if (arr[index] >= arr[i]) {
                    win++;
                    arr[i] = arr[index];
                } else break;
            }
            if (win >= k) {
                return arr[index];
            }
            index++;
        }
        return -1;
    }

    public static void main(String[] args) {
//        System.out.println(getWinner(new int[]{1, 9, 8, 2, 3, 7, 6, 4, 5}, 7));
        System.out.println(getWinner(new int[]{2, 1, 3, 5, 4, 6, 7}, 7));
    }
}
