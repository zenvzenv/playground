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
	public static void main(String[] args) {
		Predicate<String> predicate = s -> s.length() > 5;
		System.out.println(predicate.test("hello"));
	}

	@Test
	void testPredicate() {
		List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		//所有方法的实现全部由使用者来提供
		conditionFilter(list, integer -> integer % 2 == 0);
		conditionFilter(list, integer -> integer % 2 != 0);
		conditionFilter(list, integer -> integer > 5);
		conditionFilter(list, integer -> integer < 3);
		conditionFilter(list, integer -> true);
		conditionFilter(list, integer -> false);
	}

	private static void conditionFilter(List<Integer> list, Predicate<Integer> predicate) {
		list.forEach(x -> {
			if (predicate.test(x)) System.out.printf("int:%d\t", x);
		});
		System.out.println();
	}

	private static void conditionAndFilter(List<Integer> list,
	                                       Predicate<Integer> predicate,
	                                       Predicate<Integer> predicate2){
		list.forEach(x->{
			if (predicate.and(predicate2).test(x)) {

			}
		});
	}
}
