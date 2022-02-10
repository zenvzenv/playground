package zhengwei.leetcode.daily;

import java.util.ArrayList;
import java.util.List;

/**
 * 1447. 最简分数
 * @author zhengwei AKA zenv
 * @since 2022/2/10
 */
public class LeetCode1447SimplifiedFractions {
    public List<String> simplifiedFractions(int n) {
        final List<String> result = new ArrayList<>();
        for (int denominator = 2; denominator <= n; denominator++) {
            for (int numerator = 1; numerator < denominator; numerator++) {
                // 最简分数即分子和分母没有公约数
                if (gcd(denominator, numerator) == 1) {
                    result.add(numerator + "/" + denominator);
                }
            }
        }
        return result;
    }

    /**
     * 欧几里得求公约数
     *
     * @return 如果返回1则表示两个数没有公约数，否则有公约数
     */
    private int gcd(int a, int b) {
        return b != 0 ? gcd(b, a % b) : a;
    }
}
