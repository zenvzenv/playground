package zhengwei.jvm.bytecode;

/**
 * 现代JVM在指向Java代码的时候，通常会将解释执行和编译执行结合起来
 *
 * 所谓解释执行，就是通过解释器读取字节码，遇到相应的指令就去执行指令
 * 所谓编译执行，是通过即时编译器(just in time,JIT)将字节码转换成本地机器码来执行，现代JVM会根据代码热点来生成相应的本地代码(机器码)
 *
 * JVM采用基于栈的指令集，而不是采用基于寄存器的指令集
 * 基于栈指令集和即寄存器指令集之间的关系：
 *  1.JVM执行指令时所采取的方式是基于栈的指令集
 *  2.基于栈的指令集的主要操作就是入栈和出战两种
 *  3.基于栈指令集的优势在于它可以在不同的平台进行移植，而基于寄存器的指令集与硬件架构紧密关联，无法做到可移植
 *  4.基于栈指令集的缺点就是完成相同的操作，指令条数通常要比寄存器指令集数量要多；基于栈的指令集是在内存种完成操作的，
 *    而基于寄存器的指令集是直接由CPU来执行的，它是在CPU的告诉缓冲区种执行的，速度要快很多。虽然虚拟机可以采用一些优化手段，
 *    但总体来说，基于栈的指令集要比基于寄存器的指令集要慢。
 * @author zhengwei AKA Sherlock
 * @since 2019/6/29 14:04
 */
public class TestByteCode5 {
	public static void main(String[] args) {
		System.out.println(111);
	}
	public int add(int a,int b,int c,int d){
		return (a+b-c)*d;
	}
	public int add(){
		int a=1;
		int b=2;
		int c=3;
		int d=4;
		return (a+b-c)*d;
	}
}
