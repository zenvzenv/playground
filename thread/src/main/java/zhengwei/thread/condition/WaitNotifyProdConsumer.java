package zhengwei.thread.condition;

/**
 * @author zhengwei AKA Awei
 * @since 2019/11/24 10:07
 */
public class WaitNotifyProdConsumer {
    private static int data = 0;
    private static boolean use = false;
    private static final Object object = new Object();

    public static void main(String[] args) {

    }

    private static void buildData() {
        synchronized (object) {
            while (use) {
                try {
                    object.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("p -> " + data++);
            use = true;
            object.notifyAll();
        }
    }

    private static void useData() {
        synchronized (object) {
            while (!use) {
                try {
                    object.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("c -> " + data);
            use = false;
            object.notifyAll();
        }
    }
}
