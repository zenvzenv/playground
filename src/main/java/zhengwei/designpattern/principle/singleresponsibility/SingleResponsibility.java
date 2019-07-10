package zhengwei.designpattern.principle.singleresponsibility;

/**
 * 单一职责模式，方案三
 *
 * @author zhengwei AKA Sherlock
 * @since 2019/7/9 16:35
 */
public class SingleResponsibility {
	public static void main(String[] args) {
		Vehicle2 vehicle2 = new Vehicle2();
		vehicle2.roadRun("motorcycle");
		vehicle2.airRun("plane");
		vehicle2.waterRun("ship");
	}
}
/*
方案三分析：
1.这种方式没有对原来的Vehicle有很大的改动，只是增加了方法
2.这里虽然没有在类级别上遵守单一职责原则，但是在方法级别上遵守了单一职责原则
 */

class Vehicle2 {
	void roadRun(String vehicle) {
		System.out.println(vehicle + " run on the road");
	}

	void airRun(String vehicle) {
		System.out.println(vehicle + " run in the air");
	}

	void waterRun(String vehicle) {
		System.out.println(vehicle + " run on the water");
	}
}