package zhengwei.thread.chapter02.bank2;

/**
 * @author zhengwei AKA Sherlock
 * @since 2019/6/25 19:37
 */
public class TicketWindowRunnable implements Runnable {
	private int index=0;
	private final static int MAX=50;
	@Override
	public void run() {
		while (index<=50){
			System.out.println(Thread.currentThread().getName()+"柜台的票号是->"+index++);
		}
	}
}
