package zhengwei.designpattern.thread.twophasetermination;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/13 13:12
 */
public class AppServerClient {
	public static void main(String[] args) {
		AppServer appServer = new AppServer(12345);
		appServer.start();
	}
}
