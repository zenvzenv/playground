package zhengwei.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

/**
 * scattering:一个数据可以被分散的写入多个ByteBuffer中
 * gathering:多个ByteBuffer可以依次读出
 *
 * @author zhengwei AKA Awei
 * @since 2019/12/1 11:14
 */
public class ScatteringGatheringBuffer {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress address = new InetSocketAddress(8899);
        serverSocketChannel.socket().bind(address);
        int messageLength = 2 + 3 + 4;
        ByteBuffer[] buffers = new ByteBuffer[3];
        buffers[0] = ByteBuffer.allocate(2);
        buffers[1] = ByteBuffer.allocate(3);
        buffers[2] = ByteBuffer.allocate(4);
        SocketChannel socketChannel = serverSocketChannel.accept();
        while (true) {
            int byteRead = 0;
            while (byteRead < messageLength) {
                long r = socketChannel.read(buffers);
                byteRead += r;
                System.out.println("byteRead -> " + byteRead);
                Arrays.stream(buffers)
                        .map(buffer -> "position->" + buffer.position() + ",limit -> " + buffer.limit())
                        .forEach(System.out::println);
                Arrays.asList(buffers).forEach(ByteBuffer::flip);
            }
            int byteWrite = 0;
            while (byteWrite < messageLength) {
                long write = socketChannel.write(buffers);
                byteWrite += write;
            }
            Arrays.asList(buffers).forEach(ByteBuffer::reset);
            System.out.println("byteRead -> " + byteRead + ", byteWrite -> " + byteWrite + ", messageLength -> " + messageLength);
        }
    }
}
