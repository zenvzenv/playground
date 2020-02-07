package zhengwei.jvm.memory;

/**
 * jcmd综合命令:
 * 1. jcmd pid VM.flag:查看JVM的启动参数
 * 2. jcmd pid help:列出当前JVM可以执行操作
 * 3. jcmd pid help JFR.dump:查看具体命令的使用方法
 * 4. jcmd pid PerfCounter.print:查看JVM性能相关指标
 * 5. jcmd pid VM.uptime:查看JVM启动时间
 * 6. jcmd pid GC.class_histogram:查看系统中类的统计信息
 * 7. jcmd pid Thread.print:查看thread栈信息
 * 8. jcmd pid GC.heap_dump file_path:转储JVM信息
 * 9. jcmd pid VM.system_properties:查看JVM的属性信息
 * 10. jcmd pid VM.version:查看JVM的版本信息和jdk版本信息
 * 11. jcmd pid VM.command_line:查看JVM启动的命令参数信息
 *
 * @author zhengwei AKA Awei
 * @since 2020/1/22 14:31
 */
public class Jcmd {
    public static void main(String[] args) throws InterruptedException {
        Thread.currentThread().join();
    }
}
