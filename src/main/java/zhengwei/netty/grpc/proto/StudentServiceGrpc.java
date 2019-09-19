package zhengwei.netty.grpc.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.23.0)",
    comments = "Source: Student.proto")
public final class StudentServiceGrpc {

  private StudentServiceGrpc() {}

  public static final String SERVICE_NAME = "zhengwei.netty.grpc.proto.StudentService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<zhengwei.netty.grpc.proto.MyRequest,
      zhengwei.netty.grpc.proto.MyResponse> getGetRealNameByUserNameMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetRealNameByUserName",
      requestType = zhengwei.netty.grpc.proto.MyRequest.class,
      responseType = zhengwei.netty.grpc.proto.MyResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<zhengwei.netty.grpc.proto.MyRequest,
      zhengwei.netty.grpc.proto.MyResponse> getGetRealNameByUserNameMethod() {
    io.grpc.MethodDescriptor<zhengwei.netty.grpc.proto.MyRequest, zhengwei.netty.grpc.proto.MyResponse> getGetRealNameByUserNameMethod;
    if ((getGetRealNameByUserNameMethod = StudentServiceGrpc.getGetRealNameByUserNameMethod) == null) {
      synchronized (StudentServiceGrpc.class) {
        if ((getGetRealNameByUserNameMethod = StudentServiceGrpc.getGetRealNameByUserNameMethod) == null) {
          StudentServiceGrpc.getGetRealNameByUserNameMethod = getGetRealNameByUserNameMethod =
              io.grpc.MethodDescriptor.<zhengwei.netty.grpc.proto.MyRequest, zhengwei.netty.grpc.proto.MyResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetRealNameByUserName"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  zhengwei.netty.grpc.proto.MyRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  zhengwei.netty.grpc.proto.MyResponse.getDefaultInstance()))
              .setSchemaDescriptor(new StudentServiceMethodDescriptorSupplier("GetRealNameByUserName"))
              .build();
        }
      }
    }
    return getGetRealNameByUserNameMethod;
  }

  private static volatile io.grpc.MethodDescriptor<zhengwei.netty.grpc.proto.StudentRequest,
      zhengwei.netty.grpc.proto.StudentResponse> getGetStudentByAgeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetStudentByAge",
      requestType = zhengwei.netty.grpc.proto.StudentRequest.class,
      responseType = zhengwei.netty.grpc.proto.StudentResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<zhengwei.netty.grpc.proto.StudentRequest,
      zhengwei.netty.grpc.proto.StudentResponse> getGetStudentByAgeMethod() {
    io.grpc.MethodDescriptor<zhengwei.netty.grpc.proto.StudentRequest, zhengwei.netty.grpc.proto.StudentResponse> getGetStudentByAgeMethod;
    if ((getGetStudentByAgeMethod = StudentServiceGrpc.getGetStudentByAgeMethod) == null) {
      synchronized (StudentServiceGrpc.class) {
        if ((getGetStudentByAgeMethod = StudentServiceGrpc.getGetStudentByAgeMethod) == null) {
          StudentServiceGrpc.getGetStudentByAgeMethod = getGetStudentByAgeMethod =
              io.grpc.MethodDescriptor.<zhengwei.netty.grpc.proto.StudentRequest, zhengwei.netty.grpc.proto.StudentResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetStudentByAge"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  zhengwei.netty.grpc.proto.StudentRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  zhengwei.netty.grpc.proto.StudentResponse.getDefaultInstance()))
              .setSchemaDescriptor(new StudentServiceMethodDescriptorSupplier("GetStudentByAge"))
              .build();
        }
      }
    }
    return getGetStudentByAgeMethod;
  }

