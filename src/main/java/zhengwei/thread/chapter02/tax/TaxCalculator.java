package zhengwei.thread.chapter02.tax;

import lombok.Data;

/**
 * 税率计算器
 * @author zhengwei AKA Sherlock
 * @since 2019/7/1 18:38
 */
@Data
public class TaxCalculator {
	private double salary;
	private double bonus;
	private CalculatorStrategy calculatorStrategy;

	public TaxCalculator(double salary, double bonus, CalculatorStrategy calculatorStrategy) {
		this.salary = salary;
		this.bonus = bonus;
		this.calculatorStrategy = calculatorStrategy;
	}

	protected double calcTax(){
		return calculatorStrategy.calculate(salary,bonus);
	}

	public double calculator(){
		return calcTax();
	}
}
