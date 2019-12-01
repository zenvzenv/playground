package zhengwei.netty.nio;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 内存映射文件
 *
 * @author zhengwei AKA Awei
 * @since 2019/12/1 11:00
 */
public class MappedByteBufferDemo {
    public static void main(String[] args) {
        try(RandomAccessFile randomAccessFile = new RandomAccessFile("111.txt", "rw")) {
            FileChannel channel = randomAccessFile.getChannel();
            MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 5);
            mappedByteBuffer.put(0, (byte) 'a');
            mappedByteBuffer.put(3, (byte) 'b');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
