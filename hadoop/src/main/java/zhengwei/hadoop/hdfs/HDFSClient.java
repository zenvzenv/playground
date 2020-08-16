package zhengwei.hadoop.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 测试hadoop环境，Hadoop客户端
 *
 * @author zhengwei
 */
public class HDFSClient {
    public static FileSystem getFS() throws IOException, InterruptedException, URISyntaxException {
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(new URI("hdfs://slave02:9000"), conf, "zw");
        return fs;
    }

    /**
     * 判断是否是文件或者是文件夹
     *
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void testIsFile() throws IOException, InterruptedException, URISyntaxException {
        FileSystem fs = getFS();
        FileStatus[] listStatus = fs.listStatus(new Path("/"));
        for (FileStatus fileStatus : listStatus) {
            if (fileStatus.isFile()) {
                System.out.println("f:" + fileStatus.getPath().getName());
            } else {
                System.out.println("d:" + fileStatus.getPath().getName());
            }
        }
    }

    /**
     * 文件详情查看
     *
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void testListFiles() throws IOException, InterruptedException, URISyntaxException {
        FileSystem fs = getFS();
        //查看文件列表
        RemoteIterator<LocatedFileStatus> files = fs.listFiles(new Path("/"), true);
        while (files.hasNext()) {
            LocatedFileStatus next = files.next();
            //
            System.out.println(next.getPath().getName());//文件名称
            System.out.println(next.getPermission());//权限
            System.out.println(next.getLen());//文件长度
            BlockLocation[] locations = next.getBlockLocations();//存放的块信息
            for (BlockLocation blockLocation : locations) {
                String[] hosts = blockLocation.getHosts();//主机名
                for (String host : hosts) {
                    System.out.println(host);
                }
            }
            System.out.println("---------------------------------");
        }
        fs.close();
    }

    /**
     * 文件更名
     *
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void testRenameFile() throws IOException, InterruptedException, URISyntaxException {
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(new URI("hdfs://slave02:9000"), conf, "zw");
        fs.rename(new Path("/user/zw/input/wcinput.input"), new Path("/user/zw/input/wc.input"));
        fs.close();
    }

    /**
     * 删除文件
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws URISyntaxException
     */
    @Test
    public void testDeleteFile() throws IOException, InterruptedException, URISyntaxException {
        Configuration conf = new Configuration();
        //1.获取hdfs对象
        FileSystem fs = FileSystem.get(new URI("hdfs://slave02:9000"), conf, "zw");
        //删除文件
        fs.delete(new Path("/user/zwzw"), true);//是否递归删除
        fs.close();
    }

    /**
     * 文件下载
     *
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void testCopyToLocalFile() throws IOException, InterruptedException, URISyntaxException {
        Configuration conf = new Configuration();
        //1.获取hdfs对象
        FileSystem fs = FileSystem.get(new URI("hdfs://slave02:9000"), conf, "zw");
        //下载文件
        fs.copyToLocalFile(new Path("/user/zw/input/wc.input"), new Path("C:\\Users\\zhengwei\\Desktop\\"));
        //3.关闭资源
        fs.close();
    }

    /**
     * 文件上传
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws URISyntaxException
     */
    @Test
    public void testCopyFromLocalFile() throws IOException, InterruptedException, URISyntaxException {
        Configuration conf = new Configuration();
        conf.set("", "");
        //1.获取hdfs对象
        FileSystem fs = FileSystem.get(new URI("hdfs://slave02:9000"), conf, "zw");
        //2.使用API进行文件上传
        fs.copyFromLocalFile(new Path("C:\\Users\\zhengwei\\Desktop\\sss.txt")/*源路径*/, new Path("/user/zwzw/")/*目的路径*/);
        //3.关闭资源
        fs.close();
        System.out.println("over");
    }

    public static void main(String[] args) throws Exception {
        //配置信息
        Configuration conf = new Configuration();
//		conf.set("fs.defaultFS", "hdfs://slave02:9000");
        //1.获取HDFS对象
//		FileSystem fs = FileSystem.get(conf);
        FileSystem fs = FileSystem.get(new URI("hdfs://slave02:9000"), conf, "zw");
        //2.在hdfs上创建路径
        fs.mkdirs(new Path("/user/zwzw/"));
        //3.关闭资源
        fs.close();
        System.out.println("over");
    }
}
