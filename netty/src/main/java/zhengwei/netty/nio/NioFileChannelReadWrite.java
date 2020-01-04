package zhengwei.netty.nio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author zhengwei AKA Awei
 * @since 2020/1/4 16:26
 */
public class NioFileChannelReadWrite {
    public static void main(String[] args) {
        try (final FileInputStream fis = new FileInputStream(new File("c://temp/file.txt"));
             final FileChannel fisChannel = fis.getChannel();
             final FileOutputStream fos = new FileOutputStream(new File("c://temp/file2.txt"));
             final FileChannel fosChannel = fos.getChannel()) {
            final ByteBuffer buffer = ByteBuffer.allocate(512);
            while (true) {
                //需要重置buffer中的标志位
                //如果不重置标志位的话，那么position和limit的值将会一直相等，从而导致死循环
                buffer.clear();
                //从channel中读出
                int read = fisChannel.read(buffer);
                if (read == -1) {
                    break;
                }
                //转换buffer的模式
                buffer.flip();
                //往channel写入
                fosChannel.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
