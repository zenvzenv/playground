package zhengwei.thread.blockingqueue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zhengwei AKA Awei
 * @since 2020/1/2 19:47
 */
public class ArrayBlockingQueueDemo1 {
    private static final ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(5);

    public static void main(String[] args) {

    }

    /**
     * {@link ArrayBlockingQueue#add(Object o)}添加一个元素，
     * 当队列满的时候，再往队列中添加元素时会抛出java.lang.IllegalStateException: Queue full异常。
     */
    @Test
    void addException() {
        queue.add("zhengwei1");
        queue.add("zhengwei2");
        queue.add("zhengwei3");
        queue.add("zhengwei4");
        queue.add("zhengwei5");
//        queue.add("zhengwei6");
        System.out.println(queue.size());
    }

    /**
     * {@link ArrayBlockingQueue#offer(Object o)}的功能与{@link ArrayBlockingQueue#add(Object o)}一样，
     * 往队列中插入数据，但是不会报错，即使队列满了，只是会返回false
     */
    @Test
    void offerObjTpQueue() {
        System.out.println(queue.offer("zhengwei1"));
        System.out.println(queue.offer("zhengwei2"));
        System.out.println(queue.offer("zhengwei3"));
        System.out.println(queue.offer("zhengwei4"));
        System.out.println(queue.offer("zhengwei5"));
        System.out.println(queue.offer("zhengwei6"));
    }

    /**
     * {@link ArrayBlockingQueue#put(Object o)}往队列中放入数据，如果队列满了则阻塞住
     */
    @Test
    void putObiToQueue() throws InterruptedException {
        final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.schedule(() -> {
            try {
                System.out.println(queue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 1, TimeUnit.SECONDS);
        queue.put("zhengwei1");
        queue.put("zhengwei2");
        queue.put("zhengwei3");
        queue.put("zhengwei4");
        queue.put("zhengwei5");
        queue.put("zhengwei6");
        System.out.println(queue.size());
//        Thread.currentThread().join();
        service.shutdown();
    }

    /**
     * {@link ArrayBlockingQueue#poll()}从队首弹出一个值，如果没有值则弹出null
     */
    @Test
    void pollFromQueue() {
        queue.add("zhengwei1");
        queue.add("zhengwei2");
        System.out.println(queue.poll());
        System.out.println(queue.poll());
        System.out.println(queue.poll());
    }

    /**
     * {@link ArrayBlockingQueue#remove()}作用和poll一样，底层调用poll，但是如果队首为空的话将会报错
     */
    @Test
    void removeFromQueue() {
        queue.add("zhengwei1");
        queue.add("zhengwei2");
        queue.add("zhengwei3");
        queue.remove();
        queue.remove();
        queue.remove();
        queue.remove();
    }

    /**
     * {@link ArrayBlockingQueue#peek()}每次只会取出队首的值，不会像poll一样去弹出值，不会抛出异常
     */
    @Test
    void peekFromQueue() {
        queue.add("zhengwei1");
        queue.add("zhengwei2");
        queue.add("zhengwei3");
        System.out.println(queue.peek());
        System.out.println(queue.peek());
        System.out.println(queue.peek());
    }

    /**
     * {@link ArrayBlockingQueue#element()}作用和peek一样，底层调用peek，如果队首为null的话，抛出异常
     */
    @Test
    void elementFromQueue() {
        queue.add("zhengwei1");
        queue.add("zhengwei2");
        queue.add("zhengwei3");
        System.out.println(queue.element());
        System.out.println(queue.element());
        System.out.println(queue.element());
        System.out.println(queue.element());
        queue.clear();
        System.out.println(queue.element());
    }

    /**
     * {@link ArrayBlockingQueue#size()}队列中的元素个数
     * {@link ArrayBlockingQueue#remainingCapacity()}队列剩余的空位置
     * {@link ArrayBlockingQueue#drainTo(Collection c)}将队列中的所有元素排到一个集合中
     */
    @Test
    void drainTo() {
        queue.add("zhengwei1");
        queue.add("zhengwei2");
        queue.add("zhengwei3");
        //队列中有多少元素
        System.out.println(queue.size());
        //队列中还剩多少个空位
        System.out.println(queue.remainingCapacity());
        System.out.println(queue.poll());
        System.out.println(queue.remainingCapacity());
        List<String> list = new ArrayList<>();
        //排干队列中的所有值到一个集合中
        queue.drainTo(list);
        System.out.println(list);
    }

    @Test
    void test() {
        List<String> list = new ArrayList<>();
        list.add("11");
        list.add("11");
        list.add("11");
        for (int i = 0; i < list.size(); i++) {
            list.remove(i);
        }
        System.out.println(list.size());
        System.out.println(list);
    }
}
