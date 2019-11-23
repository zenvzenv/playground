package zhengwei.thread.chapter04;

/**
 * @author zhengwei AKA Sherlock
 * @since 2019/7/6 11:26
 */
public class ThreadJoin {
	public static void main(String[] args) throws InterruptedException {
		System.out.println("App has been started at "+System.currentTimeMillis());
		Thread t1=new Thread(new CaptureRunnable("M1",10*1000L));
		Thread t2=new Thread(new CaptureRunnable("M2",20*1000L));
		Thread t3=new Thread(new CaptureRunnable("M3",15*1000L));

		t1.start();
		t2.start();
		t3.start();
		//让main线程等待t1,t2和t3都结束之后再去结束整个程序
		//如果不加上join，那么main不会去等待t1、t2和t3线程执行完毕，这样还没等线程完成必要工作就结束了程序
		t1.join();
		t2.join();
		t3.join();

		System.out.println("App has been end at "+System.currentTimeMillis()+",and start to save data.");
	}
}
class CaptureRunnable implements Runnable{
	//采集的机器名
	private String machineName;
	//花费的时间
	private long spendTime;

	public CaptureRunnable(String machineName, long spendTime) {
		this.machineName = machineName;
		this.spendTime = spendTime;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(this.spendTime);
			System.out.println(this.machineName+" has been end,and timestamp is " + System.currentTimeMillis()+",spendTime is "+this.spendTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}