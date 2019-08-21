package zhengwei.designpattern.thread.workerthread;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/21 13:36
 */
@Data
@AllArgsConstructor
@ToString
public class Request {
	private final String name;
	private final int number;

	public void execute() {
		System.out.println(Thread.currentThread().getName() + " execute " + this);
	}
}
