package zhengwei.algorithm.queue;

import java.util.Scanner;

/**
 * 单向数组队列(一次性队列)
 * 问题：
 *  目前的这个队列只能使用一次，达不到复用的效果
 * 优化：
 *  后期可以考虑加锁，以实现同步队列的效果
 *  做成环形队列，已达到服用的效果
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/7 15:15
 */
public class UnidirectionalArrayQueue<E> implements Queue<E> {
	//队列的头部
	private int front;
	//队列的尾部
	private int rear;
	private final E[] queue;
	private int maxSize;
	//使用Object数组来左到泛型数组
	private Object[] elementData;

	public UnidirectionalArrayQueue(int size) {
		elementData = new Object[size];
		queue = (E[]) elementData;
		this.maxSize = queue.length;
		this.front = -1;
		this.rear = -1;
	}

	@Override
	public boolean isFull() {
		return this.rear == this.maxSize - 1;
	}

	@Override
	public boolean isEmpty() {
		return this.front == this.rear;
	}

	@Override
	public E get() {
		if (isEmpty()) {
			throw new RuntimeException("队列以空，不能再取数据");
		}
		front++;
		return queue[front];
	}

	@Override
	public void add(E e) {
		if (isFull()) {
			System.out.println("队列已满，不能再添加数据");
			return;
		}
		rear++;
		queue[rear] = e;
	}

	@Override
	public E head() {
		if (isEmpty()) {
			throw new RuntimeException("队列为空，没有数据");
		}
		//只是显示元素，而不是取出元素
		return queue[front + 1];
	}

	@Override
	public void show() {
		if (isEmpty()) {
			System.out.println("队列为空，没有数据");
			return;
		}
		for (int i = 0; i < queue.length; i++) {
			System.out.printf("queue[%d]=%d\n", i, queue[i]);
		}
	}

	public static void main(String[] args) {
		UnidirectionalArrayQueue<Integer> queue = new UnidirectionalArrayQueue<>(3);
		char key = ' ';
		Scanner scanner = new Scanner(System.in);
		boolean loop = true;
		while (loop) {
			key=scanner.next().charAt(0);
			switch (key) {
				case 's':
					queue.show();
					break;
				case 'a':
					System.out.println("输入一个数：");
					int v = scanner.nextInt();
					queue.add(v);
					break;
				case 'g':
					try {
						System.out.println(queue.get());
					} catch (Exception e) {
						e.printStackTrace();
					}
				case 'h':
					System.out.println(queue.head());
					break;
				case 'e':
					loop = false;
					break;
				default:
					break;
			}
		}
	}
}
