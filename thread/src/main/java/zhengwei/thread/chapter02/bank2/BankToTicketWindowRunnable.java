package zhengwei.thread.chapter02.bank2;

/**
 * @author zhengwei AKA Sherlock
 * @since 2019/6/25 19:39
 */
public class BankToTicketWindowRunnable {
	public static void main(String[] args) {
		final TicketWindowRunnable ticketWindowRunnable=new TicketWindowRunnable();
		new Thread(ticketWindowRunnable,"一号柜台").start();
		new Thread(ticketWindowRunnable,"二号柜台").start();
		new Thread(ticketWindowRunnable,"三号柜台").start();
	}
}
