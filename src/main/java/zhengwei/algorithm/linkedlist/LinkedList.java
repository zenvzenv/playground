package zhengwei.algorithm.linkedlist;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/13 17:27
 */
public interface LinkedList<E> {
	/**
	 * 无序添加元素
	 * @param e 需要添加的元素
	 */
	void add(E e);

	/**
	 * 顺序添加节点
	 */
	void addByOrder(E e);

	/**
	 * 删除节点
	 */
	void delete();

	/**
	 * 遍历链表并打印
	 */
	void list();

	/**
	 * 更新节点信息
	 */
	void update();
}
