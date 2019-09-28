package zhengwei.java8.lambda;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author zhengwei AKA Awei
 * @since 2019/9/25 13:42
 */
public class PredicateTest {
	private static final List<Integer> LIST = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

	public static void main(String[] args) {
		Predicate<String> predicate = s -> s.length() > 5;
		System.out.println(predicate.test("hello"));
	}

	@Test
	void testPredicate() {

		//所有方法的实现全部由使用者来提供
		conditionFilter(LIST, integer -> integer % 2 == 0);
		conditionFilter(LIST, integer -> integer % 2 != 0);
		conditionFilter(LIST, integer -> integer > 5);
		conditionFilter(LIST, integer -> integer < 3);
		conditionFilter(LIST, integer -> true);
		conditionFilter(LIST, integer -> false);
	}

	@Test
	void testPredicateAnd() {
		conditionAndFilter(LIST,
				integer -> integer % 2 == 0,
				integer -> integer > 5);
	}

	private static void conditionFilter(List<Integer> list, Predicate<Integer> predicate) {
		list.forEach(x -> {
			if (predicate.test(x)) System.out.printf("int:%d\t", x);
		});
		System.out.println();
	}

	/**
	 * 两个Predicate函数的and操作
	 *
	 * @param list       待操作集合
	 * @param predicate1 第一个Predicate
	 * @param predicate2 第二个Predicate
	 */
	private static void conditionAndFilter(List<Integer> list,
	                                       Predicate<Integer> predicate1,
	                                       Predicate<Integer> predicate2) {
		list.forEach(x -> {
			//and的操作，即同时满足两个Predicate都返回true才会去执行接下来的操作
			if (predicate1.and(predicate2).test(x)) {
				System.out.println(x);
			}
		});
	}

	/**
	 * 两个Predicate函数的or操作
	 *
	 * @param list       待操作集合
	 * @param predicate1 第一个Predicate
	 * @param predicate2 第二个Predicate
	 */
	private static void conditionOrFilter(List<Integer> list,
	                                      Predicate<Integer> predicate1,
	                                      Predicate<Integer> predicate2) {
		list.forEach(x -> {
			if (predicate1.or(predicate2).test(x)) {
				System.out.println(x);
			}
		});
	}
}
