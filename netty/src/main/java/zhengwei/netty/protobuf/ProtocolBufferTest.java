package zhengwei.netty.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * 测试下从 .proto 文件中自动生成Java对象代码
 * 从代码中构造相关实例
 * 把对象实例序列化成字节数组
 * 然后从字节数组中反序列化成一个Java对象
 *
 * @author zhengwei AKA Awei
 * @since 2019/9/1 14:59
 */
public class ProtocolBufferTest {
	public static void main(String[] args) throws InvalidProtocolBufferException {
		StudentInfo.Student student = StudentInfo.Student.newBuilder().setName("zhengwei").setAge(25).setAddress("NJC").build();
		//变成字节数组之后就可以在网络上进行传输
		byte[] studentBytes = student.toByteArray();
		StudentInfo.Student student1 = StudentInfo.Student.parseFrom(studentBytes);
		System.out.println(student1);
	}
}
