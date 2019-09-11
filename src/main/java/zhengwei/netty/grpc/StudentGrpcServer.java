package zhengwei.netty.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author zhengwei AKA Awei
 * @since 2019/9/11 19:26
 */
public class StudentGrpcServer {
	private Server server;

	//启动服务器的方法
	private void start() {
		try {
			this.server = ServerBuilder.forPort(8888).addService(new StudentServiceImpl()).build().start();
			System.out.println("server started");
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println("关闭JVM,释放资源");
				this.stop();
			}));
			System.out.println("钩子函数使异步执行的");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//关闭服务器
	private void stop() {
		if (null != this.server) {
			this.server.shutdown();
		}
	}

	//使服务器一直运行
	private void awaitTermination() {
		if (server != null) {
			try {
				this.server.awaitTermination();
				//指定等待时间
				this.server.awaitTermination(3000, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		StudentGrpcServer studentGrpcServer = new StudentGrpcServer();
		studentGrpcServer.start();
		studentGrpcServer.awaitTermination();
	}
}
