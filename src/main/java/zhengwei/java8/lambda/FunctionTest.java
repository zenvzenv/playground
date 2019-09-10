package zhengwei.java8.lambda;

import java.util.Arrays;
import java.util.function.Function;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/4 11:03
 */
public class FunctionTest {
	public static void main(String[] args) {
		/*
		* 注意：由于toUpperCase是实例方法，所有实例方法的第一个隐藏变量就是this
		* 也就是说调用该方法的时候一定会有一个字符串实例，这个字符串实例对象就是lambda的第一个参数
		*/
		Function<String,String> function=String::toUpperCase;
		//function是Function的一个实现类，它是直接继承自Object，所以接口中重写Object的方法时，也不会把重写的方法算作是一个新的方法
		System.out.println(function.getClass());
		System.out.println(function.getClass().getSuperclass());
		System.out.println(Arrays.toString(function.getClass().getInterfaces()));
		//传递行为
		System.out.println(compute1(1, value -> 2 * value));
		System.out.println(compute1(2, value -> value * value));
		System.out.println(compute1(3, value -> 3 + value));

		System.out.println(compute2(4, x -> x * 2, x -> x * x));
		System.out.println(compute3(4, x -> x * 2, x -> x * x));
	}

	//现在是在执行的时候再去指定操作
	public static int compute1(int a, Function<Integer, Integer> function) {
		return function.apply(a);
	}

	//以前是我们将要对参数的操作提前定义好，需要的时候调用
	private static int method1(int a) {
		return 2 * a;
	}

	private static int method2(int a) {
		return a * a;
	}

	private static int method3(int a) {
		return 3 + a;
	}

	private static int compute2(int a, Function<Integer, Integer> function1, Function<Integer, Integer> function2) {
		//先引用compose中传入的函数，再应用调用compose的函数
		return function1.compose(function2).apply(a);
	}

	private static int compute3(int a, Function<Integer, Integer> function1, Function<Integer, Integer> function2) {
		//先应用调用andThen的函数，再应用传入andThen的函数
		return function1.andThen(function2).apply(a);
	}
}
