package zhengwei.thread.countdownlatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * table->event
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/13 20:15
 */
public class CountDownLatchDemo3 {
    private static final Random random = new Random(System.currentTimeMillis());
    private static final ExecutorService service = Executors.newFixedThreadPool(5);

    private static class Event {
        private int id;

        public Event(int id) {
            this.id = id;
        }
    }

    private static class Table {
        private String tableName;
        private long sourceRecordCount;
        private long targetCount;
        private String sourceColumnSchema = "<table name='a'><column name='col1' type='varchar2'/></table>";
        private String targetColumnSchema = "";

        public Table(String tableName, long sourceRecordCount) {
            this.tableName = tableName;
            this.sourceRecordCount = sourceRecordCount;
        }

        @Override
        public String toString() {
            return "Table{" +
                    "tableName='" + tableName + '\'' +
                    ", sourceRecordCount=" + sourceRecordCount +
                    ", targetCount=" + targetCount +
                    ", sourceColumnSchema='" + sourceColumnSchema + '\'' +
                    ", targetColumnSchema='" + targetColumnSchema + '\'' +
                    '}';
        }
    }

    /**
     * 完全信任source的计数线程
     */
    private static class TrustSourceRecordCounter implements Runnable {
        private final Table table;
        private final TaskBatch taskBatch;

        public TrustSourceRecordCounter(Table table, TaskBatch taskBatch) {
            this.table = table;
            this.taskBatch = taskBatch;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(random.nextInt(10_000));
                table.targetCount = table.sourceRecordCount;
//                System.out.println("The table - " + table.tableName + " target record count calc down and update data");
                taskBatch.done(table);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class TrustSourceColumns implements Runnable {
        private final Table table;
        private final TaskBatch taskBatch;

        public TrustSourceColumns(Table table, TaskBatch taskBatch) {
            this.table = table;
            this.taskBatch = taskBatch;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(random.nextInt(10_000));
                table.targetColumnSchema = table.sourceColumnSchema;
//                System.out.println("The table - " + table.tableName + " target columns calc down and update data");
                taskBatch.done(table);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    interface Watcher {
//        void startWatch();

        void done(Table table);
    }

    private static class TaskBatch implements Watcher {
        private static CountDownLatch latch;
        private final TaskGroup taskGroup;

        /**
         * 初始化门闩大小
         *
         * @param size      门闩大小
         * @param taskGroup
         */
        public TaskBatch(int size, TaskGroup taskGroup) {
            latch = new CountDownLatch(size);
            this.taskGroup = taskGroup;
        }

        @Override
        public void done(Table table) {
            latch.countDown();
            if (latch.getCount() == 0) {
                System.out.println("The table " + table.tableName + " finish work,the table is [" + table.toString() + "]");
                taskGroup.done(table);
            }
        }
    }

    private static class TaskGroup implements Watcher {
        private static CountDownLatch latch;
        private final Event event;

        /**
         * 初始化门闩大小
         *
         * @param size  门闩大小
         * @param event
         */
        public TaskGroup(int size, Event event) {
            latch = new CountDownLatch(size);
            this.event = event;
        }

        @Override
        public void done(Table table) {
            latch.countDown();
            if (latch.getCount() == 0) {
                System.out.println("All of table done in event : " + event.id);
            }
        }
    }

    private static List<Table> calc(Event event) {
        List<Table> tables = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tables.add(new Table("table-" + event.id + "-" + i, i * 1000));
        }
        return tables;
    }

    public static void main(String[] args) {
        Event[] events = {new Event(1), new Event(2)};
        for (Event event : events) {
            List<Table> tables = calc(event);
            TaskGroup taskGroup = new TaskGroup(tables.size(), event);
            tables.forEach(t -> {
                TaskBatch taskBatch = new TaskBatch(2, taskGroup);
                service.submit(new TrustSourceColumns(t, taskBatch));
                service.submit(new TrustSourceRecordCounter(t, taskBatch));
            });
        }
    }
}
