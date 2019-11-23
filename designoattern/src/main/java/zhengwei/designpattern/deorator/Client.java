package zhengwei.designpattern.deorator;

/**
 * 客户端
 *
 * @author zhengwei AKA Awei
 * @since 2019/9/21 20:13
 */
public class Client {
	public static void main(String[] args) {
		Component component = new ConcreteDecorator2(new ConcreteDecorator1(new ConcreteComponent()));
		component.doSomething();
	}
}
