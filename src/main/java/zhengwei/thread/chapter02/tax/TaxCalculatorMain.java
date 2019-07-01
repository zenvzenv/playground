package zhengwei.thread.chapter02.tax;

/**
 * 模仿Thread的策略模式
 * @author zhengwei AKA Sherlock
 * @since 2019/7/1 18:39
 */
public class TaxCalculatorMain {
	public static void main(String[] args) {
		/*TaxCalculator taxCalculator =new TaxCalculator(11300d,0d){
			@Override
			protected double calcTax(double salary,double bouns) {
				return bouns*0.1+salary*0.12;
			}
		};
		double tax = taxCalculator.calculator(taxCalculator.getSalary(), taxCalculator.getBonus());
		System.out.println(tax);*/
		TaxCalculator taxCalculator=new TaxCalculator(11300d,0d, (salary, bouns) -> salary*0.1+bouns*0.12);
		System.out.println(taxCalculator.calculator());
	}
}
