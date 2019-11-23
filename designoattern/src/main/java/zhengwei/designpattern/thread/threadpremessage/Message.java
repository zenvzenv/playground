package zhengwei.designpattern.thread.threadpremessage;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 消息
 * 有一个请求就会有一个线程回去·为其服务
 * @author zhengwei AKA Awei
 * @since 2019/8/10 14:59
 */
@Data
@AllArgsConstructor
public class Message {
	private String message;
}
