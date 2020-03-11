package zhengwei.netty.zerocopy;

import lombok.SneakyThrows;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author zhengwei AKA Awei
 * @since 2020/3/8 16:57
 */
public class NewIOServer {
    @SneakyThrows
    public static void main(String[] args) {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(8899);
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        final ServerSocket socket = serverSocketChannel.socket();
        //当socket连接失效时，端口可以被其他socket程序继续绑定
        socket.setReuseAddress(true);
        socket.bind(inetSocketAddress);
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        while (true) {
            final SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(true);
            int readCount = 0;
            while (-1 != readCount) {
                readCount = socketChannel.read(buffer);
                //重置buffer
                buffer.clear();
            }
        }
    }
}
