package zhengwei.java8.lambda;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 字符排序
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/4 10:45
 */
public class StringComparator {
	public static void main(String[] args) {
		//倒序
		List<String> names = Arrays.asList("zhangsan", "lisi", "wangwu");
		//方法一
		Collections.sort(names, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o2.compareTo(o1);
			}
		});
		System.out.println(names);
		//方法二
		//expression o2.compareTo(o1)
		//statment {return o2.compareTo(o1);}
		Collections.sort(names, (o1, o2) -> {
			return o1.compareTo(o2);
		});
		Collections.sort(names, (o1, o2) -> o1.compareTo(o2));
		//方法三
		names.sort(Comparator.reverseOrder());
	}
}
