package zhengwei.designpattern.immutable;

import java.util.stream.IntStream;

/**
 * 不可变对象的客户端
 * 1.不可变对象一定是线程安全的  String
 * 2.可变对象不一定是不安全的 StringBuffer
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/27 10:45
 */
public class ImmutableClient {
	public static void main(String[] args) {
		//共享数据
		Person person = new Person("Awei", "SQC");
		IntStream.rangeClosed(0, 5).forEach(i -> new UsePersonThread(person).start());
	}
}
