package zhengwei.designpattern.future;

/**
 * 封装代码逻辑
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/27 13:59
 */
public interface FutureTask<T> {
	T call();
}
