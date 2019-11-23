package zhengwei.designpattern.immutable;

import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 不可变对象测试
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/27 11:18
 */
@AllArgsConstructor
public class ImmutableTest {
	private final int age;
	private final String name;
	private final List<String> list;

	/**
	 * 需要注意的是如果直接返回list，那么返回的是这个list的引用，那么别的线程还是可以对这个list进行修改
	 * 要么返回这个list的一个副本，要么返回这个list的时候标记为不可变
	 *
	 * @return 不可变的list
	 */
	public List<String> getList() {
		//返回不可变list集合
		return Collections.unmodifiableList(list);
	}

	public int getAge() {
		return age;
	}

	public String getName() {
		return name;
	}
}
