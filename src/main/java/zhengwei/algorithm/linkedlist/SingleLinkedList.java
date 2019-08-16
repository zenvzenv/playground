package zhengwei.algorithm.linkedlist;

import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.val;

/**
 * 单向链表
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/13 19:58
 */
public class SingleLinkedList<E> implements LinkedList<E> {
	//头节点
	private Node<E> head = new Node<>(null, null);

	/*
	添加元素不考虑顺序
	将最后的节点的next指向新的节点
	 */
	@Override
	public void add(E e) {
		Node temp = head;
		while (temp.next != null) {
			temp = temp.next;
		}
		temp.next = new Node(e);
	}

	@Override
	public void addByOrder(E e) {

	}

	@Override
	public void delete() {

	}

	@Override
	public void list() {
		Node<E> temp=head.next;
		while (temp != null) {
			System.out.println(temp.e);
			temp = temp.next;
		}
	}

	@Override
	public void update() {

	}

	/**
	 * 英雄节点
	 */
	private static class Node<E> {
		private E e;
		private Node next;

		private Node(E e) {
			this(e, null);
		}

		private Node(E e, Node next) {
			this.e = e;
			this.next = next;
		}
	}

	/**
	 * 英雄实体类
	 */
	@AllArgsConstructor
	@ToString
	private static class Hero{
		private int no;
		private String name;
		private String nickName;
	}

	public static void main(String[] args) {
		SingleLinkedList<Hero> linkedList=new SingleLinkedList<>();
		final Hero hero1 = new Hero(1, "宋江", "及时雨");
		final val hero2 = new Hero(2, "吴用", "智多星");
		final val hero3 = new Hero(3, "林冲", "豹子头");
		linkedList.add(hero1);
		linkedList.add(hero2);
		linkedList.add(hero3);
		linkedList.list();
	}
}
