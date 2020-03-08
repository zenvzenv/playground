package zhengwei.netty.zerocopy;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.Socket;

/**
 * @author zhengwei AKA Awei
 * @since 2020/3/8 16:53
 */
public class OldIOClient {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 8899);
        String file = args[0];
        final FileInputStream fileInputStream = new FileInputStream(file);
        final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        byte[] buffer = new byte[4096];
        long read;
        long total = 0;
        long startTime = System.currentTimeMillis();
        while ((read = fileInputStream.read(buffer)) >= 0) {
            total += read;
            //将buffer中的内容写出
            dataOutputStream.write(buffer);
        }
        System.out.println("total=" + total + ", time=" + (System.currentTimeMillis() - startTime));
    }
}
