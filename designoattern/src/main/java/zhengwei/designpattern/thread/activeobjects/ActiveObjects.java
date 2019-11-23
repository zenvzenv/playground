package zhengwei.designpattern.thread.activeobjects;

/**
 * 接收异步消息的主动对象
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/21 19:41
 */
public interface ActiveObjects<E> {
	Result<E> makeString(int count, char fillChar);
	void displayString(String text);
}
