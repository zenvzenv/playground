package zhengwei.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * channel --register--> selector
 *
 * @author zhengwei AKA Awei
 * @since 2020/1/5 14:43
 */
public class NioServer {
    public static void main(String[] args) throws IOException {
        //创建一个ServerSocketChannel
        final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //创建一个selector对象
        final Selector selector = Selector.open();
        //绑定端口
        serverSocketChannel.socket().bind(new InetSocketAddress(6666));
        //设置为非阻塞模式
        serverSocketChannel.configureBlocking(false);
        //注册
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("selectedKeys---" + selector.selectedKeys().size());
        System.out.println("keys---" + selector.keys().size());
        System.out.println("====================");
        //等待客户端的连接
        while (true) {
            if (selector.select(1_000) == 0) {//没有事件发生
//                System.out.println("no event happen");
                continue;
            }

            /*如果有事件发生
            1. 如果放回大于0，就会获取到相关的selectionKey集合，表示已经获取到关注的事件了
            2. selector.selectedKey()返回关注的事件集合，通过SelectionKey反向获取channel
            3. selectionKey只有我们关心的事件，而keys则是所有注册的事件
            */
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> selectionKeysIter = selectionKeys.iterator();
            while (selectionKeysIter.hasNext()) {
                //获取到SelectionKey
                SelectionKey selectionKey = selectionKeysIter.next();
                if (selectionKey.isAcceptable()) {//发生OP_ACCEPT
                    //为该客户端生成一个SocketChannel
                    //在accept时不会像BIO一样阻塞住，因为一旦进入了isAcceptable中就代表已经可以连接了
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    //设置SocketChannel为非阻塞模式
                    socketChannel.configureBlocking(false);
                    //将当前的SocketChannel注册到Selector上，关注SelectionKey.OP_READ事件，给该SocketChannel关联一个buffer
                    //关联的这个buffer就是服务端接受数据的buffer
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                    System.out.println("one client is connected " + socketChannel.hashCode());
                    System.out.println("selectedKeys---" + selector.selectedKeys().size());
                    System.out.println("keys---" + selector.keys().size());
                    System.out.println("====================");
                }  else if (selectionKey.isReadable()) {//发生OP_READ
                    //通过SelectionKey反向获取Channel
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    //获取该channel关联的buffer
                    ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
                    socketChannel.read(buffer);
                    System.out.println("from client data -> " + new String(buffer.array()));
                    System.out.println("selectedKeys---" + selector.selectedKeys().size());
                    System.out.println("keys---" + selector.keys().size());
                    System.out.println("====================");
                }
                //手动从key set中移除SelectionKey，防止重复操作
                selectionKeysIter.remove();
            }
        }
    }
}
