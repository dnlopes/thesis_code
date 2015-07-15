package tests;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import util.thrift.CoordinatorRequest;
import util.thrift.CoordinatorResponse;
import util.thrift.UniqueValue;
import util.zookeeper.EZKOperationCoordination;

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
		ZooKeeper zooKeeper = new ZooKeeper(serverAddresses, 400000, null);

		EZKOperationCoordination coordinationExtenstion = new EZKOperationCoordination(zooKeeper, 1);
		coordinationExtenstion.init(extensionCodeDir);

		CoordinatorRequest request = new CoordinatorRequest();

		//zooKeeper.create("/coordination/uniques/a", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode
		// .PERSISTENT);


		UniqueValue u1 = new UniqueValue("a", "value1");
		UniqueValue u2 = new UniqueValue("a", "value2");
		UniqueValue u3 = new UniqueValue("a", "value3");
		UniqueValue u4 = new UniqueValue("a", "value4");

		request.setUniqueValues(new ArrayList<UniqueValue>());
		//request.addToUniqueValues(u1);
		//request.addToUniqueValues(u2);

		//CoordinatorResponse response = coordinationExtenstion.coordinate(request);


		request = new CoordinatorRequest();
		request.setUniqueValues(new ArrayList<UniqueValue>());
		request.addToUniqueValues(u1);
		request.addToUniqueValues(u4);

		CoordinatorResponse response = coordinationExtenstion.coordinate(request);

		System.exit(1);
		int b = 0;

		System.out.println("Asdasdas");
		//success = coordinationExtenstion.reserveValues("test2");
		//success = coordinationExtenstion.reserveValues("test");


	}

}
