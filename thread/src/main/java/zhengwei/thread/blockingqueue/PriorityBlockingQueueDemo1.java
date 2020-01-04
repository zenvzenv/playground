package zhengwei.thread.blockingqueue;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author zhengwei AKA Awei
 * @since 2020/1/3 13:22
 */
public class PriorityBlockingQueueDemo1 {
    /**
     * 虽然指定了容量，但是其内部有扩容机制，队列的最大长度为{@code Integer@MAX_VALUE-8}
     */
    private static final PriorityBlockingQueue<String> queue = new PriorityBlockingQueue<>(5);

    /**
     * {@link PriorityBlockingQueue#add(Object)}
     * {@link PriorityBlockingQueue#put(Object)}
     * {@link PriorityBlockingQueue#offer(Object)}
     * 这三个方法实际上是同一种方法，最终调用的都是offer，
     * 不允许插入null，会报空指针异常
     * PriorityBlockingQueue有扩容机制，当当前容量不足时进行扩容，
     * 扩容机制为：旧的的容量是否小于64，如果小于64的话，就进行快速扩容，对在旧容量的基础上加2，如果大于64的话，就扩到原来容量的1.5倍，
     * 如果扩容之后的容量大小大于Integer.MAX_VALUE-8的话，那么就把容量设置为MAX_ARRAY_SIZE
     */
    @Test
    void addElement(){
        System.out.println(queue.add("zhengwei1"));
        queue.put("zhengwei2");
        System.out.println(queue.offer("zhengwei3"));
        System.out.println(queue.add("zhengwei4"));
        System.out.println(queue.add("zhengwei5"));
        System.out.println(queue.add("zhengwei6"));
    }

    /**
     * {@link PriorityBlockingQueue#poll()}当队列中有值时，直接返回结果，当队列中没有值的时候，不会阻塞，直接返回null
     * {@link PriorityBlockingQueue#take()}当队列中没有值的时候，会阻塞，直到取到值再返回
     */
    @Test
    void getElement() throws InterruptedException {
        queue.offer("zhengwei1");
        queue.offer("zhengwei2");
        queue.offer("zhengwei3");
        queue.offer("zhengwei4");
        System.out.println(queue.poll());
        System.out.println(queue.poll());
        System.out.println(queue.poll());
        System.out.println(queue.poll());
        System.out.println(queue.poll());
//        System.out.println(queue.take());
    }

    /**
     * 凡是添加到PriorityBlockingQueue的对象要么实现了Comparable接口，要么再构造PriorityBlockingQueue时指定Comparator
     */
    @Test
    void addNotComparableObject(){
        PriorityBlockingQueue<UserNoComparable> queue = new PriorityBlockingQueue<>();
        queue.offer(new UserNoComparable());
        System.out.println("fail");
        fail("should not process to here");
    }

    @Test
    void addObjectWithComparator(){
        PriorityBlockingQueue<UserNoComparable> queueWithComparator = new PriorityBlockingQueue<>(5, Comparator.comparingInt(Object::hashCode));
        queueWithComparator.offer(new UserNoComparable());
    }

    private static class UserNoComparable{

    }
}
