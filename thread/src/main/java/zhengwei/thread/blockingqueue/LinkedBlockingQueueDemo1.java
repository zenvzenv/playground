package zhengwei.thread.blockingqueue;

import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * LinkedBlockingQueue可以有界也可以无界，主要看使用者如何构造LinkedBlockingQueue
 *
 * @author zhengwei AKA Awei
 * @since 2020/1/4 14:23
 */
public class LinkedBlockingQueueDemo1 {
    /**
     * {@link LinkedBlockingQueue#add(Object)}实际调用offer，当offer返回false的时候(即插入失败的时候)，抛出java.lang.IllegalStateException: Queue full异常
     * {@link LinkedBlockingQueue#put(Object)}当队列满的时候，再往里面put数据会阻塞住，直到队列中的数据被消费之后，才能再往队列中插入数据
     * {@link LinkedBlockingQueue#offer(Object)}当队列满时，再插入数据时会返回false，否则返回true
     */
    @Test
    void addElement() throws InterruptedException {
        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(3);
        /*queue.add("zhengwei1");
        queue.add("zhengwei2");
        queue.add("zhengwei3");
        queue.add("zhengwei4");*/
        /*queue.put("zhengwei1");
        queue.put("zhengwei2");
        queue.put("zhengwei3");
        queue.put("zhengwei4");*/
        queue.offer("zhengwei1");
        queue.offer("zhengwei2");
        queue.offer("zhengwei3");
        queue.offer("zhengwei4");
    }

    /**
     * {@link LinkedBlockingQueue#take()}取出队首元素，当队列无数据时会阻塞住，直到取到数据
     * {@link LinkedBlockingQueue#poll()}取出队首元素，不会阻塞，会将元素从队列中移除，如果队列中无数据，那么返回null
     * {@link LinkedBlockingQueue#peek()}取出队首元素，不会阻塞，不会将元素从队列中移除，如果队列中无数据，那么返回null
     */
    @Test
    void getElement() throws InterruptedException {
        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(2);
//        queue.take();
        System.out.println(queue.poll());
        System.out.println(queue.peek());
    }
}
