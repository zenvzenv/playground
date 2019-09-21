package zhengwei.designpattern.deorator;

import lombok.AllArgsConstructor;

/**
 * 装饰角色
 * 需要实现抽象构建角色
 * 还需要持有一个构建角色的引用
 * 类似与FilterInputStream
 *
 * @author zhengwei AKA Awei
 * @since 2019/9/21 20:02
 */
@AllArgsConstructor
public class Decorator implements Component {
	//一个构建角色的引用
	private final Component component;
	@Override
	public void doSomething() {
		//交由成员变量->构建角色去完成
		component.doSomething();
	}
}
