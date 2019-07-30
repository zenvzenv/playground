package zhengwei.designpattern.guardedsuspension;

/**
 * 当我们的处理能力满了或者暂时无法处理新的请求时，就把新的请求挂起，过一会再去处理它
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/30 12:53
 */
public class Request {
	private final String value;

	public Request(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
