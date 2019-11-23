package zhengwei.thread.chapter07;

/**
 * ThreadGroup相关信息
 * 如果创建ThreadGroup不指定父ThreadGroup的话，那么默认会继承自父ThreadGroup
 * 子ThreadGroup对象能够访问到父ThreadGroup的一些只读指标
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/16 19:37
 */
public class CreateThreadGroup {
	public static void main(String[] args) {
		/*System.out.println(Thread.currentThread().getName());
		System.out.println(Thread.currentThread().getThreadGroup());*/
		//Constructor1->use name
		ThreadGroup tg1 = new ThreadGroup("TG1");
		new Thread(tg1, () -> {
			System.out.println(Thread.currentThread().getThreadGroup().getName());
			System.out.println(Thread.currentThread().getThreadGroup().getParent().getName());
			System.out.println(Thread.currentThread().getName());
			System.out.println("---------------------------------------");
			while (true) {
				//do something
			}
		}, "t1").start();

		//Constructor->use name,parent ThreadGroup
		ThreadGroup tg2 = new ThreadGroup(tg1, "TG2");
		new Thread(tg2, () -> {
			System.out.println(Thread.currentThread().getThreadGroup().getName());
			System.out.println(Thread.currentThread().getThreadGroup().getParent().getName());
			System.out.println(Thread.currentThread().getThreadGroup().getParent().getParent().getName());
			System.out.println(Thread.currentThread().getName());
			System.out.println("parent active count ->"+tg1.activeCount());
			System.out.println("---------------------------------------");
			while (true) {
				//do something
			}
		}, "t2").start();
	}
}
