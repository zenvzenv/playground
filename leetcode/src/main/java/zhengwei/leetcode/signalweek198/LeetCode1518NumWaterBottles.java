package zhengwei.leetcode.signalweek198;

/**
 * 1518. 换酒问题
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/20 9:38
 */
public class LeetCode1518NumWaterBottles {
    public static int numWaterBottles(int numBottles, int numExchange) {
        int res = numBottles;
        while (numBottles >= numExchange) {
            int change = numBottles / numExchange;
            res += change;
            numBottles = change + numBottles % numExchange;
        }
        return res;
    }

    public static void main(String[] args) {
        System.out.println(numWaterBottles(17, 3));
    }
}
