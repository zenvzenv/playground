package zhengwei.thread.phaser;

import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

/**
 * Phaser是循环的，是一轮一轮的
 * 当这轮工作结束之后，将会开启下一轮工作，内部有个计数器来维护所在的轮数
 * 默认是0.最大时Integer.MAX_VALUE
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/26 19:15
 */
public class PhaserDemo4 {
    public static void main(String[] args) throws InterruptedException {
        final Phaser phaser = new Phaser(1);
        //查看阶段(轮数)，每当所有线程到达arriveAndAwaitAdvance之后，阶段数加一
        System.out.println(phaser.getPhase());
        phaser.arriveAndAwaitAdvance();
        System.out.println(phaser.getPhase());
        phaser.arriveAndAwaitAdvance();
        System.out.println(phaser.getPhase());
        phaser.arriveAndAwaitAdvance();
        System.out.println(phaser.getPhase());
        System.out.println("-------------------------------");
        //查看注册了多少个party
        //自带一个party
        System.out.println(phaser.getRegisteredParties());
        phaser.register();
        System.out.println(phaser.getRegisteredParties());
        phaser.register();
        System.out.println(phaser.getRegisteredParties());
        System.out.println("-------------------------------");
        //获取已经到达的party和没有到达的party
        System.out.println(phaser.getArrivedParties());
        System.out.println(phaser.getUnarrivedParties());
        phaser.arriveAndAwaitAdvance();
        System.out.println(phaser.getArrivedParties());
        System.out.println(phaser.getUnarrivedParties());
        System.out.println("-------------------------------");
        //批量注册party
        phaser.bulkRegister(10);
        System.out.println(phaser.getRegisteredParties());
        System.out.println(phaser.getArrivedParties());
        System.out.println(phaser.getUnarrivedParties());
        new Thread(phaser::arriveAndAwaitAdvance).start();
        System.out.println("***********");
        System.out.println(phaser.getRegisteredParties());
        System.out.println(phaser.getArrivedParties());
        System.out.println(phaser.getUnarrivedParties());
        final Phaser phaser1 = new Phaser(2) {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                return true;
            }
        };
        new OnAdvanceTask("zw1", phaser1).start();
        new OnAdvanceTask("zw2", phaser1).start();
        TimeUnit.SECONDS.sleep(2);
        System.out.println(phaser1.getArrivedParties());
        System.out.println(phaser1.getUnarrivedParties());
    }

    private static class OnAdvanceTask extends Thread {
        private final Phaser phaser;

        OnAdvanceTask(String name, Phaser phaser) {
            super(name);
            this.phaser = phaser;
        }

        @Override
        public void run() {
            System.out.println(getName() + " - i am end..." + phaser.getPhase());
            phaser.arriveAndAwaitAdvance();
            System.out.println(getName() + " - i am end");
            System.out.println("isTerminated->" + phaser.isTerminated());
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //程序不会退出，因为zw1做如下代码，而zw2没有做，导致第二阶段没有结束，所有程序不会退出
            //需要所有线程到达同一阶段之后，共同退出
            if (getName().equals("zw1")) {
                System.out.println(getName() + " - i am end..." + phaser.getPhase());
                phaser.arriveAndAwaitAdvance();
                System.out.println(getName() + " - i am end");
            }
        }
    }
}
