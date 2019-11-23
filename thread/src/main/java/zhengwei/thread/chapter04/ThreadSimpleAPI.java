package zhengwei.thread.chapter04;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.stream.IntStream;

/**
 * 一些简单的Thread API
 *
 * @author zhengwei AKA Sherlock
 * @since 2019/7/6 10:01
 */
public class ThreadSimpleAPI {
	@Test
	void testSimpleAPI() {
		Thread t = new Thread(() -> {
			Optional.of("Hello").ifPresent(System.out::println);
			try {
				Thread.sleep(10_000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}, "t1");
		t.start();
		Optional.of(t.getName()).ifPresent(System.out::println);//线程的名字
		Optional.of(t.getId()).ifPresent(System.out::println);//线程的ID
		Optional.of(t.getPriority()).ifPresent(System.out::println);//线程的优先级
	}

	/**
	 * 线程的优先级
	 * 虽然手动设置的线程的优先级，但线程的调度并不一定严格按照优先级的顺序去执行
	 */
	@Test
	void testThreadPriority() {
		Thread t1 = new Thread(() -> {
			for (int i = 1; i <= 1000; i++) {
				Optional.of(Thread.currentThread().getName() + "-index-" + i).ifPresent(System.out::println);
			}
		}, "t1");
		t1.setPriority(Thread.MAX_PRIORITY);

		Thread t2 = new Thread(() -> {
			for (int i = 1; i <= 1000; i++) {
				Optional.of(Thread.currentThread().getName() + "-index-" + i).ifPresent(System.out::println);
			}
		}, "t2");
		t2.setPriority(Thread.NORM_PRIORITY);

		Thread t3 = new Thread(() -> {
			for (int i = 1; i <= 1000; i++) {
				Optional.of(Thread.currentThread().getName() + "-index-" + i).ifPresent(System.out::println);
			}
		}, "t3");
		t3.setPriority(Thread.MIN_PRIORITY);

		t1.start();
		t2.start();
		t3.start();
	}

	@Test
	void testThreadJoin1() throws InterruptedException {
		Thread t1 = new Thread(() -> {
			IntStream.range(1, 1000).forEach(x -> System.out.println(Thread.currentThread().getName() + "--index--" + x));
		},"t1");
		Thread t2 = new Thread(() -> {
			IntStream.range(1, 1000).forEach(x -> System.out.println(Thread.currentThread().getName() + "--index--" + x));
		},"t2");
		t1.start();
		t2.start();
		/*
		等待当前线程执行完毕之后,再去执行别的线程，current thread wait for execute thread to die
		相对于父线程(启动别的线程的线程)来说，会等待调用了join方法的子线程执行结束之后，再去继续执行父线程
		如果不加的话，那么main线程和t1线程会交替执行，加了的话，会先等t1执行完毕之后再去执行main线程
		join必须在start之后调用

		虽然t1和t2都调用了join方法，其实很t1和t2是并行执行的
		main会等到t1和t2都指向完毕之后再去指向main方法
		 */
		t1.join();
		t2.join();
		IntStream.range(1, 1000).forEach(x -> System.out.println(Thread.currentThread().getName() + "--index--" + x));
		System.out.println("all thread has been done.");
	}
	@Test
	void testThreadJoin2() throws InterruptedException {
		Thread t1 = new Thread(() -> {
			IntStream.range(1, 1000).forEach(x -> System.out.println(Thread.currentThread().getName() + "--index--" + x));
		},"t1");
		t1.start();
		//指定等待的时间，如果到了等待的时间，t1线程没有结束就会去执行main线程
		t1.join(100,10);
		IntStream.range(1, 1000).forEach(x -> System.out.println(Thread.currentThread().getName() + "--index--" + x));
		System.out.println("all thread has been done.");

		//会保持程序一直执行
		//当前线程等当前线程结束，一直在循环
		Thread.currentThread().join();
	}
}
