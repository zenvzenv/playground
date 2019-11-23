package zhengwei.netty.protobuf;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * @author zhengwei AKA Awei
 * @since 2019/9/1 15:34
 */
public class NettyWithProtobufClient {
	public static void main(String[] args) {
		EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).handler(new NettyWithProtobufClientInitializer());
			Channel channel = bootstrap.connect("localhost", 8888).sync().channel();
			channel.closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

class NettyWithProtobufClientInitializer extends ChannelInitializer<SocketChannel> {
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast(new ProtobufVarint32FrameDecoder());
		pipeline.addLast(new ProtobufDecoder(StudentInfo.Student.getDefaultInstance()));
		pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
		pipeline.addLast(new ProtobufEncoder());
		pipeline.addLast(new NettyWithProtobufClientHandler());
	}
}

class NettyWithProtobufClientHandler extends SimpleChannelInboundHandler<StudentInfo.Student> {
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, StudentInfo.Student msg) throws Exception {

	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		StudentInfo.Student student = StudentInfo.Student.newBuilder().setName("zhengwei").setAge(25).setAddress("NJC").build();
		ctx.channel().writeAndFlush(student);
	}
}