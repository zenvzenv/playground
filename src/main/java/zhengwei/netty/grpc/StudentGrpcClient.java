package zhengwei.netty.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import zhengwei.netty.grpc.proto.*;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * @author zhengwei AKA Awei
 * @since 2019/9/11 19:36
 */
public class StudentGrpcClient {
	private final ManagedChannel managedChannel;
	//用于传输信息
	private final StudentServiceGrpc.StudentServiceBlockingStub blockingStub;
	//异步stub，任何方法都可以通过异步的方式进行调用
	private final StudentServiceGrpc.StudentServiceStub stub;

	public StudentGrpcClient(String address, int port) {
		this(ManagedChannelBuilder.forAddress(address, port)
				.usePlaintext()//无秘传输
				.build());
	}

	private StudentGrpcClient(ManagedChannel managedChannel) {
		this.managedChannel = managedChannel;
		//阻塞stub，等待响应
		this.blockingStub = StudentServiceGrpc.newBlockingStub(managedChannel);
		this.stub = StudentServiceGrpc.newStub(managedChannel);
	}

	private void shutdown() throws InterruptedException {
		managedChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}

	private MyResponse getRealNameByUserName(MyRequest request) {
		return blockingStub.getRealNameByUserName(request);
	}

	private Iterator<StudentResponse> getStudentsByAge(StudentRequest request) {
		return blockingStub.getStudentByAge(request);
	}

	private void getStudentsWrapperByAgs() {
		StreamObserver<StudentResponseList> studentResponseListStreamObserver = new StreamObserver<StudentResponseList>() {
			@Override
			public void onNext(StudentResponseList studentResponseList) {
				studentResponseList.getStudetnResponseList().forEach(studentResponse -> {
					System.out.println(studentResponse.getName());
					System.out.println(studentResponse.getAge());
					System.out.println(studentResponse.getCity());
					System.out.println("***************************");
				});
			}

			@Override
			public void onError(Throwable throwable) {
				System.out.println(throwable.getMessage());
			}

			@Override
			public void onCompleted() {
				System.out.println("completed");
			}
		};
		StreamObserver<StudentRequest> studentRequestStreamObserver= stub.getStudentsWrapperByAgs(studentResponseListStreamObserver);
		studentRequestStreamObserver.onNext(StudentRequest.newBuilder().setAge(18).build());
		studentRequestStreamObserver.onNext(StudentRequest.newBuilder().setAge(19).build());
		studentRequestStreamObserver.onNext(StudentRequest.newBuilder().setAge(20).build());
		studentRequestStreamObserver.onNext(StudentRequest.newBuilder().setAge(21).build());
		studentRequestStreamObserver.onCompleted();
	}

	public static void main(String[] args) throws InterruptedException {
		StudentGrpcClient client = new StudentGrpcClient("localhost", 8888);
		try {
			MyRequest myRequest = MyRequest.newBuilder().setUserName("zhengwei1").build();
			MyResponse zhengwei;
			//复用同一个socket链接
			/*for (int i = 0; i < 100; i++) {
				zhengwei = client.getRealNameByUserName(myRequest);
				System.out.println(zhengwei.getRealName());
			}*/
			/*client.getStudentsByAge(StudentRequest.newBuilder().setAge(18).build()).forEachRemaining(x -> {
				System.out.println(x.getName());
				System.out.println(x.getAge());
				System.out.println(x.getCity());
			});*/
			client.getStudentsWrapperByAgs();
			Thread.sleep(5000);
		} finally {
			client.shutdown();
		}
	}
}
