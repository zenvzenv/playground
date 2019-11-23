package zhengwei.designpattern.principle.singleresponsibility;

/**
 * 单一职责原则，方式二
 *
 * @author zhengwei AKA Sherlock
 * @since 2019/7/9 16:11
 */
public class SingleResponsibility2 {
	public static void main(String[] args) {
		RoadVehicle motorcycle = new RoadVehicle();
		motorcycle.run("motorcycle");
		AirVehicle plane = new AirVehicle();
		plane.run("plane");
		WaterVehicle ship = new WaterVehicle();
		ship.run("ship");
	}
}
/*
方案二分析
1.遵循了单一职责原则
2.但是改动比较大，即要将类分解，同时要修改客户端
3.改造：直接该Vehicle类，改动的代码比较少->方案三
 */

class RoadVehicle {
	public void run(String vehicle) {
		System.out.println(vehicle + " run on the road");
	}
}

class AirVehicle {
	public void run(String vehicle) {
		System.out.println(vehicle + " run in the air");
	}
}

class WaterVehicle {
	public void run(String vehicle) {
		System.out.println(vehicle + " run on the water");
	}
}