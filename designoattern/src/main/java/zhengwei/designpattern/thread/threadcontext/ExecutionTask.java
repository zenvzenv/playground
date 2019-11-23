package zhengwei.designpattern.thread.threadcontext;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/3 7:47
 */
public class ExecutionTask implements Runnable {
	private QueryAction queryFromDatabaseAction = new QueryFromDatabaseAction();
	private QueryAction queryFromHttpAction = new QueryFromHttpAction();

	@Override
	public void run() {
		/*//每个线程都会创建属于它自己的线程上下文，实例化自己的上下文实例
		final ThreadContext context = new ThreadContext();
		//处理完相关的业务逻辑之后往上下文中设置相应的属性值
		queryFromDatabaseAction.execute(context);
		System.out.println("the name query successfully");
		queryFromHttpAction.execute(context);
		System.out.println("the card id query successfully");
		System.out.println("the name is " + context.getName() + " and card id is " + context.getCardId());*/

		//处理完相关的业务逻辑之后往上下文中设置相应的属性值
		queryFromDatabaseAction.execute();
		System.out.println("the name query successfully");
		queryFromHttpAction.execute();
		System.out.println("the card id query successfully");
		ThreadContext context = ThreadActionContext.getInstance().getContext();
		System.out.println("the name is " + context.getName() + " and card id is " + context.getCardId());
		//清理数据
		ThreadActionContext.getInstance().cleanThreadLocal();
	}
}
