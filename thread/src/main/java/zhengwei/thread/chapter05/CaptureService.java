package zhengwei.thread.chapter05;

import java.util.*;
import java.util.stream.Stream;

/**
 * 线程综合小案例
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/13 15:12
 */
public class CaptureService {
	private static final LinkedList<Object> CONTROLS = new LinkedList<>();
	private static final int MAX = 5;

	public static void main(String[] args) {
		List<Thread> workers = new ArrayList<>();
		Stream.of("m1", "m2", "m3", "m4", "m5", "m6", "m7", "m8", "m9", "m10")
				.map(CaptureService::createCaptureThread)
				.forEach(t -> {
					t.start();
					workers.add(t);
				});
		workers.forEach(t -> {
			try {
				//子线程去调用join方法，父线程等待子线程的结束
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		System.out.println("All of capture work has been finished");
	}

	private static Thread createCaptureThread(String name) {
		return new Thread(() -> {
			Optional.of("The worker [" + Thread.currentThread().getName() + "] begin capture data").ifPresent(System.out::println);
			synchronized (CONTROLS) {
				//控制同时工作的线程的数量在5以内
				while (CONTROLS.size() > MAX) {
					try {
						CONTROLS.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				//synchronized主要是要维护工作容器中的线程
				CONTROLS.addLast(new Object());
			}
			Optional.of("The worker [" + Thread.currentThread().getName() + "] is working...").ifPresent(System.out::println);
			//do something
			try {
				Thread.sleep(10_000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//工作结束之后唤醒所有WAITING的线程
			synchronized (CONTROLS) {
				Optional.of("The worker [" + Thread.currentThread().getName() + "] is end capture data...").ifPresent(System.out::println);
				//FIFO
				CONTROLS.removeFirst();
				CONTROLS.notifyAll();
			}
		}, name);
	}
}
