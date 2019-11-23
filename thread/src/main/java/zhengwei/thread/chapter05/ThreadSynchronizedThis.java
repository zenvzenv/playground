package zhengwei.thread.chapter05;

/**
 * @author zhengwei AKA Sherlock
 * @since 2019/7/12 20:26
 */
public class ThreadSynchronizedThis {
	public static void main(String[] args) {
		SynchronizedLockThis synchronizedLockThis=new SynchronizedLockThis();
		new Thread(synchronizedLockThis::m1).start();//1
		new Thread(synchronizedLockThis::m2).start();//2
		new Thread(synchronizedLockThis::m3).start();//3
		new Thread(synchronizedLockThis::m4).start();//4
	}
}

/**
 * synchronized修饰在方法上的话，锁定的是class锁对应的实例对象，也就是对this上锁
 * 注意：修饰在方法上的synchronized在字节码中是体现不出monitorenter和monitorexit的
 * 如m1和m2方法，都是被synchronized修饰在方法上的，那么我们这时其实没有显示指定锁对象的，那么锁定的是class对应的那个实例对象即this
 * 所以main方法中1和2会发生锁竞争
 *
 * synchronized修饰在方法内部，锁定的是我们指定的任意对象
 * 注意：修饰在方法内部的synchronized在字节码中是能够体现monitorenter和monitorexit的，一个monitorenter可以对应多个monitorexit
 * 如m3和m4方法，都是锁定的我们指定的LOCK对象
 * 所以main方法中3和4会发生锁竞争
 *
 * 但是m1,m2和m3,m4所锁定的对象是不同的，那么在调用m1,m2和m3,m4的时候是不会发生锁竞争的
 */
class SynchronizedLockThis {
	//自定义锁
	private static final Object LOCK=new Object();
	synchronized void m1(){
		try {
			System.out.println(Thread.currentThread().getName()+"--lei le");
			Thread.sleep(5_000L);
			System.out.println(Thread.currentThread().getName()+"--zou le");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	synchronized void m2(){
		try {
			System.out.println(Thread.currentThread().getName()+"--lei le");
			Thread.sleep(5_000L);
			System.out.println(Thread.currentThread().getName()+"--zou le");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	void m3(){
		synchronized (LOCK){
			try {
				System.out.println(Thread.currentThread().getName()+"--lei le");
				Thread.sleep(5_000L);
				System.out.println(Thread.currentThread().getName()+"--lei le");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	void m4(){
		synchronized (LOCK){
			try {
				System.out.println(Thread.currentThread().getName()+"--lei le");
				Thread.sleep(5_000L);
				System.out.println(Thread.currentThread().getName()+"--lei le");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}