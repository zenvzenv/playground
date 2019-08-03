package zhengwei.designpattern.threadcontext;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/3 7:55
 */
public class QueryFromHttpAction implements QueryAction {
	@Override
	public void execute() {
		System.out.println("query form http...");
		try {
			Thread.sleep(1_000L);
			ThreadActionContext.getInstance().getContext().setCardId(getCardId(ThreadActionContext.getInstance().getContext().getName()));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void execute(ThreadContext context) {
		System.out.println("query from http...");
		try {
			Thread.sleep(1_000L);
			String cardId = getCardId(context.getName());
			context.setCardId(cardId);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private String getCardId(String name) {
		System.out.println("get card id by name...");
		try {
			Thread.sleep(1_000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "1234567890->" + Thread.currentThread().getName();
	}
}
