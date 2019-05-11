package zhengwei.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Zookeeper的一些API学习
 * @author zhengwei AKA Sherlock
 * @since 2019/5/11 17:25
 */
final class TestZookeeper {
    //zookeeper默认的客户端端口时2181，默认的Leader和Follower通信端口时2888，默认的Leader选举端口时3888
    private static final String connectString="hadoopha01:2181,hadoopha02:2181,hadoopha03:2181";
    //超时时间，单位：毫秒
    private static final int sessionTimeout=2000;
    private ZooKeeper zkClient;

    /**
     * 初始化zookeeper客户端
     * @throws IOException 异常
     */
    @BeforeAll
    void init() throws IOException {
        zkClient =new ZooKeeper(connectString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                //收到事件通知后的回调函数(用户的业务逻辑在这写)
                System.out.println(event.getType()+"--"+event.getPath());
                try {
                    List<String> children = zkClient.getChildren("/", true);
                    children.forEach(System.out::println);
                    //再次启动监听，因为每次注册一次监听只会起作用一次，需要再次启动
                    zkClient.getChildren("/",true);
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 创建znode
     */
    @Test
    void createNode() throws KeeperException, InterruptedException {
        //需要特别注意第四个参数，创建znode有两种模式
        //第一种是普通的和普通的带序号的，普通的znode在客户端断开连接后，znode不会删除，带序号的znode在每次创建时，序号都会加一
        //一种是短暂的和短暂的带序号的，短暂的znode在节点断开连接后znode也会跟着删除，其余同上
        String path = zkClient.create("/zhengwei", "zhengweizuishuai".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println(path);
    }

    /**
     * 获取数据并监听目录变化
     */
    @Test
    void getDataAndWatch() throws KeeperException, InterruptedException {
        //获取路径下的所有子节点
        List<String> children = zkClient.getChildren("/", true);
        children.forEach(System.out::println);
        Thread.sleep(Integer.MAX_VALUE);
    }

    /**
     * 判断节点是否存在
     */
    @Test
    void exist() throws KeeperException, InterruptedException {
        Stat exists = zkClient.exists("/", false);
        System.out.println(Objects.isNull(exists)?"not exist":"exist");
    }
}