  private static volatile io.grpc.MethodDescriptor<zhengwei.netty.grpc.proto.StudentRequest,
      zhengwei.netty.grpc.proto.StudentResponseList> getGetStudentsWrapperByArgsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetStudentsWrapperByArgs",
      requestType = zhengwei.netty.grpc.proto.StudentRequest.class,
      responseType = zhengwei.netty.grpc.proto.StudentResponseList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<zhengwei.netty.grpc.proto.StudentRequest,
      zhengwei.netty.grpc.proto.StudentResponseList> getGetStudentsWrapperByArgsMethod() {
    io.grpc.MethodDescriptor<zhengwei.netty.grpc.proto.StudentRequest, zhengwei.netty.grpc.proto.StudentResponseList> getGetStudentsWrapperByArgsMethod;
    if ((getGetStudentsWrapperByArgsMethod = StudentServiceGrpc.getGetStudentsWrapperByArgsMethod) == null) {
      synchronized (StudentServiceGrpc.class) {
        if ((getGetStudentsWrapperByArgsMethod = StudentServiceGrpc.getGetStudentsWrapperByArgsMethod) == null) {
          StudentServiceGrpc.getGetStudentsWrapperByArgsMethod = getGetStudentsWrapperByArgsMethod =
              io.grpc.MethodDescriptor.<zhengwei.netty.grpc.proto.StudentRequest, zhengwei.netty.grpc.proto.StudentResponseList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetStudentsWrapperByArgs"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  zhengwei.netty.grpc.proto.StudentRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  zhengwei.netty.grpc.proto.StudentResponseList.getDefaultInstance()))
              .setSchemaDescriptor(new StudentServiceMethodDescriptorSupplier("GetStudentsWrapperByArgs"))
              .build();
        }
      }
    }
    return getGetStudentsWrapperByArgsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<zhengwei.netty.grpc.proto.StreamRequest,
      zhengwei.netty.grpc.proto.StreamResponse> getBiTalkMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BiTalk",
      requestType = zhengwei.netty.grpc.proto.StreamRequest.class,
      responseType = zhengwei.netty.grpc.proto.StreamResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<zhengwei.netty.grpc.proto.StreamRequest,
      zhengwei.netty.grpc.proto.StreamResponse> getBiTalkMethod() {
    io.grpc.MethodDescriptor<zhengwei.netty.grpc.proto.StreamRequest, zhengwei.netty.grpc.proto.StreamResponse> getBiTalkMethod;
    if ((getBiTalkMethod = StudentServiceGrpc.getBiTalkMethod) == null) {
      synchronized (StudentServiceGrpc.class) {
        if ((getBiTalkMethod = StudentServiceGrpc.getBiTalkMethod) == null) {
          StudentServiceGrpc.getBiTalkMethod = getBiTalkMethod =
              io.grpc.MethodDescriptor.<zhengwei.netty.grpc.proto.StreamRequest, zhengwei.netty.grpc.proto.StreamResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "BiTalk"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  zhengwei.netty.grpc.proto.StreamRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  zhengwei.netty.grpc.proto.StreamResponse.getDefaultInstance()))
              .setSchemaDescriptor(new StudentServiceMethodDescriptorSupplier("BiTalk"))
              .build();
        }
      }
    }
    return getBiTalkMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static StudentServiceStub newStub(io.grpc.Channel channel) {
    return new StudentServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static StudentServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new StudentServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static StudentServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new StudentServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class StudentServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void getRealNameByUserName(zhengwei.netty.grpc.proto.MyRequest request,
        io.grpc.stub.StreamObserver<zhengwei.netty.grpc.proto.MyResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetRealNameByUserNameMethod(), responseObserver);
    }

