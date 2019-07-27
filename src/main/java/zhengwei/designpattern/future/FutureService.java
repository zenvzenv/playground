package zhengwei.designpattern.future;

import java.util.function.Consumer;

/**
 * @author zhengwei AKA Awei
 * @since 2019/7/27 14:00
 */
public class FutureService {
	/**
	 * 异步获取结果
	 * 但是在我们调用Future的get方法时还是会阻塞
	 *
	 * @param task 我们封装的业务逻辑的实现
	 * @param <T>  结果泛型
	 * @return Future凭证
	 */
	public <T> Future<T> submit(final FutureTask<T> task) {
		//其实什么也不做，只是把Future凭证返回出去，供调用者以后对结果的获取
		AsyncFuture<T> asyncFuture = new AsyncFuture<>();
		//去调用FutureTask的call()方法，这个call()方法由我们自己来定义实现
		new Thread(() -> {
			//调用我们定义的具体的业务逻辑实现
			T result = task.call();
			//做完之后通知
			asyncFuture.done(result);
		}).start();
		return asyncFuture;
	}

	/**
	 * 改进的版本
	 * 可以不返回Future凭证，新加一个参数直接对结果进行消费
	 * 也就不用阻塞住了，等到业务逻辑执行完毕之后自动去消费了(或者做其他操作)
	 *
	 * @param task     我们封装的业务逻辑的实现
	 * @param consumer 模拟对我们逻辑产物的消费，相当于回调函数
	 * @param <T>      泛型
	 * @return Future凭证，可以不用返回
	 */
	public <T> Future<T> submit(final FutureTask<T> task, final Consumer<T> consumer) {
		//其实什么也不做，只是把Future凭证返回出去，供调用者以后对结果的获取
		AsyncFuture<T> asyncFuture = new AsyncFuture<>();
		//去调用FutureTask的call()方法，这个call()方法由我们自己来定义实现
		new Thread(() -> {
			//调用我们定义的具体的业务逻辑实现
			T result = task.call();
			//做完之后通知
			asyncFuture.done(result);
			consumer.accept(result);
		}).start();
		return asyncFuture;
	}
}
