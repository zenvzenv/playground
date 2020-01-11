package zhengwei.netty.simple;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

/**
 * 1. channel和pipeline是相互包含的关系，可以通过channel获取到pipeline，也可以通过pipeline获取channel
 * 2. bossGroup和workerGroup中默认的NioEventLoop个数为CPU核数*2(即8核有16线程)，也可以自己手动设置NioEventLoop的个数
 * 3. pipeline底层是一个双向链表
 * <p>
 * NioEventLoopGroup包含多个NioEventLoop,NioEventLoop说白了就是一个监听事件的线程。
 * NioEventLoop中包含一个Selector、taskQueue、scheduleTaskQueue
 * 每个NioEventLoop中的Selector中监听多个NioChannel(Channel注册到Selector上)
 * 每个NioChannel绑定在一个NioEventLoop(线程)上
 * 每个NioEventLoop绑定一个ChannelPipeline，两者可以互相获取
 *
 * @author zhengwei AKA Awei
 * @since 2020/1/8 20:12
 */
public class NettyTcpDemo {
    private static final class NettyTcpServer {
        @SneakyThrows
        public static void main(String[] args) {
            /*
            1. 创建BossGroup和WorkerGroup线程组
            2. bossGroup只处理连接请求，真正处理客户端的业务请求的时workerGroup
            3. 两个都是无限循环
            4. bossGroup和workerGroup的实际类型是NioEventLoopGroup，在其中的监听线程是NioEventLoop，而NioEventLoop的个数默认=实际CPU的核心数 * 2(即8核16线程)
             */
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workGroup = new NioEventLoopGroup();
            try {
                //服务器启动对象
                ServerBootstrap bootstrap = new ServerBootstrap();
                //
                bootstrap.group(bossGroup, workGroup)//设置两个线程组
                        .channel(NioServerSocketChannel.class)//使用NioServerSocketChannel作为服务器端的通道
                        .option(ChannelOption.SO_BACKLOG, 128)//设置线程队列最大连接数
                        .childOption(ChannelOption.SO_KEEPALIVE, true)//设置保持活跃连接状态
                        .childHandler(new ChannelInitializer<SocketChannel>() {//创建一个通道初始化对象
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline().addLast("NettyServerHandler", new NettyTcpServerHandler());
                            }
                        });//给workerGroup的EventLoop对应的管道设置处理器
                System.out.println("server is ready");
                //绑定一个端口，生成ChannelFuture对象
                //启动服务器，这边不会阻塞住下面的代码，可以注册一个监听器，等待方法执行完毕之后回调监听器
                final ChannelFuture channelFuture = bootstrap.bind(8888).sync();
                channelFuture.addListener(NettyTcpServer::operationComplete);
                //对关闭通道进行监听
                channelFuture.channel().closeFuture().sync();
            } finally {
                //关闭线程组
                bossGroup.shutdownGracefully();
                workGroup.shutdownGracefully();
            }
        }

