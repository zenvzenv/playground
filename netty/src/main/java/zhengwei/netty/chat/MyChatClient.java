package zhengwei.netty.chat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 基于Netty的聊天室的客户端的简单实现
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/24 17:13
 */
public class MyChatClient {
	public static void main(String[] args) {
		EventLoopGroup eventExecutors = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(eventExecutors).channel(NioSocketChannel.class).handler(new MyChatClientInitializer());
			Channel channel = bootstrap.connect("localhost", 8888).sync().channel();
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				channel.writeAndFlush(br.readLine() + "\r\n");
			}
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		} finally {
			eventExecutors.shutdownGracefully();
		}
	}
}

class MyChatClientInitializer extends ChannelInitializer<SocketChannel> {
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		//根据分隔符对内容进行解码
		pipeline.addLast(new DelimiterBasedFrameDecoder(4096, Delimiters.lineDelimiter()));
		pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
		pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
		pipeline.addLast(new MyChatClientHandler());
	}
}

class MyChatClientHandler extends SimpleChannelInboundHandler<String> {
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		System.out.println(msg);
	}
}