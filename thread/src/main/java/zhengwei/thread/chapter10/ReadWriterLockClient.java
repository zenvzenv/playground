package zhengwei.thread.chapter10;

/**
 * ReadWriteLock design pattern
 * Read-Write design pattern
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/26 19:41
 */
public class ReadWriterLockClient {
	public static void main(String[] args) {
		final SharedData data =new SharedData(10);
		new ReadWorker(data).start();
		new ReadWorker(data).start();
		new ReadWorker(data).start();
		new ReadWorker(data).start();
		new ReadWorker(data).start();
		new WriteWorker(data,"qwertyuiop").start();
		new WriteWorker(data,"QWERTYUIOP").start();
	}
}