        private static void operationComplete(Future<? super Void> future) {
            if (future.isSuccess()) {
                System.out.println("server start successful");
            } else {
                System.out.println("server start failed");
            }
        }
    }

    /**
     * 客户端只需要一个工作线程组
     */
    private static final class NettyTcpClient {
        @SneakyThrows
        public static void main(String[] args) {
            //工作线程组
            final EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                //客户端启动程序
                final Bootstrap bootstrap = new Bootstrap();
                //设置相关参数
                bootstrap.group(workerGroup)
                        .channel(NioSocketChannel.class)//设置客户端通道的实现类
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                //可以使用一个集合管理用户和SocketChannel的关系
                                //就可以将业务加入到channel中对应的NioEventLoop的taskQueue或者scheduleTaskQueue
                                System.out.println("client socket channel -> " + socketChannel.hashCode());
                                //加入自己的处理器
                                socketChannel.pipeline().addLast(new NettyClientHandler());
                            }
                        });
                System.out.println("client is ready");
                //启动客户端连接服务器端
                final ChannelFuture channelFuture = bootstrap.connect("localhost", 8888).sync();
                //为通道关闭添加监听
                channelFuture.channel().closeFuture().sync();

            } finally {
                workerGroup.shutdownGracefully();
            }
        }
    }

    /**
     * 说明：
     * 1. 我们自定义的handler，需要继承Netty规定好的HandlerAdapter(规范约束)
     * 2. 这时我们自定义的handler才是真正的handler
     */
    private static final class NettyTcpServerHandler extends ChannelInboundHandlerAdapter {
        /**
         * 读取数据实现，这里我们可以读取到客户端发送的消息
         *
         * @param ctx 上下文对象，内部含有管道pipeline(针对数据的处理)，通道channel(针对数据的读写)，连接的地址...
         * @param msg 客户端发送的数据，默认是Object类型
         */
        @SneakyThrows
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            /*System.out.println("server context -> " + ctx);
            System.out.println("handle thread -> " + Thread.currentThread().getName());
            //将msg转成ByteBuffer
            //ByteBuf由Netty提供，不是NIO的ByteBuffer
            ByteBuf buf = (ByteBuf) msg;
            System.out.println("the message from client is " + buf.toString(CharsetUtil.UTF_8));
            System.out.println("client address -> " + ctx.channel().remoteAddress());*/

            //假设有一个非常耗时的操作
            /*System.out.println("start to sleep");
            //此时客户端和服务端都会阻塞在这
            TimeUnit.SECONDS.sleep(10);
            System.out.println("end to sleep");
            ctx.writeAndFlush(Unpooled.copiedBuffer("I am awake\n", CharsetUtil.UTF_8));*/

            /*
            上面的代码，线程睡眠时，会导致客户端和服务端都阻塞
            我们想要异步执行，需要将任务提交给该channel对应的NioEventLoop的taskQueue中去执行或者scheduleTaskQueue中去执行，
            需要注意的是，taskQueue是由一个NioEventLoop维护的，即是单线程从taskQueue/scheduleTaskQueue中取任务执行
            按提交顺序执行任务
             */
            ctx.channel().eventLoop().execute(() -> {
                try {
                    TimeUnit.SECONDS.sleep(5);
                    ctx.writeAndFlush(Unpooled.copiedBuffer("I am awake from task queue1\n", CharsetUtil.UTF_8));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            ctx.channel().eventLoop().execute(() -> {
                try {
                    TimeUnit.SECONDS.sleep(5);
                    ctx.writeAndFlush(Unpooled.copiedBuffer("I am awake from task queue2\n", CharsetUtil.UTF_8));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            ctx.channel().eventLoop().schedule(() -> {
                try {
                    TimeUnit.SECONDS.sleep(5);
                    ctx.writeAndFlush(Unpooled.copiedBuffer("I am awake from task queue3\n", CharsetUtil.UTF_8));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 5, TimeUnit.SECONDS);
            System.out.println("end to sleep");
        }

        /**
         * 数据读取完毕
         */
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            //将数据写入到缓存并刷新
            //一般来讲，我们对发送的数据需要进行编码
            ctx.writeAndFlush(Unpooled.copiedBuffer("hello, client", CharsetUtil.UTF_8));
        }

        /**
         * 当发生异常时，
         * 一般我们需要关闭通道
         *
         * @param ctx   上下文
         * @param cause 处理过程中的异常信息
         */
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
        }
    }

    private static final class NettyClientHandler extends ChannelInboundHandlerAdapter {
        /**
         * 当通道就绪时触发该方法
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            System.out.println("client ctx -> " + ctx);
            ctx.writeAndFlush(Unpooled.copiedBuffer("hello,server", CharsetUtil.UTF_8));
        }

        /**
         * 当通道有读取事件时会触发
         */
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf buf = (ByteBuf) msg;
            System.out.println("the message from server is " + buf.toString(CharsetUtil.UTF_8));
            System.out.println("server client -> " + ctx.channel().remoteAddress());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
        }
    }
}
