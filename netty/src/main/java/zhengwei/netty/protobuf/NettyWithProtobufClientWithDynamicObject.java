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

import java.util.Random;

/**
 * @author zhengwei AKA Awei
 * @since 2019/9/1 15:34
 */
public class NettyWithProtobufClientWithDynamicObject {
	public static void main(String[] args) {
		EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).handler(new NettyWithProtobufClientInitializerWithDynamicObject());
			Channel channel = bootstrap.connect("localhost", 8888).sync().channel();
			channel.closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

class NettyWithProtobufClientInitializerWithDynamicObject extends ChannelInitializer<SocketChannel> {
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast(new ProtobufVarint32FrameDecoder());
		pipeline.addLast(new ProtobufDecoder(MyDataInfo.MyMessage.getDefaultInstance()));
		pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
		pipeline.addLast(new ProtobufEncoder());
		pipeline.addLast(new NettyWithProtobufClientHandlerWithDynamicObject());
	}
}

class NettyWithProtobufClientHandlerWithDynamicObject extends SimpleChannelInboundHandler<MyDataInfo.MyMessage> {
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, MyDataInfo.MyMessage msg) throws Exception {

	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		int random = new Random(System.currentTimeMillis()).nextInt(3);
		MyDataInfo.MyMessage.Builder message;
		if (0 == random) {
			MyDataInfo.Person person = MyDataInfo.Person.newBuilder().setName("zhengwei").setAge(25).setAddress("NJC").build();
			message = MyDataInfo.MyMessage.newBuilder().setDataType(MyDataInfo.MyMessage.DataType.PersonType).setPerson(person);
		} else if (1 == random) {
			MyDataInfo.Dog dog = MyDataInfo.Dog.newBuilder().setName("dog").setAge(10).build();
			message = MyDataInfo.MyMessage.newBuilder().setDataType(MyDataInfo.MyMessage.DataType.DogType).setDog(dog);
		} else {
			MyDataInfo.Cat cat = MyDataInfo.Cat.newBuilder().setName("cat").setCity("NJ").build();
			message = MyDataInfo.MyMessage.newBuilder().setDataType(MyDataInfo.MyMessage.DataType.CatType).setCat(cat);
		}
		ctx.channel().writeAndFlush(message);
	}
}