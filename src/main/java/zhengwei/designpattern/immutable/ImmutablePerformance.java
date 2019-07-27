package zhengwei.designpattern.immutable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * @author zhengwei AKA Awei
 * @since 2019/7/27 11:23
 */
public class ImmutablePerformance {
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		SyncObj syncObj = new SyncObj();
		syncObj.setName("zhengwei");
		for (long i = 0; i < 10000000; i++) {
			System.out.println(syncObj.toString());
		}
		long end = System.currentTimeMillis();
		System.out.println("Time -> " + (end - start));
	}
}

/**
 * 不可变对象
 */
@ToString
@AllArgsConstructor
final class ImmutableObj {
	private final String name;
}

/**
 * 可变对象
 */
class SyncObj {
	private String name;

	public synchronized String getName() {
		return name;
	}

	public synchronized void setName(String name) {
		this.name = name;
	}

	@Override
	public synchronized String toString() {
		return "{ " + this.name + " }";
	}
}