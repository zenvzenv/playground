package zhengwei.netty.bio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BIO server demo
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/22 15:35
 */
public class BIOServer {
    private static final ExecutorService service = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(6666);
        System.out.println("server is start");
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("a client is connect");
            service.submit(() -> handler(socket));
        }
    }

    private static void handler(Socket socket) {
        try (InputStream inputStream = socket.getInputStream()) {
            System.out.println(Thread.currentThread().getName() + " is running");
            byte[] bytes = new byte[1024];
            while (true) {
                int read = inputStream.read(bytes);
                if (read != -1) {
                    System.out.println(new String(bytes, 0, read));
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
