package zhengwei.designpattern.thread.twophasetermination;

import java.io.*;
import java.net.Socket;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/13 12:53
 */
public class ClientHandler implements Runnable {
	private final Socket socket;
	private volatile boolean running = true;

	public ClientHandler(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try (InputStream inputStream = socket.getInputStream();
		     OutputStream outputStream = socket.getOutputStream();
		     BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		     PrintWriter printWriter = new PrintWriter(outputStream)) {
			while (running) {
				String message = br.readLine();
				if (message == null) {
					break;
				}
				System.out.println("come from client -> " + message);
				printWriter.write("echo " + message + "\n");
				printWriter.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
			this.running = true;
		} finally {
			this.close();
		}
	}

	public void close() {
		try {
			if (!running) {
				return;
			}
			this.running = false;
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
