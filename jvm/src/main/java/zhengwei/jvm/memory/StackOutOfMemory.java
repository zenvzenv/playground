package zhengwei.jvm.memory;

import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

/**
 * 演示虚拟机栈溢出
 * 设置虚拟机栈大小：-Xss100k
 *
 * @author zhengwei AKA Awei
 * @since 2020/1/16 19:16
 */
public class StackOutOfMemory {
    private static int length;
    public static void main(String[] args) {
        try {
            test();
        } catch (Throwable throwable){
            System.out.println(length);
            throwable.printStackTrace();
        }
    }

    public int getLength() {
        return length;
    }
    @SneakyThrows
    private static void test(){
        length++;
        TimeUnit.SECONDS.sleep(3);
        test();
    }
}