    /**
     * <pre>
     *请求的参数和返回的参数必须使message类型的，否则会报Expected message type.
     *和Thrift不同，Thrift可以在返回和请求参数中指定基本类型
     *所谓流式数据其实就是一个迭代器Iterator,每返回一个数据就会放入到迭代器中
     * </pre>
     */
    public void getStudentByAge(zhengwei.netty.grpc.proto.StudentRequest request,
        io.grpc.stub.StreamObserver<zhengwei.netty.grpc.proto.StudentResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetStudentByAgeMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<zhengwei.netty.grpc.proto.StudentRequest> getStudentsWrapperByArgs(
        io.grpc.stub.StreamObserver<zhengwei.netty.grpc.proto.StudentResponseList> responseObserver) {
      return asyncUnimplementedStreamingCall(getGetStudentsWrapperByArgsMethod(), responseObserver);
    }

    /**
     * <pre>
     *双向流式数据传递
     * </pre>
     */
    public io.grpc.stub.StreamObserver<zhengwei.netty.grpc.proto.StreamRequest> biTalk(
        io.grpc.stub.StreamObserver<zhengwei.netty.grpc.proto.StreamResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(getBiTalkMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGetRealNameByUserNameMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                zhengwei.netty.grpc.proto.MyRequest,
                zhengwei.netty.grpc.proto.MyResponse>(
                  this, METHODID_GET_REAL_NAME_BY_USER_NAME)))
          .addMethod(
            getGetStudentByAgeMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                zhengwei.netty.grpc.proto.StudentRequest,
                zhengwei.netty.grpc.proto.StudentResponse>(
                  this, METHODID_GET_STUDENT_BY_AGE)))
          .addMethod(
            getGetStudentsWrapperByArgsMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                zhengwei.netty.grpc.proto.StudentRequest,
                zhengwei.netty.grpc.proto.StudentResponseList>(
                  this, METHODID_GET_STUDENTS_WRAPPER_BY_ARGS)))
          .addMethod(
            getBiTalkMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                zhengwei.netty.grpc.proto.StreamRequest,
                zhengwei.netty.grpc.proto.StreamResponse>(
                  this, METHODID_BI_TALK)))
          .build();
    }
  }

  /**
   */
  public static final class StudentServiceStub extends io.grpc.stub.AbstractStub<StudentServiceStub> {
    private StudentServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private StudentServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StudentServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new StudentServiceStub(channel, callOptions);
    }

    /**
     */
    public void getRealNameByUserName(zhengwei.netty.grpc.proto.MyRequest request,
        io.grpc.stub.StreamObserver<zhengwei.netty.grpc.proto.MyResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetRealNameByUserNameMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *请求的参数和返回的参数必须使message类型的，否则会报Expected message type.
     *和Thrift不同，Thrift可以在返回和请求参数中指定基本类型
     *所谓流式数据其实就是一个迭代器Iterator,每返回一个数据就会放入到迭代器中
     * </pre>
     */
    public void getStudentByAge(zhengwei.netty.grpc.proto.StudentRequest request,
        io.grpc.stub.StreamObserver<zhengwei.netty.grpc.proto.StudentResponse> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetStudentByAgeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<zhengwei.netty.grpc.proto.StudentRequest> getStudentsWrapperByArgs(
        io.grpc.stub.StreamObserver<zhengwei.netty.grpc.proto.StudentResponseList> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(getGetStudentsWrapperByArgsMethod(), getCallOptions()), responseObserver);
    }

    /**
     * <pre>
     *双向流式数据传递
     * </pre>
     */
    public io.grpc.stub.StreamObserver<zhengwei.netty.grpc.proto.StreamRequest> biTalk(
        io.grpc.stub.StreamObserver<zhengwei.netty.grpc.proto.StreamResponse> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getBiTalkMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   */
  public static final class StudentServiceBlockingStub extends io.grpc.stub.AbstractStub<StudentServiceBlockingStub> {
    private StudentServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private StudentServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StudentServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new StudentServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public zhengwei.netty.grpc.proto.MyResponse getRealNameByUserName(zhengwei.netty.grpc.proto.MyRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetRealNameByUserNameMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     *请求的参数和返回的参数必须使message类型的，否则会报Expected message type.
     *和Thrift不同，Thrift可以在返回和请求参数中指定基本类型
     *所谓流式数据其实就是一个迭代器Iterator,每返回一个数据就会放入到迭代器中
     * </pre>
     */
    public java.util.Iterator<zhengwei.netty.grpc.proto.StudentResponse> getStudentByAge(
        zhengwei.netty.grpc.proto.StudentRequest request) {
      return blockingServerStreamingCall(
          getChannel(), getGetStudentByAgeMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class StudentServiceFutureStub extends io.grpc.stub.AbstractStub<StudentServiceFutureStub> {
    private StudentServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private StudentServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StudentServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new StudentServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<zhengwei.netty.grpc.proto.MyResponse> getRealNameByUserName(
        zhengwei.netty.grpc.proto.MyRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetRealNameByUserNameMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_REAL_NAME_BY_USER_NAME = 0;
  private static final int METHODID_GET_STUDENT_BY_AGE = 1;
  private static final int METHODID_GET_STUDENTS_WRAPPER_BY_ARGS = 2;
  private static final int METHODID_BI_TALK = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final StudentServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(StudentServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_REAL_NAME_BY_USER_NAME:
          serviceImpl.getRealNameByUserName((zhengwei.netty.grpc.proto.MyRequest) request,
              (io.grpc.stub.StreamObserver<zhengwei.netty.grpc.proto.MyResponse>) responseObserver);
          break;
        case METHODID_GET_STUDENT_BY_AGE:
          serviceImpl.getStudentByAge((zhengwei.netty.grpc.proto.StudentRequest) request,
              (io.grpc.stub.StreamObserver<zhengwei.netty.grpc.proto.StudentResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_STUDENTS_WRAPPER_BY_ARGS:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.getStudentsWrapperByArgs(
              (io.grpc.stub.StreamObserver<zhengwei.netty.grpc.proto.StudentResponseList>) responseObserver);
        case METHODID_BI_TALK:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.biTalk(
              (io.grpc.stub.StreamObserver<zhengwei.netty.grpc.proto.StreamResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class StudentServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    StudentServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return zhengwei.netty.grpc.proto.StudentProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("StudentService");
    }
  }

  private static final class StudentServiceFileDescriptorSupplier
      extends StudentServiceBaseDescriptorSupplier {
    StudentServiceFileDescriptorSupplier() {}
  }

  private static final class StudentServiceMethodDescriptorSupplier
      extends StudentServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    StudentServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (StudentServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new StudentServiceFileDescriptorSupplier())
              .addMethod(getGetRealNameByUserNameMethod())
              .addMethod(getGetStudentByAgeMethod())
              .addMethod(getGetStudentsWrapperByArgsMethod())
              .addMethod(getBiTalkMethod())
              .build();
        }
      }
    }
    return result;
  }
}
