package zhengwei.thread.atomic;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * 原子的更新字段
 * 功能类似于AtomicInteger，对字段进行原子更新操作
 * 1.希望类中的属性具备原子性
 * 1.1 必须被volatile修饰
 * 1.2 修饰符必须是非private(如果是当前类，字段的访问权限可以是private,protected,public)
 * 1.3 类型必须一致
 * 2.不想使用锁(包括显示锁或者重量级锁)
 * 3.大量需要原子类型修饰的对象，相比较耗费内存
 *
 * @author zhengwei AKA Awei
 * @since 2019/10/4 15:54
 */
public class AtomicIntegerFieldUpdateTest {
	static class TestMe {
		volatile int i;
		private volatile int z;
		volatile Integer I;
		int x;
	}

	public static void main(String[] args) {
		//第二个参数必须是类中有的字段，否则报错
		AtomicIntegerFieldUpdater<TestMe> updater = AtomicIntegerFieldUpdater.newUpdater(TestMe.class, "i");
		TestMe testMe = new TestMe();
		for (int i = 0; i < 2; i++) {
			new Thread(() -> {
				for (int j = 0; j < 20; j++) {
					int v = updater.getAndIncrement(testMe);
					System.out.println(Thread.currentThread().getName() + "->" + v);
				}
			}).start();
		}
	}

	@Test
	void testPrivateField() {
		//如果类中的字段是私有的话，则无法访问，会报错：Class zhengwei.thread.atomic.AtomicIntegerFieldUpdateTest can not access a member of class zhengwei.thread.atomic.AtomicIntegerFieldUpdateTest$TestMe with modifiers "private volatile"
		AtomicIntegerFieldUpdater<TestMe> updater = AtomicIntegerFieldUpdater.newUpdater(TestMe.class, "z");
		TestMe me = new TestMe();
		System.out.println(updater.compareAndSet(me, 0, 1));
	}

	/**
	 * 如果被比较的对象是null的话，也是会报错的
	 */
	@Test
	void testTargetIsNull() {
		AtomicIntegerFieldUpdater<TestMe> updater = AtomicIntegerFieldUpdater.newUpdater(TestMe.class, "i");
		updater.compareAndSet(null, 0, 1);
	}

	@Test
	void testFieldName() {
		//通过反射去拿字段，如果字段不存在也是会报错的
		AtomicIntegerFieldUpdater<TestMe> updater = AtomicIntegerFieldUpdater.newUpdater(TestMe.class, "i1");
		TestMe me = new TestMe();
		updater.compareAndSet(me, 0, 1);
	}

	@Test
	void testReferenceType() {
		//引用类型，如果泛型不正确的话，会报ClassCastException
		AtomicReferenceFieldUpdater<TestMe, Integer> updater = AtomicReferenceFieldUpdater.newUpdater(TestMe.class, Integer.class, "I");
		TestMe me = new TestMe();
		updater.compareAndSet(me, 0, 1);
	}

	@Test
	void testFieldIsNotVolatile() {
		//字段必须被volatile修饰，否则也是会报错的
		AtomicReferenceFieldUpdater<TestMe, Integer> updater = AtomicReferenceFieldUpdater.newUpdater(TestMe.class, Integer.class, "x");
		TestMe me = new TestMe();
		updater.compareAndSet(me, 0, 1);
	}
}

class TestMe2 {
	private volatile int i;
	private AtomicInteger j = new AtomicInteger();
	private static final AtomicIntegerFieldUpdater<TestMe2> UPDATER = AtomicIntegerFieldUpdater.newUpdater(TestMe2.class, "i");

	public void update(int newValue) {
		UPDATER.compareAndSet(this, i, newValue);
	}

	public int get() {
		return i;
	}

	public static void main(String[] args) {
		TestMe2 me = new TestMe2();
		me.update(10);
		System.out.println(me.get());
	}
}