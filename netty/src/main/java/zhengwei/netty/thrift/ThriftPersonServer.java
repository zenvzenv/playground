package zhengwei.netty.thrift;


import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;

/**
 * @author zhengwei AKA Awei
 * @since 2019/9/4 20:52
 */
public class ThriftPersonServer {
	public static void main(String[] args) throws IOException, TTransportException {
		TNonblockingServerSocket socket = new TNonblockingServerSocket(8888);
		THsHaServer.Args arg = new THsHaServer.Args(socket).minWorkerThreads(2).maxWorkerThreads(4);
		PersonService.Processor<ThriftPersonServiceImpl> processor = new PersonService.Processor<>(new ThriftPersonServiceImpl());
		//协议层
		arg.protocolFactory(new TCompactProtocol.Factory());
		//传输层
		arg.transportFactory(new TFramedTransport.Factory());
		arg.processorFactory(new TProcessorFactory(processor));

		TServer server = new THsHaServer(arg);
		System.out.println("Thrift Server Started");
		server.serve();
	}
}
