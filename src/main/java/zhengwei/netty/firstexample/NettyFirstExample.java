package zhengwei.netty.firstexample;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

/**
 * Netty第一个程序
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/23 20:02
 */
public class NettyFirstExample {
	public static void main(String[] args) throws InterruptedException {
		//bossGroup -> workerGroup
		//接收请求，转而转发给workerGroup
		EventLoopGroup bossGroup = new NioEventLoopGroup();//死循环，事件循环组
		//接收请求做业务处理
		EventLoopGroup workerGroup = new NioEventLoopGroup();//死循环，事件循环组
		try {
			//启动ServerChannel，即启动服务
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			//1.传入组，boosGroup接收请求然后转交给workerGroup，workerGroup去处理
			//2.声明channel的类型为NioServerSocketChannel.class，Netty通过反射生成该类的实例
			//3.指定处理器
			serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ServerInitialize());
			//绑定端口
			ChannelFuture channelFuture = serverBootstrap.bind(8888).sync();
			channelFuture.channel().closeFuture().sync();
		} finally {
			//优雅关闭
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}

/**
 * 初始化channel
 */
class ServerInitialize extends ChannelInitializer<SocketChannel> {
	//初始化管道
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		//对于外部的请求和响应的编解码器
		pipeline.addLast("HttpServerCodec", new HttpServerCodec());
		pipeline.addLast("HttpServerHandler", new HttpServerHandler());
	}
}

/**
 * 自定义Http处理器
 * 构造出Hello World的一个响应实例，并返回
 */
class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {
	//读取客户端所发过来的请求，并且返回相应的方法
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		if (msg instanceof HttpRequest) {
			//相应给客户端的内容
			ByteBuf content = Unpooled.copiedBuffer("Hello World", CharsetUtil.UTF_8);
			//响应对象
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
			//设置响应头信息
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
			response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
			//返回响应对象
			ctx.writeAndFlush(response);
		}
	}
}