package zhengwei.netty.chat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 基于Netty的聊天室的简单实现
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/24 15:56
 */
public class MyChatServer {
	public static void main(String[] args) {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new MyChatServerInitializer());
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

class MyChatServerInitializer extends ChannelInitializer<SocketChannel> {
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		//根据分隔符对内容进行解码
		pipeline.addLast(new DelimiterBasedFrameDecoder(4096, Delimiters.lineDelimiter()));
		pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
		pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
		pipeline.addLast(new MyChatServerHandler());
	}
}

class MyChatServerHandler extends SimpleChannelInboundHandler<String> {
	//保存连接所有到服务器的channel
	private static final ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		//发送消息的channel对象
		Channel channel = ctx.channel();
		CHANNEL_GROUP.forEach(ch -> {
			if (channel != ch) {//向除了自己之外的channel对象发送消息
				ch.writeAndFlush("[MESSAGE]" + channel.remoteAddress() + " - send message -> " + msg + "\n");
			} else {
				ch.writeAndFlush("[SELF] " + msg + "\n");
			}
		});
	}

	//客户端与服务器端已经建立好连接
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		Channel channel = ctx.channel();//就是一个连接(connection)
		//通知其他channel，此客户端已经建立连接
		//遍历ChannelGroup中所有对象，向ChannelGroup中的每个Channel写出相同的内容
		CHANNEL_GROUP.writeAndFlush("[SERVER] - " + channel.remoteAddress() + " join\n");
		CHANNEL_GROUP.add(channel);
	}

	//客户端与服务器端断开连接
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		Channel channel = ctx.channel();
		//把断开的Channel移除ChannelGroup，但是Netty会自动移除，无需手动移除
		CHANNEL_GROUP.remove(channel);
		CHANNEL_GROUP.writeAndFlush("[SERVER] - " + channel.remoteAddress() + " leave\n");
	}

	//客户端上线
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Channel channel = ctx.channel();
		System.out.println("[INFO] - " + channel.remoteAddress() + " is online");
	}

	//客户端下线
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("[INFO] - " + ctx.channel().remoteAddress() + " is out line");
	}

	//异常处理
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}