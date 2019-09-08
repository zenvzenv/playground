package zhengwei.thread.atomic;

/**
 * VM_SUPPORTS_LONG_CAS
 * JVM会保证基本数据类型复制的原子性
 * 因为int,boolean,char......等数据类型的长度都小于32位或者64位，JVM可以一次性设值成功
 * 而long是64位，对于32位操作系统来说需要分两次进行复制，分别对high位和low位进行赋值
 * VM_SUPPORTS_LONG_CAS这个参数会去检查CPU是否支持无锁操作
 * 对于64位的long和double来说，JVM想要依次进行64位的交换，那么CPU应该要支持lock cmpxchg8b指令
 * (CPU的位数，如果是64位的可以支持无锁操作，否则不支持)，如果支持则进行无锁操作，否则对总线加锁
 *
 * @author zhengwei AKA Awei
 * @since 2019/9/7 19:30
 */
public class AtomicLongTest {

}
