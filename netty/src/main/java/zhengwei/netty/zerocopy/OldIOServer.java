package zhengwei.netty.zerocopy;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author zhengwei AKA Awei
 * @since 2020/3/8 16:48
 */
public class OldIOServer {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8899);
        while (true) {
            final Socket socket = serverSocket.accept();
            final DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            byte[] buffer = new byte[4096];
            while (true) {
                final int read = dataInputStream.read(buffer, 0, buffer.length);
                if (-1 == read) break;
            }
        }
    }
}
