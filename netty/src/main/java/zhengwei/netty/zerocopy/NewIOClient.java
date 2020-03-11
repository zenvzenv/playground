package zhengwei.netty.zerocopy;

import lombok.SneakyThrows;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * @author zhengwei AKA Awei
 * @since 2020/3/11 8:44
 */
public class NewIOClient {
    @SneakyThrows
    public static void main(String[] args) {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 8899));
        //是否配置成阻塞取决于是否使用了selector，配置为非阻塞可以保证selector的不阻塞
        socketChannel.configureBlocking(true);
        String file = args[0];
        final FileChannel channel = new FileInputStream(file).getChannel();
        final long start = System.currentTimeMillis();
        final long transfer = channel.transferTo(0, channel.size(), socketChannel);
        System.out.println("total size=" + transfer + ", total time=" + (System.currentTimeMillis() - start));
        channel.close();
    }
}
