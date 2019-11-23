package zhengwei.thread.atomic;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * 原子类型的数组
 *
 * @author zhengwei AKA Awei
 * @since 2019/9/23 20:07
 */
public class AtomicArrayTest {
	private static AtomicIntegerArray integerArray;

	@BeforeAll
	static void testInitAtomicArray() {
		integerArray = new AtomicIntegerArray(10);
		System.out.println(integerArray.length());
	}

	@Test
	void testGet() {
		System.out.println(integerArray.get(5));
	}

	@Test
	void testSet() {
		for (int i = 0; i < integerArray.length(); i++) {
			integerArray.set(i, i);
		}
		for (int i = 0; i < integerArray.length(); i++) {
			System.out.println(integerArray.get(i));
		}
	}
	@Test
	void testGetAndSet(){

	}
}
