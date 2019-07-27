package zhengwei.designpattern.future;

/**
 * 同步调用
 * 异步调用的几个角色
 * 1.Future         ->interface     ->代表了未来的一个凭证
 * 2.FutureTask     ->interface     ->将业务逻辑进行隔离
 * 3.FutureService  ->class         ->桥接Future和FutureService
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/27 13:52
 */
public class SyncInvoker {
	private static String get() throws InterruptedException {
		Thread.sleep(10_000L);
		return "FINISH";
	}

	public static void main(String[] args) throws InterruptedException {
		/*String result = get();
		System.out.println(result);*/
		FutureService futureService=new FutureService();
		futureService.submit(() -> {
			try {
				Thread.sleep(10_000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return "FINISH";
		},System.out::println);
		System.out.println("-------------");
		System.out.println("do other thing");
		Thread.sleep(1_000L);
		System.out.println("-------------");
//		System.out.println(future.get());
	}
}
