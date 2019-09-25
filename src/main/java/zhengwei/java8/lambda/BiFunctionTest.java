package zhengwei.java8.lambda;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author zhengwei AKA Awei
 * @since 2019/9/24 12:41
 */
public class BiFunctionTest {
	@Data
	@AllArgsConstructor
	private static class Person {
		private String name;
		private int age;
	}

	@Test
	void testBiFunction() {
		List<Person> persons = Arrays.asList(
				new Person("zhengwei1", 18),
				new Person("zhengwei2", 19),
				new Person("zhengwei3", 20)
		);

		System.out.println(getPersonsByAge(18, persons));
		System.out.println(getPersonsByAge(18, persons, (age, personList) -> personList.stream().filter(person -> person.getAge() > age).collect(Collectors.toList())));
		System.out.println(getPersonsByAge(18, persons, (age, personList) -> personList.stream().filter(person -> person.getAge() <= age).collect(Collectors.toList())));
	}

	List<Person> getPersonsByAge(int age, List<Person> persons) {
		BiFunction<Integer, List<Person>, List<Person>> biFunction = (ageOfPerson, personList) -> personList.stream().filter(person -> person.getAge() > age).collect(Collectors.toList());
		return biFunction.apply(age, persons);
	}
	//这种方式更灵活
	List<Person> getPersonsByAge(int age, List<Person> persons, BiFunction<Integer, List<Person>, List<Person>> biFunction) {
		return biFunction.apply(age, persons);
	}
}
