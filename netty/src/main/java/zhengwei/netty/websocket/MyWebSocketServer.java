package zhengwei.netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.time.LocalTime;

/**
 * Netty实现WebSocket的服务端
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/30 6:51
 */
public class MyWebSocketServer {
	public static void main(String[] args) {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.channel(NioServerSocketChannel.class).handler(new LoggingHandler()).childHandler(new MyWebSocketServerInitializer());
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

class MyWebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast(new HttpServerCodec());//http请求的编解码器
		pipeline.addLast(new ChunkedWriteHandler());//块写处理器
		//Netty对于各种各样的请求是采取分段或分块进行处理，而这个处理器是将所有的段请求或块请求聚合到一起形成一个完整的请求。
		pipeline.addLast(new HttpObjectAggregator(8192));
		//WebSocket协议：ws://localhost:8888/websocket
		pipeline.addLast(new WebSocketServerProtocolHandler("/websocket"));
		pipeline.addLast(new MyWebSocketServerHandler());
	}
}

/**
 * 所继承类的泛型是真正需要处理数据的类型
 */
class MyWebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
		System.out.println("get message -> " + msg.text());
		//这里不能使用普通的字符串，因为前面指定的是TextWebSocketFrame类型。
		ctx.channel().writeAndFlush(new TextWebSocketFrame("server time -> " + LocalTime.now()));
	}

	/**
	 * 客户端与服务器端建立好链接
	 *
	 * @param ctx channel上下文
	 */
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		System.out.println("handler added -> " + ctx.channel().id().asLongText());
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		System.out.println("handler removed -> " + ctx.channel().id().asLongText());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("exception");
		ctx.close();
	}
}