package zhengwei.thread.chapter10;

/**
 * @author zhengwei AKA Awei
 * @since 2019/7/25 12:57
 */
public class Person extends Thread {
	private final String name;
	private final String address;
	private final Gate gate;

	public Person(String name, String address, Gate gate) {
		this.name = name;
		this.address = address;
		this.gate = gate;
	}

	@Override
	public void run() {
		System.out.println(this.name+" begin");
		while (true) {
			this.gate.pass(this.name,this.address);
		}
	}
}
