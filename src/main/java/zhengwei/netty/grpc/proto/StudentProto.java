// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: Student.proto

package zhengwei.netty.grpc.proto;

public final class StudentProto {
  private StudentProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_zhengwei_netty_grpc_proto_MyRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_zhengwei_netty_grpc_proto_MyRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_zhengwei_netty_grpc_proto_MyResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_zhengwei_netty_grpc_proto_MyResponse_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    String[] descriptorData = {
      "\n\rStudent.proto\022\031zhengwei.netty.grpc.pro" +
      "to\"\036\n\tMyRequest\022\021\n\tuser_name\030\001 \001(\t\"\037\n\nMy" +
      "Response\022\021\n\treal_name\030\001 \001(\t2x\n\016StudentSe" +
      "rvice\022f\n\025GetRealNameByUserName\022$.zhengwe" +
      "i.netty.grpc.proto.MyRequest\032%.zhengwei." +
      "netty.grpc.proto.MyResponse\"\000B+\n\031zhengwe" +
      "i.netty.grpc.protoB\014StudentProtoP\001b\006prot" +
      "o3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_zhengwei_netty_grpc_proto_MyRequest_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_zhengwei_netty_grpc_proto_MyRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_zhengwei_netty_grpc_proto_MyRequest_descriptor,
        new String[] { "UserName", });
    internal_static_zhengwei_netty_grpc_proto_MyResponse_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_zhengwei_netty_grpc_proto_MyResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_zhengwei_netty_grpc_proto_MyResponse_descriptor,
        new String[] { "RealName", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}