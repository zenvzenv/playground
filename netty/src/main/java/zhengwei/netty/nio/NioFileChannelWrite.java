package zhengwei.netty.nio;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author zhengwei AKA Awei
 * @since 2020/1/4 15:50
 */
public class NioFileChannelWrite {
    public static void main(String[] args) {
        String content = "zhengwei1";
        try (FileOutputStream fos = new FileOutputStream("c://temp/file.txt");
             final FileChannel channel = fos.getChannel()) {//stream对channel封装一层，实际类型为FileChannelImpl
            //创建缓冲区
            final ByteBuffer buffer = ByteBuffer.allocate(1024);
            //往缓冲区中放入数据
            buffer.put(content.getBytes());
            //反转缓冲区，由读模式转成写模式
            buffer.flip();
            channel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
