package zhengwei.designpattern.future;

/**
 * 获取未来结果的一个凭证，凭Future去获取最终结果
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/27 13:55
 */
public interface Future<T> {
	T get() throws InterruptedException;
}
