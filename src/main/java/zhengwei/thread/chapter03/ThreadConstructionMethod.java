package zhengwei.thread.chapter03;

import org.junit.jupiter.api.Test;

/**
 * @author zhengwei AKA Sherlock
 * @since 2019/7/1 19:21
 */
public class ThreadConstructionMethod {
	public static void main(String[] args) {
		Thread t0=new Thread();
		Thread t1=new Thread();
		System.out.println(t0.getName());
		System.out.println(t1.getName());
		Thread t2=new Thread(()-> System.out.println("thread name -> "+Thread.currentThread().getName()),"runnable");
		t2.start();
	}

	/**
	 * 创建线程的第一种方法，默认有一个线程名，以Thread-开头，从0开始计数
	 * 如果没有传入Runnable接口实例或没有复写run方法，那么线程启动时将不会做任何事
	 */
	@Test
	void createThread1(){
		Thread t0=new Thread();
		Thread t1=new Thread();
		System.out.println(t0.getName());
		System.out.println(t1.getName());
	}

	/**
	 * 创建线程的第二种方法，传入Runnable和name
	 * 如果传入了Runnable接口实例或重写run方法，那么线程启动时会去执行相应的代码
	 */
	@Test
	void createThread2(){
		Thread t0=new Thread("zhengwei1"){
			@Override
			public void run() {
				System.out.println("thread name->"+Thread.currentThread().getName());
			}
		};
		Thread t1=new Thread(()->{
			System.out.println("thread name->"+Thread.currentThread().getName());
		},"zhengwei2");
	}

	/**
	 *
	 */
	@Test
	void createThread3(){
		Thread t=new Thread(()-> System.out.println(Thread.currentThread().getName()),"runnable");
		//main方法的ThreadGroup
		System.out.println(Thread.currentThread().getThreadGroup().getName());
		/*
		自己生成的线程的ThreadGroup
		因为我们创建线程的时候并没有指明ThreadGroup，那么会沿用父线程的ThreadGroup，所谓的父线程就是启动该线程的线程
		 */
		System.out.println(t.getThreadGroup().getName());
		ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
	}
}
