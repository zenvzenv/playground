package zhengwei.netty.thrift;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 * @author zhengwei AKA Awei
 * @since 2019/9/4 20:58
 */
public class ThriftPersonClient {
	public static void main(String[] args) {
		TTransport transport = new TFramedTransport(new TSocket("localhost", 8888), 600);
		TProtocol protocol = new TCompactProtocol(transport);
		PersonService.Client client = new PersonService.Client(protocol);
		try {
			transport.open();
			Person person = client.getPersonByName("zhengwei");
			System.out.println(person);
			System.out.println("------------");
			Person person1 = new Person();
			person.setName("zhengwei1");
			person.setAge(20);
			person.setMarried(true);
			client.savePerson(person1);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			transport.close();
		}
	}
}
