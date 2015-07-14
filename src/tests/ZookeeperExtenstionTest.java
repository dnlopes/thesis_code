package tests;

import org.apache.zookeeper.ExtendedZookeeper;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.recipes.coordination.EZKOperationCoordination;
import util.thrift.CoordinatorRequest;
import util.thrift.CoordinatorResponse;
import util.thrift.UniqueValue;

import java.util.ArrayList;


/**
 * Created by dnlopes on 09/07/15.
 */
public class ZookeeperExtenstionTest
{

	public static void main(String[] args) throws Exception
	{

		int a = 0;

		if(args.length < 2)
		{
			System.err.println("Usage: ./ezkSharedValueDemo.sh <server-address(es)>");
			System.exit(1);
		}

		String extensionCodeDir = args[0];
		String serverAddresses = args[1];

		// Create and register counter extension
		ZooKeeper zooKeeper = new ZooKeeper(serverAddresses, 6000000, null);
		EZKOperationCoordination coordinationExtenstion = new EZKOperationCoordination(zooKeeper, 1);
		coordinationExtenstion.init(extensionCodeDir);

		CoordinatorRequest request = new CoordinatorRequest();

		UniqueValue u1 = new UniqueValue("a", "value3");
		UniqueValue u2 = new UniqueValue("a", "value4");
		request.setUniqueValues(new ArrayList<UniqueValue>());
		request.addToUniqueValues(u1);
		request.addToUniqueValues(u2);

		CoordinatorResponse response = coordinationExtenstion.coordinate(request);
		int b = 0;

		System.out.println("Asdasdas");
		//success = coordinationExtenstion.reserveValues("test2");
		//success = coordinationExtenstion.reserveValues("test");


	}

}
