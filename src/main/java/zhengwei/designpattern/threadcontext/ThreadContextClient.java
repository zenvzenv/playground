package zhengwei.designpattern.threadcontext;

import java.util.stream.IntStream;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/3 8:11
 */
public class ThreadContextClient {
	public static void main(String[] args) {
		IntStream.rangeClosed(1, 5)
				.forEach(i -> new Thread(new ExecutionTask()).start());
	}
}
