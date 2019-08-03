package zhengwei.designpattern.threadcontext;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/3 7:50
 */
public class QueryFromDatabaseAction implements QueryAction {
	@Override
	public void execute() {
		System.out.println("query from data base...");
		try {
			Thread.sleep(1_000L);
			String name = "zhengwei->" + Thread.currentThread().getName();
			ThreadActionContext.getInstance().getContext().setName(name);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void execute(ThreadContext context) {
		System.out.println("query from data base ...");
		try {
			Thread.sleep(1_000L);
			String name = "zhengwei->" + Thread.currentThread().getName();
			context.setName(name);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
