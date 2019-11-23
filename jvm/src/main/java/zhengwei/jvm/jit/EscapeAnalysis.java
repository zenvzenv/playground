package zhengwei.jvm.jit;

/**
 * 逃逸分析
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/7 19:51
 */
public class EscapeAnalysis {
	public static Object object;
	public void globalVariableEscape(){//全局变量赋值逃逸
		object =new Object();
	}

	public Object methodEscape(){  //方法返回值逃逸
		return new Object();
	}

	public void instancePassEscape(){ //实例引用发生逃逸
		this.speak(this);
	}

	public void speak(EscapeAnalysis escapeAnalysis){
		System.out.println("Escape Hello");
	}
	/*
	方法逃逸-返回值逃逸
	 */
	public StringBuffer createString(String ... values){
		StringBuffer stringBuffer = new StringBuffer();
		for (String string : values) {
			stringBuffer.append(string+",");
		}
		return stringBuffer;
	}

	/*
	我们可以通过改变返回值得类型为String限定了StringBuffer的作用域在createString方法中从而不发生逃逸。
	 */
	public String createString2(String ... values){
		StringBuffer stringBuffer = new StringBuffer();
		for (String string : values) {
			stringBuffer.append(string+",");
		}
		return stringBuffer.toString();
	}

}
