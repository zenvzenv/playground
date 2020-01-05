package zhengwei.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author zhengwei AKA Awei
 * @since 2020/1/5 15:09
 */
public class NioClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        //得到一个网络通道，并提供提供服务器的host,port
        InetSocketAddress inetSocketAddress = new InetSocketAddress(6666);
        SocketChannel socketChannel = SocketChannel.open();
        //设置非阻塞模式
        socketChannel.configureBlocking(false);
        //连接服务器，不会阻塞
        if (!socketChannel.connect(inetSocketAddress)) {
            while (!socketChannel.finishConnect()) {
                System.out.println("Because connection need some time,I can do other things");
            }
        }
        //连接成功，发送数据
        String msg = "zhengwei";
        //包装一个ByteBuffer
        ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
        //发送数据，实际就是将buffer中的数据写入到channel中
        socketChannel.write(buffer);
        Thread.currentThread().join();
    }
}
