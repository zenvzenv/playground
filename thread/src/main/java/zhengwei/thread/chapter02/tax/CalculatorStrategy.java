package zhengwei.thread.chapter02.tax;

/**
 * 计算税务的专用接口
 * @author zhengwei AKA Sherlock
 * @since 2019/7/1 18:50
 */
@FunctionalInterface
public interface CalculatorStrategy {
	double calculate(double salary,double bouns);
}
