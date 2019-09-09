package zhengwei.thread.chapter11;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 实现简单的ThreadLocal
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/1 12:39
 */
public class MyThreadLocal<T> {
	private final Map<Thread, T> storage = new HashMap<>();

	/**
	 * 设置值
	 *
	 * @param t 当前线程需要设置的值
	 */
	public void set(T t) {
		synchronized (storage) {
			Thread key = Thread.currentThread();
			storage.put(key, t);
		}
	}

	public T get() {
		synchronized (storage) {
			Thread key = Thread.currentThread();
			T t = storage.get(key);
			if (Objects.isNull(t)) {
				return initValue();
			}
			return t;
		}
	}

	/**
	 * 初始值，如果当前线程没有设置值的话，返回该默认值
	 * 运行子类重写此方法
	 *
	 * @return 默认值
	 */
	protected T initValue() {
		return null;
	}
}
