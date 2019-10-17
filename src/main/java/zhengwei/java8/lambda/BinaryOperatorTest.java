package zhengwei.java8.lambda;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.function.BinaryOperator;

/**
 * @author zhengwei AKA Awei
 * @since 2019/10/17 20:49
 */
public class BinaryOperatorTest {
    private static final BinaryOperator<Integer> binaryOperator = Integer::sum;

    @Test
    void testBinaryOperator() {
        System.out.println(binaryOperator.apply(1, 1));
        System.out.println(calc(1, 2, Integer::sum));
    }

    static int calc(int x, int y, BinaryOperator<Integer> binaryOperator) {
        return binaryOperator.apply(x, y);
    }

    static <T> T min(T s1, T s2, Comparator<T> comparator) {
        return BinaryOperator.minBy(comparator).apply(s1, s2);
    }

    static <T> T max(T s1, T s2, Comparator<T> comparator) {
        return BinaryOperator.maxBy(comparator).apply(s1, s2);
    }

    @Test
    void testMinBy() {
        System.out.println(min("zhengwei", "zhengwei1", Comparator.comparingInt(String::length)));
        System.out.println(min("zhengwei", "awei", Comparator.comparingInt(s -> s.charAt(0))));
    }
}
