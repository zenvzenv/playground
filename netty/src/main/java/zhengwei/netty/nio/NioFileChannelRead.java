package zhengwei.netty.nio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author zhengwei AKA Awei
 * @since 2020/1/4 15:57
 */
public class NioFileChannelRead {
    public static void main(String[] args) {
        try (final FileInputStream fis = new FileInputStream(new File("c://temp/file.txt"));//输入流
             final FileChannel channel = fis.getChannel()) {
            final ByteBuffer buffer = ByteBuffer.allocate(1024);
            //将channel中的数据读出到缓冲区中
            channel.read(buffer);
            //反转buffer
            buffer.flip();
            System.out.println(new String(buffer.array()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
