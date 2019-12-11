package zhengwei.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * @author zhengwei AKA Awei
 * @since 2019/12/11 10:50
 */
public class EchoServer {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress("localhost", 8888));
        serverSocketChannel.configureBlocking(false);
        //将accept时间绑定到selector上
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            //阻塞在select上
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            //遍历selectKeys
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isAcceptable()) {
                    //如果是accept事件
                    ServerSocketChannel ssc = (ServerSocketChannel) selectionKey.channel();
                    SocketChannel socketChannel = ssc.accept();
                    System.out.println("accept a new conn : " + socketChannel.getRemoteAddress());
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (selectionKey.isReadable()) {
                    //如果是read事件
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int length = socketChannel.read(buffer);
                    if (length > 0) {
                        buffer.flip();
                        byte[] bytes = new byte[buffer.remaining()];
                        //将数据读取到byte数组中
                        buffer.get(bytes);
                        String content = new String(bytes, StandardCharsets.UTF_8).replace("\r\n", "");
                        if (content.equalsIgnoreCase("quit")) {
                            selectionKey.cancel();
                            socketChannel.close();
                        } else {
                            System.out.println("reserve msg : " + content);
                        }
                    }
                }
                iterator.remove();
            }
        }
    }
}
