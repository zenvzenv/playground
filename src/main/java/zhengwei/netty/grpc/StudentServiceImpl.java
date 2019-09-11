package zhengwei.netty.grpc;

import io.grpc.stub.StreamObserver;
import zhengwei.netty.grpc.proto.MyRequest;
import zhengwei.netty.grpc.proto.MyResponse;

/**
 * @author zhengwei AKA Awei
 * @since 2019/9/11 19:19
 */
public class StudentServiceImpl extends StudentServiceGrpc.StudentServiceImplBase {
	/**
	 * 给客户端发送响应
	 * @param request
	 * @param responseObserver
	 */
	@Override
	public void getRealNameByUserName(MyRequest request, StreamObserver<MyResponse> responseObserver) {
		System.out.println("接收到客户端信息->"+request.getUserName());
		MyResponse zhengwei = MyResponse.newBuilder().setRealName("zhengwei").build();
		//返回消息给客户端
		responseObserver.onNext(zhengwei);
		//通知客户端方法调用结束
		responseObserver.onCompleted();
	}
}
