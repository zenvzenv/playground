package zhengwei.thread.chapter09;

import java.util.Arrays;

/**
 * @author zhengwei AKA Awei
 * @since 2019/7/23 12:58
 */
public class ThreadLifecycleClient {
	public static void main(String[] args) {
		new ThreadLifecycleObserver().concurrentQuery(Arrays.asList("1", "2"));
	}
}
