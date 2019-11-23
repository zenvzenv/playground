package zhengwei.thread.atomic;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zhengwei AKA Awei
 * @since 2019/9/10 13:21
 */
public class AtomicReferenceTest {
	@Test
	void testAtomicReference() {
		AtomicReference<Person> personAtomic = new AtomicReference<>(new Person("zhengwei", 18));
		System.out.println(personAtomic.get());
		//使用场景，容器中对于引用的更改
		//对于引用类型最终还是判断对象的偏移量来判断是不是同一个对象，而不是通过hashcode或equals
		System.out.println(personAtomic.compareAndSet(new Person("zhengwei", 18), new Person("zhengwei", 18)));
		JButton button = new JButton();
		//可以包装一个对象进行使用
		AtomicReference<Person> a = new AtomicReference<>(new Person("zhengwei", 18));
		button.addActionListener(listener -> {
			a.set(new Person("zhengwei", 19));
		});
	}

	@Data
	@AllArgsConstructor
	private static class Person {
		private String name;
		private int age;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Person person = (Person) o;
			return age == person.age &&
					name.equals(person.name);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, age);
		}
	}
}
