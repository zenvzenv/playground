package zhengwei.netty.grpc;

import io.grpc.stub.StreamObserver;
import zhengwei.netty.grpc.proto.*;

import java.util.UUID;

/**
 * @author zhengwei AKA Awei
 * @since 2019/9/11 19:19
 */
public class StudentServiceImpl extends StudentServiceGrpc.StudentServiceImplBase {
	/**
	 * 给客户端发送响应
	 *
	 * @param request
	 * @param responseObserver
	 */
	@Override
	public void getRealNameByUserName(MyRequest request, StreamObserver<MyResponse> responseObserver) {
		System.out.println("接收到客户端信息->" + request.getUserName());
		MyResponse zhengwei = MyResponse.newBuilder().setRealName("zhengwei").build();
		//返回消息给客户端
		responseObserver.onNext(zhengwei);
		//通知客户端方法调用结束
		responseObserver.onCompleted();
	}

	@Override
	public void getStudentByAge(StudentRequest request, StreamObserver<StudentResponse> responseObserver) {
		System.out.println("接收到客户端的数据->" + request.getAge());
		responseObserver.onNext(StudentResponse.newBuilder().setName("zhengwei1").setAge(18).setCity("SQC").build());
		responseObserver.onNext(StudentResponse.newBuilder().setName("zhengwei2").setAge(18).setCity("SQC").build());
		responseObserver.onNext(StudentResponse.newBuilder().setName("zhengwei3").setAge(18).setCity("SQC").build());
		responseObserver.onNext(StudentResponse.newBuilder().setName("zhengwei4").setAge(18).setCity("SQC").build());
		responseObserver.onNext(StudentResponse.newBuilder().setName("zhengwei5").setAge(18).setCity("SQC").build());
		responseObserver.onCompleted();
	}

	/**
	 * 主要是实现StreamObserver接口中的三个回调方法
	 * 在特定的时候方法将会被调用
	 */
	@Override
	public StreamObserver<StudentRequest> getStudentsWrapperByArgs(StreamObserver<StudentResponseList> responseObserver) {
		return new StreamObserver<StudentRequest>() {
			//以下方法都是针对客户端处理的
			//处理请求
			@Override
			public void onNext(StudentRequest studentRequest) {
				System.out.println("onNext->" + studentRequest.getAge());
			}

			@Override
			public void onError(Throwable throwable) {
				System.out.println(throwable.getMessage());
			}

			@Override
			public void onCompleted() {
				StudentResponse response1 = StudentResponse.newBuilder().setName("zhengwei1").setAge(18).setCity("NJ").build();
				StudentResponse response2 = StudentResponse.newBuilder().setName("zhengwei2").setAge(18).setCity("NJ").build();
				StudentResponseList list = StudentResponseList
						.newBuilder()
						.addStudentResponse(response1)
						.addStudentResponse(response2)
						.build();
				responseObserver.onNext(list);
				responseObserver.onCompleted();
			}
		};
	}

	@Override
	public StreamObserver<StreamRequest> biTalk(StreamObserver<StreamResponse> responseObserver) {
		return new StreamObserver<StreamRequest>() {
			@Override
			public void onNext(StreamRequest streamRequest) {
				System.out.println(streamRequest.getRequestInfo());
				responseObserver.onNext(StreamResponse.newBuilder().setResponseInfo(UUID.randomUUID().toString()).build());
			}

			@Override
			public void onError(Throwable throwable) {
				System.out.println(throwable.getMessage());
			}

			@Override
			public void onCompleted() {
				responseObserver.onCompleted();
			}
		};
	}
}
