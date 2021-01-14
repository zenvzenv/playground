package zhengwei.leetcode.daily;

import java.util.ArrayList;
import java.util.List;

/**
 * 1018. 可被 5 整除的二进制前缀
 *
 * @author zhengwei AKA zenv
 * @since 2021/1/14 9:02
 */
public class LeetCode1018PrefixesDivBy5 {
    public List<Boolean> prefixesDivBy5(int[] A) {
        if (null == A || 0 == A.length) return new ArrayList<>(0);
        final int len = A.length;
        final List<Boolean> result = new ArrayList<>(A.length);
        int remainder = 0;
        for (int value : A) {
            //每次只保留余数
            remainder = ((remainder << 1) + value) % 5;
            result.add(remainder == 0);
        }
        return result;
    }
}
