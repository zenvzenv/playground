package zhengwei.designpattern.thread.consumerproductor;

import lombok.Data;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/8 13:36
 */
@Data
public class Message {
	private String message;

	public Message(String message) {
		this.message = message;
	}
}
