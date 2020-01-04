package zhengwei.netty.nio;

import java.nio.ByteBuffer;

/**
 * 对于存入ByteBuffer中的数据，在取出时需要按照存入时的顺序和类型一致，否则会报错BufferUnderflowException
 * 可以将一个普通buffer转换成read only buffer，只读buffer只能读不能写，往里面写数据会报错
 *
 * @author zhengwei AKA Awei
 * @since 2020/1/4 20:12
 */
public class NioBufferKeyPoints {
    public static void main(String[] args) {
        final ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.putInt(100);
        buffer.putLong(3L);
        buffer.putChar('z');

        System.out.println(buffer.getInt());
        System.out.println(buffer.getLong());
        System.out.println(buffer.getChar());

        //只读buffer
        final ByteBuffer readOnlyBuffer = buffer.asReadOnlyBuffer();
        while (readOnlyBuffer.hasRemaining()) {
            System.out.println(readOnlyBuffer.get());
        }
    }
}
