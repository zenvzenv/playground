package zhengwei.thread.chapter08;

import org.junit.jupiter.api.Test;

/**
 * 介绍volatile关键字
 * 程序运行时，有这样一个架构
 * 我们的程序主要是在主存RAM和CPU中去运行的，因为CPU的速度相比较与主存是非常快的，所以在主存与CPU之间加了多级缓存
 * 越靠近CPU的缓存速度是越快的，如果我们声明的变量不加上volatile关键字修饰的话，CPU在程序运行的时候会把变量从主存中拷贝一份进CPU缓存中
 * 这样其实是每个线程都会去CPU缓存中去读写数据，而不会去主存中去读写数据了，导致了数据的不一致性
 *
 * 解决数据不一致的方法
 *  1.给数据总线加锁，总线包括：数据总线，地址总线，控制总线，LOCK#
 *  2.CPU高速缓存一致性协议
 *      核心西乡：1.当CPU写入数据的时候，如果发现该变量被共享的（也就是说，在其他CPU中存在该变量的副本），会发出一个信号，通知其他CPU该变量的缓存无效
 *               2.当其他CPU访问该变量的时候，重新导主内存中获取
 *
 *  并发编程的三个重要概念
 *  1.原子性：即一个或多个操作，要么全部都执行执行过程中不会被别的线程打断；要么都不执行
 *  2.可见性：即多个线程访问一个变量时。一个线程修改了这个变量的值，其他线程能够立即看到修改的值
 *  3.有序性：即程序执行的顺序按照代码的先后顺序执行
 *      可能会有指令重排序的问题发生：一般来说，处理器为了提高程序运行效率，可能会对代码进行优化，它不保证程序中各个语句的执行顺序与代码中定义的顺序一致，但是它保证程序最终输出的结果和代码顺序执行的结果一致
 *      当然，CPU对代码重排序是会考虑到各个代码之间的依赖关系的
 *      指令重排序不会影响单个线程的执行，但是会影响多线程并发执行的正确性
 *
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/20 13:12
 */
public class VolatileKeyWord {
	private static int INIT_VALUE = 0;
	private static final int MAX_INIT = 5;

	@Test
	void testVolatile1() throws InterruptedException {
		/*
		在这个线程中只有对变量的读操作，那么JVM会对这段代码进行一些优化
		这个线程只去读cache中的数据，不会再去主存中去刷新
		 */
		new Thread(() -> {
			while (INIT_VALUE < MAX_INIT) {
				System.out.printf("the value is [%d]\n", INIT_VALUE);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, "reader").start();
		new Thread(() -> {
			while (INIT_VALUE < MAX_INIT) {
				System.out.printf("the value is update to [%d]\n", ++INIT_VALUE);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, "writer").start();
		Thread.sleep(10_000L);
	}

	/**
	 * 基本变量在赋值时是原子性的，可以通过字节码来看出这一点
	 */
	static void baseTypeAtomic(){
		int a=10;
		System.out.println();
		int b=a;
		System.out.println();
		a++;
		a=a+1;
	}
	private static volatile int initValue=0;
	private static final int maxValue=5;
	public static void main(String[] args) {
		new Thread(()->{
			int localValue=initValue;
			while (localValue<maxValue){
				System.out.println("The value update to "+initValue);
				localValue=initValue;
			}
		},"reader").start();
		new Thread(()->{
			int localValue=initValue;
			while (localValue<maxValue){
				System.out.println("Update the value to "+ ++localValue);
				initValue=localValue;
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		},"updater").start();
	}
}
