package zhengwei.designpattern.thread.activeobjects;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/21 19:45
 */
class Servant implements ActiveObjects {
	@Override
	public Result makeString(int count, char fillChar) {
		char[] buffer=new char[count];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i]=fillChar;
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
//				e.printStackTrace();
			}
		}
		return new RealResult(new String(buffer));
	}

	@Override
	public void displayString(String text) {
		try {
			System.out.println("Display -> "+text);
			Thread.sleep(10_000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
