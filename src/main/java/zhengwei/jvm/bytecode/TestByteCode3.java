package zhengwei.jvm.bytecode;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;

/**
 * Java字节码对于this关键字的处理方式
 * 对于每一个Java类中的实例方法(非static方法)，其在编译后所生成的字节码中，方法参数的数量总会比源代码方法中参数的个数多一个(this)。
 * 它位于方法参数列表的第一个位置处，这样，我们就可以在Java的实例方法中使用this来去访问当前对象的属性以及其他方法。
 * 对于静态方法而言，是使用不了this的，因为静态方法不是属于实例的，而是属于实例所对应的那个class对象的。
 * 这个操作是在编译期间完成的，即由javac编译器在编译的时候将对this的访问转化为对一个普通实例方法参数的访问，
 * 接下来在运行期间，由JVM在调用实例方法时，自动向实例方法传入this参数。
 * 所以，在实例方法的参数列表中，至少有一个指向当前对象的局部变量。
 *
 * Java字节码对于异常的处理方式
 *  1.统一采用异常表的方式来对异常进行处理
 *  2.在jdk1.4.2之前的版本中，并不是使用异常表的方式来对异常进行处理的，而是采用特定的指令方式
 *  3.当异常处理存在finally语句块时，现代化的JVM采取的处理方式是将finally语句块的字节码拼接到每一个catch语句块的后面
 *    换句话说，程序中存在多少个catch语句块，就会在每一个catch语句块后面重复多少个finally语句块后面。
 *
 *
 * @author zhengwei AKA Sherlock
 * @since 2019/6/26 19:29
 */
public class TestByteCode3 {
	public void test(){
		try {
			InputStream is=new FileInputStream("aaa.txt");
			ServerSocket ss=new ServerSocket(8888);
			ss.accept();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		} catch (Exception e){

		} finally {
			System.out.println("finally");
		}
	}
}
