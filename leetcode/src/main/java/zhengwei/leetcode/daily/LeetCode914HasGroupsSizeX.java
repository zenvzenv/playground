package zhengwei.leetcode.daily;

/**
 * LeetCode的第914题
 *
 * @author zhengwei AKA Awei
 * @since 2020/3/27 16:30
 */
public class LeetCode914HasGroupsSizeX {
    public static void main(String[] args) {
        System.out.println(hasGroupsSizeX(new int[]{1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3}));
//        System.out.println(gcd(4, 5));
    }

    public static boolean hasGroupsSizeX(int[] deck) {
        int[] temp = new int[10000];
        for (int i : deck) {
            temp[i]++;
        }
        int x = temp[deck[0]];
        for (int i = 0; i < 10000; i++) {
            if (temp[i] == 1) return false;
            if (temp[i] > 1) {
                x = gcd(x, temp[i]);
                if (x == 1) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 求(a,b)得最大公约数
     *
     * @param a 整数
     * @param b 整数
     * @return a和b得最大公约数
     */
    private static int gcd(int a, int b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }
}
