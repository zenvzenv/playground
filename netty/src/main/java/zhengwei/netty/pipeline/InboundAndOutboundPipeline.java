package zhengwei.netty.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;

/**
 * @author zhengwei AKA Awei
 * @since 2020/9/29 13:47
 */
public class InboundAndOutboundPipeline {
    static class SimpleInboundHandlerA extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("入站处理器A被回调");
            super.channelRead(ctx, msg);
        }
    }

    static class SimpleInboundHandlerB extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("入站处理器B被回调");
            super.channelRead(ctx, msg);
        }
    }

    static class SimpleInboundHandlerC extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("入站处理器C被回调");
            super.channelRead(ctx, msg);
        }
    }

    static class SimpleOutboundHandlerA extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            System.out.println("出站处理器A被回调");
            super.write(ctx, msg, promise);
        }
    }

    static class SimpleOutboundHandlerB extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            System.out.println("出站处理器B被回调");
            super.write(ctx, msg, promise);
        }
    }

    static class SimpleOutboundHandlerC extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            System.out.println("出站处理器C被回调");
            super.write(ctx, msg, promise);
        }
    }

    public static void main(String[] args) {
        ChannelInitializer<EmbeddedChannel> initializer = new ChannelInitializer<EmbeddedChannel>() {
            @Override
            protected void initChannel(EmbeddedChannel ch) throws Exception {
                //入站顺序是 A -> B -> C
                ch.pipeline().addLast(new SimpleInboundHandlerA());
                ch.pipeline().addLast(new SimpleInboundHandlerB());
                ch.pipeline().addLast(new SimpleInboundHandlerC());

                //出站顺序 C -> B -> A
                ch.pipeline().addLast(new SimpleOutboundHandlerA());
                ch.pipeline().addLast(new SimpleOutboundHandlerB());
                ch.pipeline().addLast(new SimpleOutboundHandlerC());
            }
        };
        EmbeddedChannel channel = new EmbeddedChannel(initializer);
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(1);
        channel.writeInbound(buf);
        channel.writeOutbound(buf);
    }
}
