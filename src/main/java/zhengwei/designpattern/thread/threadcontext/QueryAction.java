package zhengwei.designpattern.thread.threadcontext;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/3 7:48
 */
public interface QueryAction {
	/**
	 * 第一个版本，手动指定线程上下文
	 * @param context 线程上下文
	 */
	void execute(ThreadContext context);

	/**
	 * 第二个版本，利用ThreadLocal来自动切换线程上下文
	 */
	void execute();
}
