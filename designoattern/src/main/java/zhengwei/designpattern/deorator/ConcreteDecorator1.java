package zhengwei.designpattern.deorator;

/**
 * 具体构建角色
 * 类似于BufferedInputStream
 *
 * @author zhengwei AKA Awei
 * @since 2019/9/21 20:07
 */
public class ConcreteDecorator1 extends Decorator {
	public ConcreteDecorator1(Component component) {
		super(component);
	}

	@Override
	public void doSomething() {
		super.doSomething();
		this.doAnotherThing();
	}

	public void doAnotherThing() {
		System.out.println("功能B");
	}
}
