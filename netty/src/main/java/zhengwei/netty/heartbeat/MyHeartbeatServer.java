package zhengwei.netty.heartbeat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * 基于Netty的心跳机制
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/24 17:47
 */
public class MyHeartbeatServer {
	public static void main(String[] args) {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO))//日志
					.childHandler(new MyHeartbeatServerInitializer());
			ChannelFuture channelFuture = serverBootstrap.bind(8888).sync();
			channelFuture.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}

class MyHeartbeatServerInitializer extends ChannelInitializer<SocketChannel> {
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		//责任链模式
		//一个请求进来和出去的所经过的handler的顺序是相反的
		//第一个参数：Netty在5秒内没检测到读操作则触发一个读空闲事件，服务端在一定时间内没有读到客户端的请求
		//第二个参数：Netty在7秒内没检测到写操作则触发一个写空闲事件，服务端接收到客户端的请求，但在一定时间内没有向客户端写数据
		//第三个参数：Netty在10秒内没检测到读写才做则触发一个读写空闲事件，在一定时间内要么没有读操作，要么没有写操作
		pipeline.addLast(new IdleStateHandler(5, 7, 10, TimeUnit.SECONDS));//空闲状态检测处理器
		pipeline.addLast(new MyHeartbeatServerHandler());
	}
}

class MyHeartbeatServerHandler extends ChannelInboundHandlerAdapter {
	//事件被触发
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent) evt;
			String eventType = null;
			switch (event.state()) {
				case READER_IDLE:
					eventType = "读空闲";
					break;
				case WRITER_IDLE:
					eventType = "写空闲";
					break;
				case ALL_IDLE:
					eventType = "读写空闲";
					break;
			}
			System.out.println(ctx.channel().remoteAddress() + " 超时事件:" + eventType);
			ctx.channel().close();
		}
	}
}