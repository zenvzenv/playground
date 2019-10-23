package zhengwei.netty.nio;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * @author zhengwei AKA Awei
 * @since 2019/9/22 16:45
 */
public class NioTest {
    private ByteBuffer buffer;

    public static void main(String[] args) {
        System.out.println(LocalDateTime.now());
    }

    @Test
    void testNioBuffer() {
        //缓冲区
        IntBuffer buffer = IntBuffer.allocate(10);
        for (int i = 0; i < buffer.capacity() / 2; i++) {
            int random = new SecureRandom().nextInt(20);
            buffer.put(random);
        }
        System.out.println("before flip,limit->" + buffer.limit());
        //读写切换，把读转成写，把写转成读
        buffer.flip();
        System.out.println("after flip,limit->" + buffer.limit());
        System.out.println("------------------------");
        while (buffer.hasRemaining()) {
            System.out.println("read capacity->" + buffer.capacity());
            System.out.println("read limit->" + buffer.limit());
            System.out.println("read position->" + buffer.position());
            System.out.println(buffer.get());
            System.out.println("------------------------");
        }
    }

    @Test
    void testUnreadBufferAndFilp() {
        IntBuffer buffer = IntBuffer.allocate(10);
        for (int i = 0; i < buffer.capacity(); i++) {
            buffer.put(new SecureRandom().nextInt(20));
        }
        buffer.flip();
        while (buffer.hasRemaining()) {
            System.out.println(buffer.get());
        }
    }

