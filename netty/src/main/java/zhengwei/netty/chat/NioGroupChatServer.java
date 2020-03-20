package zhengwei.netty.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

/**
 * @author zhengwei AKA Awei
 * @since 2020/1/11 16:42
 */
public class NioGroupChatServer {
    private static final Map<String, Channel> CLIENT_INFO = new HashMap<>();
    private static Selector selector;

    static {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(8888));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            //此次感兴趣SelectionKey
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isAcceptable()) {
                    //这里我们明确的知道OP_ACCEPT的Channel时ServerSocketChannel是因为在前面代码已经指明了ServersSocketChannel
                    ServerSocketChannel serverSocket = (ServerSocketChannel) selectionKey.channel();
                    SocketChannel socketChannel = serverSocket.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    CLIENT_INFO.put(UUID.randomUUID().toString(), socketChannel);
                    String info = "[server] - " + socketChannel.getRemoteAddress() + " is online";
                    forwardMsg(info, socketChannel);
                } else if (selectionKey.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(512);
                    while (true) {
                        buffer.clear();
                        int read = socketChannel.read(buffer);
                        if (read < 0) {
                            break;
                        }
                        buffer.flip();
                        forwardMsg(new String(buffer.array()), socketChannel);
                    }
                }
                iterator.remove();
            }
        }
    }

    /**
     * 分发消息到别的客户端
     */
    static void forwardMsg(String message, SocketChannel socketChannel) {
        selector.keys().forEach(key -> {
            SelectableChannel targetChannel = key.channel();
            if (targetChannel instanceof SocketChannel && targetChannel != socketChannel) {
                try {
                    ((SocketChannel) targetChannel).write(ByteBuffer.wrap(message.getBytes()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
