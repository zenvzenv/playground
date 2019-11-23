package zhengwei.thread.chapter10;

/**
 * @author zhengwei AKA Awei
 * @since 2019/7/25 13:09
 */
public class Client {
	public static void main(String[] args) {
		Gate gate = new Gate();
		Person bj = new Person("baobao", "beijing", gate);
		Person sh = new Person("shanglao", "shanghai", gate);
		Person gz = new Person("guanglao", "guangzhou", gate);
		bj.start();
		sh.start();
		gz.start();
	}
}
