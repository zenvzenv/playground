package zhengwei.netty.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 堆上缓冲HeapByteBuffer和直接缓冲DirectByteBuffer的区别
 * 堆上缓冲，所申请的缓冲对象是由JVM全权管理，不论是HeapByteBuffer还是数据都分配在堆内存上
 * 直接缓冲，所申请的缓冲对象DirectByteBuffer对象由JVM管理，但是其中的数据则是由OS进行管理，DirectByteBuffer中有一个address成员变量来指向该数据所在的内存地址，以便访问该数据
 *
 * 什么是零拷贝？
 * 对于堆上缓冲，如果想要操作IO(与外部系统交换你数据)的话，并不会直接去操作heap上的HeapByteBuffer封装的那个字节数组，而是会将封装的那个字节数据拷贝一份到堆外内存中，然后再对堆外内存的数据进行操作
 * 而直接缓冲，数据已经在堆外内存的了，OS可以直接对其进行操作，而不需要再从heap中复制一份出来了，少了一次数据拷贝的过程，这就是零拷贝
 *
 * 为什么OS不去直接操作JVM堆上的HeapByteBuffer中的数据呢？
 * JVM由GC操作，既然需要和IO打交道，那么对象在内存中的地址就是要确定的，
 * JVM的垃圾回收是需要标记->回收->压缩，这些步骤结束之后，JVM中的内存地址就会发生变换了，原来的对象的地址可能就会变掉，这不利于直接进行IO操作，所以要复制出来一份到堆外内存之后再进行操作。
 *
 * @author zhengwei AKA Awei
 * @since 2019/12/1 9:55
 */
public class DirectByteBufferDemo {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("src/main/java/zhengwei/netty/nio/input.txt");
             FileOutputStream fos = new FileOutputStream("src/main/java/zhengwei/netty/nio/output.txt")) {
            FileChannel fisChannel = fis.getChannel();
            FileChannel fosChannel = fos.getChannel();
            //使用直接内存，即JVM堆外内存
            ByteBuffer buffer = ByteBuffer.allocateDirect(2);
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
}
