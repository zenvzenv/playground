package zhengwei.thread.collections;

import java.util.Objects;

/**
 * @author zhengwei AKA Awei
 * @since 2020/1/1 12:40
 */
public class MyLinkedList<E> {
    private final Node<E> NULL = null;
    private static final String PLANT_NULL = "null";
    private Node<E> first;
    private int size;

    MyLinkedList() {
        first = null;
    }

    @SafeVarargs
    MyLinkedList(E... elements) {
        if (0 == elements.length) {
            new MyLinkedList<E>();
        } else {
            for (E element : elements) {
                this.addFirst(element);
            }
        }
    }

    boolean contains(E value) {
        Node<E> current = first;
        while (!Objects.isNull(current)) {
            if (current.value.equals(value)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    E removeFirst() {
        if (isEmpty()) {
            throw new NullPointerException("the link list is empty!");
        }
        Node<E> current = first;
        first = current.next;
        size--;
        return current.value;
    }

    void addFirst(E value) {
        Node<E> newNode = new Node<>(value);
        newNode.next = first;
        first = newNode;
        size++;
    }

    int size() {
        return size;
    }

    boolean isEmpty() {
        return size == 0;
    }

    Node<E> getFirst() {
        return first;
    }

    private static class Node<E> {
        private E value;
        private Node<E> next;

        Node(E value) {
            this.value = value;
        }

        @Override
        public String toString() {
            if (Objects.isNull(value)) {
                return PLANT_NULL;
            }
            return "Node{" + value + "}";
        }
    }

    public static void main(String[] args) {
        MyLinkedList<String> list = new MyLinkedList<>("hello", "world", "java", "spark", "hadoop");
        System.out.println(list.size());
        System.out.println(list.getFirst().value);
        System.out.println(list.removeFirst());
        System.out.println(list.size());
        System.out.println(list.contains("spark"));
    }
}
