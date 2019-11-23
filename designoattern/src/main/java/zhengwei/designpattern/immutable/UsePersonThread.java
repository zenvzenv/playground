package zhengwei.designpattern.immutable;

import lombok.AllArgsConstructor;

/**
 * @author zhengwei AKA Awei
 * @since 2019/7/27 10:50
 */
@AllArgsConstructor
public class UsePersonThread extends Thread {
	private Person person;

	@Override
	public void run() {
		while (true){
			System.out.println(Thread.currentThread().getName()+" print "+person.toString());
		}
	}
}
