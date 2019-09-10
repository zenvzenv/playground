package zhengwei.java8.lambda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/3 16:09
 */
public class MyLambdaTest {
	public static void main(String[] args) {
		List<String> list = Arrays.asList("zhengwei1", "zhengwei2", "zhengwei3");
		list.forEach(item -> item.toUpperCase());
		list.forEach(System.out::println);
		List<String> result = new ArrayList<>();
		list.stream().map(String::toUpperCase).forEach(result::add);
	}
}
