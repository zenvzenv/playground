package zhengwei.netty.protobuf;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * Netty整合Google Protocol Buffer
 *
 * @author zhengwei AKA Awei
 * @since 2019/9/1 15:15
 */
public class NettyWithProtobufServer {
	public static void main(String[] args) {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new NettyWithProtobufServerInitializer());
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

class NettyWithProtobufServerInitializer extends ChannelInitializer<SocketChannel> {
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast(new ProtobufVarint32FrameDecoder());
		/*
		将protobuf字节数组转换成真正的对象
		这个handler所需要的参数就是转换之后的对象
		 */
		pipeline.addLast(new ProtobufDecoder(StudentInfo.Student.getDefaultInstance()));
		pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
		pipeline.addLast(new ProtobufEncoder());
		pipeline.addLast(new NettyWithProtobufServerHandler());
	}
}

class NettyWithProtobufServerHandler extends SimpleChannelInboundHandler<StudentInfo.Student> {
	/**
	 * 在进入这个方法的时候，Netty以及将字节码解码成了我们需要的StudentInfo.Student对象了
	 * 是由Netty提供的对于Protocol Buffer的编解码器来完成相关功能的
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, StudentInfo.Student msg) throws Exception {
		System.out.println(msg);
	}
}