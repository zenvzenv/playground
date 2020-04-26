package zhengwei.jvm.memory;

/**
 * jmap
 * jstat
 * jcmd pid VM.flags 查看当前VM的启动参数
 * jcmd pid help :列出针对此VM所有可用选项
 * 
 *
 * @author zhengwei AKA Awei
 * @since 2020/1/18 10:47
 */
public class JmapJstack {
    public static void main(String[] args) throws InterruptedException {
        Thread.currentThread().join();
    }
}
