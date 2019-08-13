package zhengwei.algorithm.queue;

import java.util.Scanner;

/**
 * 循环队列
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/12 8:56
 */
public class CircularArrayQueue<E> implements Queue<E> {
	//队列的头部，就指向真正的队列头元素
	private int front;
	//队列的尾部，指向队尾的前一个元素，队列中预留一个位置
	private int rear;
	//队列数组
	private final E[] queue;
	//队列最大容量
	private int maxSize;
	//用来帮助泛型强转
	private Object[] elementData;

	public CircularArrayQueue(int size) {
		elementData = new Object[size];
		queue = (E[]) elementData;
		this.front = 0;
		this.rear = 0;
		this.maxSize = size;
	}

	@Override
	public boolean isFull() {
		//因为队列预留出一个，所以队列的有效长度是maxSize-1，所以
		return (rear + 1) % maxSize == front;
	}

	@Override
	public boolean isEmpty() {
		return front == rear;
	}

	@Override
	public E get() throws RuntimeException {
		if (isEmpty()) {
			throw new IndexOutOfBoundsException("队列为空");
//			System.out.println("队列为空");
		}
		//front指向的就是队列的头部元素，获取队列元素的话就直接获取front指向的元素即可
		E value = queue[front];
		//front不能无限制的向上递增，需要取模，front+1
		front = (front + 1) % maxSize;
		System.out.println(front);
		return value;
	}

	@Override
	public void add(E e) {
		if (isFull()) {
//			throw new IndexOutOfBoundsException("队列已满");
			System.out.println("队列已满");
			return;
		}
		queue[rear] = e;
		rear = (rear + 1) % maxSize;
		System.out.println(rear);
	}

	@Override
	public E head() {
		return queue[front];
	}

	/**
	 * 打印出队列中所有的元素
	 */
	@Override
	public void show() {
		for (int i = front; i < front + size(); i++) {
			System.out.printf("arr[%d]=%d\n", i % maxSize, queue[i % maxSize]);
		}
	}

	/**
	 * 求出当前队列的有效长度
	 *
	 * @return 队列的有效长度
	 */
	private int size() {
		return (rear + maxSize - front) % maxSize;
	}
	public static void main(String[] args) {
		Queue<Integer> queue = new CircularArrayQueue<>(4);
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
					break;
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
