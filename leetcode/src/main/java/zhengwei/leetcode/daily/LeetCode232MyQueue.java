package zhengwei.leetcode.daily;

import java.util.Stack;

/**
 * 232. 用栈实现队列
 *
 * @author zhengwei AKA zenv
 * @since 2021/3/5 14:38
 */
public class LeetCode232MyQueue {
    private final Stack<Integer> in;
    private final Stack<Integer> out;

    /**
     * Initialize your data structure here.
     */
    public LeetCode232MyQueue() {
        in = new Stack<>();
        out = new Stack<>();
    }

    /**
     * Push element x to the back of queue.
     */
    public void push(int x) {
        in.push(x);
    }

    /**
     * Removes the element from in front of queue and returns that element.
     */
    public int pop() {
        if (out.isEmpty()) {
            while (!in.isEmpty()) {
                out.push(in.pop());
            }
        }
        return out.pop();
    }

    /**
     * Get the front element.
     */
    public int peek() {
        if (out.isEmpty()) {
            while (!in.isEmpty()) {
                out.push(in.pop());
            }
        }
        return out.peek();
    }

    /**
     * Returns whether the queue is empty.
     */
    public boolean empty() {
        return in.isEmpty() && out.isEmpty();
    }
}
