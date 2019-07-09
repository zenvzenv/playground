package zhengwei.java8.lambda;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author zhengwei AKA Sherlock
 * @since 2019/7/7 19:05
 */
public class TestCollections {
	public static void main(String[] args) {
		List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
		for (int i = 0; i < list.size(); i++) {
			System.out.println(i);
		}
		System.out.println("----------------------------------");
		for (Integer i : list) {
			System.out.println(i);
		}
		System.out.println("----------------------------------");
		list.forEach(new Consumer<Integer>() {
			@Override
			public void accept(Integer integer) {
				System.out.println(integer);
			}
		});
		System.out.println("----------------------------------");
		list.forEach(System.out::println);
	}
}
