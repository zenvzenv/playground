package zhengwei.netty.thrift;

import org.apache.thrift.TException;

/**
 * @author zhengwei AKA Awei
 * @since 2019/9/4 20:43
 */
public class ThriftPersonServiceImpl implements PersonService.Iface {
	@Override
	public Person getPersonByName(String name) throws DataException, TException {
		System.out.println("Got Client Param:"+name);
		Person person=new Person();
		person.setName("zhengwei");
		person.setAge(25);
		person.setMarried(false);
		return person;
	}

	@Override
	public void savePerson(Person person) throws DataException, TException {
		System.out.println(person);
	}
}