    @Test
    void testInputStreamToNio() {
        try (FileInputStream fileInputStream = new FileInputStream("e:/restartcac.sh");
             FileChannel fileChannel = fileInputStream.getChannel()) {
            ByteBuffer buffer = ByteBuffer.allocate(512);
            //往buffer中写入数据
            fileChannel.read(buffer);
            buffer.flip();
            while (buffer.remaining() > 0) {
                final byte b = buffer.get();
                System.out.println("char->" + (char) b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testOutputStreamToNio() {
        try (FileOutputStream fileOutputStream = new FileOutputStream("e:/niotest.txt");
             FileChannel channel = fileOutputStream.getChannel()) {
            ByteBuffer buffer = ByteBuffer.allocate(512);
            final byte[] message = "hello world".getBytes();
            for (byte b : message) {
                //向缓冲区中写数据即buffer读取数据
                buffer.put(b);
            }
            //由读数据转换成写数据
            buffer.flip();
            channel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void test1() {
        //1.分配一个指定大小的缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        System.out.println("-------------allocate-----------------");
        System.out.println(byteBuffer.position());
        System.out.println(byteBuffer.limit());
        System.out.println(byteBuffer.capacity());
        //2.利用put()存入数据
        String str = "abcd";
        byteBuffer.put(str.getBytes());
        System.out.println("-------------put-----------------");
        System.out.println(byteBuffer.position());
        System.out.println(byteBuffer.limit());
        System.out.println(byteBuffer.capacity());
        //3.切换成读取数据的模式  flip()
        byteBuffer.flip();//切换到读取数据的模式
        System.out.println("-------------flip-----------------");
        System.out.println(byteBuffer.position());
        System.out.println(byteBuffer.limit());
        System.out.println(byteBuffer.capacity());
        //4.读取缓存区的数据
        byte[] dst = new byte[byteBuffer.limit()];
        byteBuffer.get(dst);
        System.out.println("-------------flip-----------------");
        System.out.println(dst);
        System.out.println(byteBuffer.position());
        System.out.println(byteBuffer.limit());
        System.out.println(byteBuffer.capacity());
        //5.rewind()可重读
        byteBuffer.rewind();
        System.out.println("-------------rewind-----------------");
        System.out.println(byteBuffer.position());
        System.out.println(byteBuffer.limit());
        System.out.println(byteBuffer.capacity());
        //6.清空缓冲区，但缓冲区的数据还在，但处于被遗忘状态
        byteBuffer.clear();
        System.out.println("-------------clear-----------------");
        System.out.println(byteBuffer.position());
        System.out.println(byteBuffer.limit());
        System.out.println(byteBuffer.capacity());
        //7.mark()
        byteBuffer.flip();
        byteBuffer.get(dst, 0, 2);
        System.out.println(byteBuffer.position());
        byteBuffer.mark();
        byteBuffer.get(dst, 2, 2);
        System.out.println(byteBuffer.position());
        byteBuffer.reset();
        System.out.println(byteBuffer.position());
        //8.remaining()
        byteBuffer.remaining();//剩余可操作数据的长度
        byteBuffer.hasRemaining();//是否还有剩余可操作数据
    }

    @Test
    void testReadAndWriteFile() {
        try (FileInputStream fis = new FileInputStream("src/main/java/zhengwei/netty/nio/input.txt");
             FileOutputStream fos = new FileOutputStream("src/main/java/zhengwei/netty/nio/output.txt")) {
            FileChannel fisChannel = fis.getChannel();
            FileChannel fosChannel = fos.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(2);
            while (true) {
                /*
                如果把下面这行注释掉的话，将会出现问题，将会无限的去写出到output.txt中。
                在第一次从fisChannel中读取2个字节的buffer，调用了flip方法之后，limit=position；position=0，fosChannel从中读取2个字节并写出到output.txt文件中，没有问题。
                在第二次循环开始的时候，由于没有调用clear方法，没有将position置为0，limit置为capacity，导致此时的position=limit，
                此时再想从fisChannel中读取数据是不可能读到的，因为position永远小于等于limit，此时的read就为0，表示读入了0个字节，而0不等于-1，后续还是会调用flip方法。又会从buffer的头部开始读取数据(buffer中还是会残存数据)
                以此类推...
                导致无限往output中写入一开始读入的数据
                 */
                buffer.clear();
                int read = fisChannel.read(buffer);
                System.out.println("read->" + read);
                if (read == -1) {
                    break;
                }
                buffer.flip();
                fosChannel.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPutBaseTypeDataIntoBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(64);
        buffer.putInt(15);
        buffer.putLong(1000L);
        buffer.putDouble(6.66);
        buffer.putChar('z');
        buffer.putShort((short) 2);
        buffer.putChar('w');
        buffer.flip();
        //注意：放进去的顺序和取出来的顺序要一致，否则会报错
        System.out.println(buffer.getInt());
        System.out.println(buffer.getLong());
        System.out.println(buffer.getDouble());
        System.out.println(buffer.getChar());
        System.out.println(buffer.getShort());
        System.out.println(buffer.getChar());
    }

    /**
     * slice buffer相当于原有buffer截取[position,limit)之间的一个快照
     * 两个buffer所使用的数据是同一份底层数组，不论哪一个对其进行修改都会影响到彼此
     * 但是position,limit和capacity是相互独立的
     */
    @Test
    void testBufferSlice() {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        for (int i = 0; i < buffer.capacity(); i++) {
            //相对操作
            buffer.put((byte) i);
        }
        //起始指针(绝对操作)
        buffer.position(2);
        //结尾指针(绝对操作)
        buffer.limit(6);
        ByteBuffer slice = buffer.slice();
        for (int i = 0; i < slice.capacity(); i++) {
            byte b = slice.get(i);
            b *= 2;
            slice.put(i, b);
        }
        //还原position(绝对操作)
        buffer.position(0);
        //还原limit(绝对操作)
        buffer.limit(buffer.capacity());
        while (buffer.hasRemaining()) {
            //相对操作
            System.out.println(buffer.get());
        }
    }

    /**
     * 我们可以将一个普通可读写buffer转换为只读buffer
     * 一个只读buffer不能转换为一个普通读写buffer
     */
    @Test
    void testReadOnlyBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        System.out.println(buffer.getClass());
        for (int i = 0; i < buffer.capacity(); i++) {
            //如果这里调用putInt方法会报java.nio.BufferOverflowException错，因为int和byte比长，所以放入几个数据之后就会占满数组
//            buffer.putInt(i);
            buffer.put((byte) i);
        }
        //只读对象
        ByteBuffer readOnlyBuffer = buffer.asReadOnlyBuffer();
        System.out.println(readOnlyBuffer.getClass());
        readOnlyBuffer.position(0);
        //不能修改，java.nio.ReadOnlyBufferException
        readOnlyBuffer.put((byte) 10);
    }
}
