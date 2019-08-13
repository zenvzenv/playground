package zhengwei.designpattern.thread.twophasetermination;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/12 12:58
 */
public class AppServer extends Thread {
	//服务端口
	private final int port;
	private volatile boolean start = true;
	//记录工作线程
	private List<ClientHandler> clientHandlers = new ArrayList<>();
	private ServerSocket server;
	public static final ExecutorService service = Executors.newFixedThreadPool(10);

	public AppServer(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		try {
			server = new ServerSocket(this.port);
			while (start) {
				//会阻塞住
				Socket client = server.accept();
				ClientHandler clientHandler = new ClientHandler(client);
				service.submit(clientHandler);
				this.clientHandlers.add(clientHandler);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			this.dispose();
		}
	}

	private void dispose() {
		clientHandlers.forEach(ClientHandler::close);
		service.shutdown();
	}

	public void shutdown() throws IOException {
		this.start = false;
		this.server.close();
		this.interrupt();
	}
}
