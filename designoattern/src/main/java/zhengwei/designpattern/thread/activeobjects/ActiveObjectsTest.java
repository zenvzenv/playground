package zhengwei.designpattern.thread.activeobjects;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/21 20:44
 */
public class ActiveObjectsTest {
	public static void main(String[] args) {
//		System.gc();
		ActiveObjectsFactory<String> activeObjectsFactory=new ActiveObjectsFactory<String>();
		ActiveObjects<String> activeObjects = activeObjectsFactory.createActiveObjects();
		new MakeStringClientThread<>(activeObjects, "zhengwei1");
		new MakeStringClientThread<>(activeObjects, "zhengwei2");
		new MakeStringClientThread<>(activeObjects, "zhengwei3");
		new DisplayStringClientThread<>("awei1",activeObjects);
//		new DisplayStringClientThread<>("awei2",activeObjects);
//		new DisplayStringClientThread<>("awei3",activeObjects);
	}
}
