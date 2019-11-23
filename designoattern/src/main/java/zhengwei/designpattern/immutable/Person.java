package zhengwei.designpattern.immutable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 不可变对象
 * JDK官方建议
 * 1.不要为类的属性添加setter方法
 * 2.类中所有的属性都要是private和final的
 * 3.不要让子类去重载方法
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/27 10:47
 */
@AllArgsConstructor
@Getter
@ToString
final class Person {
	private final String name;
	private final String address;
}
