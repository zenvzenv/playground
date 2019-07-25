package zhengwei.thread.chapter10;

/**
 * 共享资源
 * 临界值
 * @author zhengwei AKA Awei
 * @since 2019/7/25 12:53
 */
public class Gate {
	private int counter = 0;
	private String name = "Nobody";
	private String address = "Nowhere";
	/**
	 * 临界值
	 * 只能有一个线程去通过它
	 */
	public synchronized void pass(String name, String address) {
		this.counter++;
		/*线程竞争*/
		this.name = name;
		this.address = address;
		verify();
	}

	private void verify() {
		if (this.name.charAt(0) != this.address.charAt(0)) {
			System.out.println("****broker****" + toString());
		}
	}

	/**
	 * 加锁，一次只允许一个线程进行操作
	 * @return
	 */
	@Override
	public synchronized String toString() {
		return "Gate{" +
				"counter=" + counter +
				", name='" + name + '\'' +
				", address='" + address + '\'' +
				'}';
	}
}
