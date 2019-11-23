package zhengwei.designpattern.deorator;

/**
 * 具体构建角色
 * 类似于FileInputStream
 *
 * @author zhengwei AKA Awei
 * @since 2019/9/21 20:01
 */
public class ConcreteComponent implements Component {
	@Override
	public void doSomething() {
		System.out.println("功能A");
	}
}
