package zhengwei.designpattern.threadcontext;

import lombok.Data;

/**
 * 线程上线文实体类
 * 封装了每个线程的参数
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/3 7:49
 */
@Data
public class ThreadContext {
	private String name;
	private String cardId;
}
