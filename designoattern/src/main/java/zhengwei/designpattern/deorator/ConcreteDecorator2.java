package zhengwei.designpattern.deorator;

/**
 * 具体构件角色
 * 类似于Buffered
 *
 * @author zhengwei AKA Awei
 * @since 2019/9/21 20:12
 */
public class ConcreteDecorator2 extends Decorator {
	public ConcreteDecorator2(Component component) {
		super(component);
	}

	@Override
	public void doSomething() {
		super.doSomething();
		this.doAnotherThing();
	}

	public void doAnotherThing() {
		System.out.println("功能c");
	}
}
