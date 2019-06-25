package zhengwei.thread.chapter02.bank1;

/**
 * 出票窗口
 * @author zhengwei AKA Sherlock
 * @since 2019/6/25 19:22
 */
public class TicketWindow extends Thread {
	private final static int max=50;
	/*
	当前票号,静态变量在类实例化的时候内存中只会初始化一次。
	缺点是：静态变量的生命周期会比较长，因为Hotspot会把静态类信息会放进方法区中，而不是方法栈中
	生命周期会随着类的主动加载之后进入到JVM内存，直到类被卸载也不一定会被清楚
	 */
	private static int index=1;
	//柜台
	private String name;
	public TicketWindow(String name){
		this.name=name;
	}
	@Override
	public void run() {
		while (index<=50){
			System.out.println(this.name+"当前的号码是->"+index++);
		}
	}
}
