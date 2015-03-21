package tests;

import net.sf.appia.core.*;

import network.node.Proxy;
import network.node.Replicator;
import org.apache.thrift.TException;
import util.thrift.ThriftOperation;


/**
 * Created by dnlopes on 15/03/15.
 */
public class NetworkTest
{

	public static void main(String args[])
			throws AppiaInvalidQoSException, AppiaCursorException, AppiaEventException, AppiaDuplicatedSessionsException, TException
	{

		int davidPort = 45444;


		Replicator replicator = new Replicator("localhost", davidPort, 2);

		Proxy proxy = new Proxy("localhost", 45000,1, replicator);

		ThriftOperation op = new ThriftOperation();
		op.addToOperations("TESTE");
		proxy.TESTCOMMIT();
	}

}
