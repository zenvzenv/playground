package zhengwei.util.common;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * 系统工具
 * @author zhengwei AKA Sherlock
 *
 */
public class SysUtils {
	/**
	 * 非空判断
	 * @param obj 需要判断的对象
	 * @return 是否为空
	 */
	public static boolean isNotNull(Object obj){
		return !isNull(obj);
	}
	/**
	 * 空值判断
	 * @param obj 要检查的对象
	 * @return 是否为空
	 */
	public static boolean isNull(Object obj) {
		// 判断参数是否为空或者’’
		if (obj == null || "''".equals(obj) || obj.equals("null")) {
			return true;
		} else if ("java.lang.String".equals(obj.getClass().getName())) {
			// 判断传入的参数的String类型的
			// 替换各种空格
			String tmpInput = Pattern.compile("[\\r|\\n|\\u3000]").matcher(
					(String) obj).replaceAll("");
			return Pattern.compile("^(\\s)*$").matcher(tmpInput).matches();
		} else {
			Method method = null;
			try {
				// 访问传入参数的size方法
				method = obj.getClass().getMethod("size");
				// 判断size大小 size为0的场合
				return Integer.parseInt(String.valueOf(method.invoke(obj))) == 0;
			} catch (Exception e) {
				// 访问失败
				try {
					// 访问传入参数的getItemCount方法
					method = obj.getClass().getMethod("getItemCount");
					// 判断size大小 getItemCount为0的场合
					return Integer.parseInt(String.valueOf(method.invoke(obj))) == 0;
				} catch (Exception ex) {
					// 访问失败
					try {
						// 判断传入参数的长度 长度为0的场合
						return Array.getLength(obj) == 0;
					} catch (Exception exx) {
						// 访问失败
						try {
							method = Iterator.class.getMethod("hasNext");
							// 转换hasNext的值
							return !Boolean.valueOf(String.valueOf(method.invoke(obj)));
						} catch (Exception exxx) {
							// 以上场合不满足返回假
							return false;
						}
					}
				}
			}
		}
	}
}
