package zhengwei.designpattern.principle.singleresponsibility;

/**
 * 设置模式七大原则之一：单一职责原则，方式一
 * 一个类只负责一项职责
 *
 * @author zhengwei AKA Sherlock
 * @since 2019/7/9 14:17
 */
public class SingleResponsibility1 {
	public static void main(String[] args) {
		Vehicle vehicle = new Vehicle();
		vehicle.run("motorcycle");
		vehicle.run("plane");
		vehicle.run("ship");
	}
}

/**
 * 交通工具类
 * 方式1
 * 1.在方式1中的run方法中，违反了唯一职责原则
 * 2.解决方案：根据交通工具的不同职责，分解成不同的类
 */
class Vehicle {
	void run(String vehicle) {
		System.out.println(vehicle + " run on the load");
	}
}