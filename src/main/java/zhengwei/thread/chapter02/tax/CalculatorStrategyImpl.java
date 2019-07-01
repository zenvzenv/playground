package zhengwei.thread.chapter02.tax;

/**
 * @author zhengwei AKA Sherlock
 * @since 2019/7/1 18:54
 */
public class CalculatorStrategyImpl implements CalculatorStrategy {
	private final static double SALARY_RATE=0.1;
	private final static double BOUNS_RATE=0.1;
	@Override
	public double calculate(double salary, double bouns) {
		return salary*SALARY_RATE+bouns*BOUNS_RATE;
	}
}
