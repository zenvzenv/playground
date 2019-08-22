package zhengwei.designpattern.thread.activeobjects;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/21 20:23
 */
class ActiveObjectsProxy<E> implements ActiveObjects<E> {
	private final SchedulerThread schedulerThread;
	private final Servant servant;

	public ActiveObjectsProxy(SchedulerThread schedulerThread, Servant servant) {
		this.schedulerThread = schedulerThread;
		this.servant = servant;
	}

	@Override
	public Result<E> makeString(int count, char fillChar) {
		FutureResult<E> futureResult = new FutureResult<>();
		schedulerThread.invoke(new MakeStringRequest<>(servant, futureResult, count, fillChar));
		return futureResult;
	}

	@Override
	public void displayString(String text) {
		schedulerThread.invoke(new DisplayStringRequest(servant, text));
	}
}
