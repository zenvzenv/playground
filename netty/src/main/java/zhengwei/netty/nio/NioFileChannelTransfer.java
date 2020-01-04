package zhengwei.netty.nio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * @author zhengwei AKA Awei
 * @since 2020/1/4 17:00
 */
public class NioFileChannelTransfer {
    public static void main(String[] args) {
        try (final FileInputStream fis = new FileInputStream(new File("c://temp/file.txt"));
             final FileChannel fisChannel = fis.getChannel();
             final FileOutputStream fos = new FileOutputStream(new File("c://temp/file3.txt"));
             final FileChannel fosChannel = fos.getChannel()) {
            //transfer进行拷贝
            fosChannel.transferFrom(fisChannel, 0, fisChannel.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
