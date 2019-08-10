package zhengwei.algorithm.queue;

import java.util.concurrent.BlockingDeque;

/**
 * 队列
 * 先进先出，是一个有序列表，可以使用数组和链表来实现
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/8 10:17
 */
public interface Queue<E> {
	/**
	 * 队列是否已经满了
	 * @return true-满了，false-没满
	 */
	boolean isFull();

	/**
	 * 队列是否为空
	 * @return true-为空，false-不为空
	 */
	boolean isEmpty();

	/**
	 * 获取队列中的值
	 * @return 队列头的数组
	 */
	E get() throws RuntimeException;

	/**
	 * 往队列中添加值
	 */
	void add(E e);

	/**
	 * 显示队列的头元素
	 * @return 队列头元素
	 */
	E head();

	/**
	 * 显示队列中所有元素
	 */
	void show();
}
