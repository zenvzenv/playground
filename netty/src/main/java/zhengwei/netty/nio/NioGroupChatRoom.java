package zhengwei.netty.nio;

import lombok.Data;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * @author zhengwei AKA Awei
 * @since 2020/1/5 17:08
 */
public class NioGroupChatRoom {
    @Data
    private static class Server {
        //属性
        private Selector selector;
        private ServerSocketChannel listenChannel;
        private static final int port = 6667;

        @SneakyThrows
        public Server() {
            //获取Selector
            this.selector = Selector.open();
            //获取ServerSocketChannel
            this.listenChannel = ServerSocketChannel.open();
            //注册端口号
            this.listenChannel.socket().bind(new InetSocketAddress(port));
            //非阻塞模式
            this.listenChannel.configureBlocking(false);
            //将Channel注册到Selector上，感兴趣的类型为OP_ACCEPT
            this.listenChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        }

        @SneakyThrows
        void listen() {
            while (true) {
                int select = this.selector.select();
                if (select > 0) {//有事件处理
                    //遍历SelectionKey集合
                    Iterator<SelectionKey> selectionKeyIterator = this.selector.selectedKeys().iterator();
                    this.selector.keys();
                    while (selectionKeyIterator.hasNext()) {
                        //取出SelectionKey
                        SelectionKey selectionKey = selectionKeyIterator.next();
                        //监听到了accept
                        if (selectionKey.isAcceptable()) {//接受连接
                            SocketChannel socketChannel = this.listenChannel.accept();
                            //非阻塞模式
                            socketChannel.configureBlocking(false);
                            //将SocketChannel注册到Selector上
                            socketChannel.register(this.selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                            System.out.println(socketChannel.getRemoteAddress() + " is online");
                        } else if (selectionKey.isReadable()) {//通道时可读的状态
                            //读取客户端信息
                            this.readDataFromClient(selectionKey);
                        }
                        //把当前的SelectionKey删除，防止重读处理
                        selectionKeyIterator.remove();
                    }
                } else {
//                    System.out.println("waiting...");
                }
            }
        }

        //读取客户端消息
        void readDataFromClient(SelectionKey selectionKey) {
            SocketChannel socketChannel = null;
            try {
                socketChannel = (SocketChannel) selectionKey.channel();
                final ByteBuffer buffer = ByteBuffer.allocate(1024);
                int read = socketChannel.read(buffer);
                if (read > 0) {
                    //将缓冲区的数据转成字符串
                    String msg = new String(buffer.array());
                    System.out.println("from client -> " + msg);
                    //向其他客户端转发消息
                    this.forwardMsg(msg, socketChannel);
                }
            } catch (IOException e) {
//                e.printStackTrace();
                try {
                    System.out.println(socketChannel.getRemoteAddress() + " outline");
                    //取消注册
                    selectionKey.cancel();
                    //关闭通道
                    socketChannel.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        //转发消息给其他客户端(channel)
        @SneakyThrows
        void forwardMsg(String msg, SocketChannel selfSocketChannel) {
            System.out.println("server forward message");
            //遍历所有注册到Selector上的SocketChannel，并排除自己
            for (SelectionKey key : this.selector.keys()) {
                //取出key取出对应的channel
                Channel targetChannel = key.channel();
                //排除自己
                if (targetChannel instanceof SocketChannel && targetChannel != selfSocketChannel) {
                    ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                    ((SocketChannel) targetChannel).write(buffer);
                }
            }
        }

        public static void main(String[] args) {
            Server server = new Server();
            server.listen();
        }
    }

    private static class Client {
        private static final String host = "localhost";
        private static final int port = 6667;
        private Selector selector;
        private SocketChannel socketChannel;
        private String name;

        @SneakyThrows
        public Client() {
            this.selector = Selector.open();
            this.socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
            this.socketChannel.configureBlocking(false);
            this.socketChannel.register(this.selector, SelectionKey.OP_READ);
            this.name = socketChannel.getLocalAddress().toString();
            System.out.println(name + " client is ready");
        }

        @SneakyThrows
        void sendMsg(String message) {
            message = "[ " + this.name + " ] say : " + message;
            this.socketChannel.write(ByteBuffer.wrap(message.getBytes()));
        }

        @SneakyThrows
        void readMsg() {
            int select = this.selector.select();
            if (select > 0) {
                Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey selectionKey = selectionKeyIterator.next();
                    if (selectionKey.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        socketChannel.read(buffer);
                        System.out.println(new String(buffer.array()));
                    } else {
                        System.out.println("no channel can use");
                    }
                    selectionKeyIterator.remove();
                }
            }
        }

        public static void main(String[] args) {
            //启动客户端
            Client client = new Client();
            //启动一个线程，每隔3秒，读取从服务器发送数据
            new Thread(() -> {
                while (true) {
                    try {
                        client.readMsg();
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String message = scanner.nextLine();
                client.sendMsg(message);
            }
        }
    }
}
