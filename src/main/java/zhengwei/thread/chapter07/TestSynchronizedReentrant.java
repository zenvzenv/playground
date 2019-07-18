package zhengwei.thread.chapter07;

/**
 * 测试下synchronized的可重入锁
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/18 11:01
 */
public class TestSynchronizedReentrant {
	public static void main(String[] args) {
		Child child = new Child();
		Parent parent = new Parent();
		new Thread(parent::parentDoSomething, "parent1").start();
//		new Thread(parent::parentDoSomething, "parent2").start();
		new Thread(child::childDoSomething, "child1").start();
	}
}

class Parent {
	synchronized void parentDoSomething() {
		try {
			System.out.println("Parent do something ->" + Thread.currentThread().getName()+" start");
			Thread.sleep(10_000L);
			System.out.println("Parent do something ->" + Thread.currentThread().getName()+" end");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

class Child extends Parent {
	synchronized void childDoSomething() {
		try {
			System.out.println("Child do something ->" + Thread.currentThread().getName()+" start");
//			Thread.sleep(5_000L);
			childDoAnotherThing();
			System.out.println("Child do something ->" + Thread.currentThread().getName()+" end");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private synchronized void childDoAnotherThing() {
		try {
			System.out.println("Child do another thing ->" + Thread.currentThread().getName()+" start");
//			Thread.sleep(5_000L);
			super.parentDoSomething();
			System.out.println("Child do another thing ->" + Thread.currentThread().getName()+" end");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}