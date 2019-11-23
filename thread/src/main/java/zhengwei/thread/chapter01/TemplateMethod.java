package zhengwei.thread.chapter01;

/**
 * 模板方法类
 * 模拟Thread类
 * @author zhengwei AKA Sherlock
 * @since 2019/6/22 19:59
 */
public abstract class TemplateMethod {
	public final void print(String string){
		System.out.println("---------------------");

		System.out.println("---------------------");
	}
	protected abstract void wrapPrint(String message);

	public static void main(String[] args) {

	}
}
