package zhengwei.netty.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @author zhengwei AKA Awei
 * @since 2020/1/11 17:04
 */
public class NioGroupChatClient {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(8888));

    }
}
