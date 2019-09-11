package zhengwei.netty.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import zhengwei.netty.grpc.proto.MyRequest;
import zhengwei.netty.grpc.proto.MyResponse;

import java.util.concurrent.TimeUnit;

/**
 * @author zhengwei AKA Awei
 * @since 2019/9/11 19:36
 */
public class StudentGrpcClient {
	private final ManagedChannel managedChannel;
	//用于传输信息
	private final StudentServiceGrpc.StudentServiceBlockingStub blockingStub;

	public StudentGrpcClient(String address, int port) {
		this(ManagedChannelBuilder.forAddress(address, port)
				.usePlaintext()//无秘传输
				.build());
	}

	private StudentGrpcClient(ManagedChannel managedChannel) {
		this.managedChannel = managedChannel;
		this.blockingStub = StudentServiceGrpc.newBlockingStub(managedChannel);
	}

	private void shutdown() throws InterruptedException {
		managedChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}

	public static void main(String[] args) throws InterruptedException {
		StudentGrpcClient client = new StudentGrpcClient("localhost", 8888);
		try {
			MyRequest myRequest = MyRequest.newBuilder().setUserName("zhengwei1").build();
			MyResponse zhengwei;
			//复用同一个socket链接
			for (int i = 0; i < 100; i++) {
				zhengwei = client.blockingStub.getRealNameByUserName(myRequest);
				System.out.println(zhengwei.getRealName());
			}
		} finally {
			client.shutdown();
		}
	}
}
