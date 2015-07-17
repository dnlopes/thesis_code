package tests;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import util.defaults.Configuration;
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

	private final Configuration CONFIG = Configuration.getInstance();
	private static final int SESSION_TIMEOUT = 200000;


	public static void main(String[] args) throws Exception
	{
		int a = 0;

		if(args.length != 1)
		{
			System.err.println("Usage: <configFileLocation>");
			System.exit(1);
		}

		String configFilePath = args[0];
		System.setProperty("configPath", configFilePath);

		ZookeeperExtenstionTest tester = new ZookeeperExtenstionTest();
		tester.testExtension();
	}

	public void testExtension() throws Exception
	{
		ZooKeeper zooKeeper = new ZooKeeper(CONFIG.getZookeeperConnectionString(), SESSION_TIMEOUT, null);

		EZKOperationCoordination coordinationExtenstion = new EZKOperationCoordination(zooKeeper, 1);
		coordinationExtenstion.init(CONFIG.getExtensionCodeDir());
		coordinationExtenstion.cleanupDatabase();

		zooKeeper.create("/coordination/uniques/w_id_warehouse_UNIQUE", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
				CreateMode.PERSISTENT);

		UniqueValue u1 = new UniqueValue("w_id_warehouse_UNIQUE", "value1");
		UniqueValue u2 = new UniqueValue("w_id_warehouse_UNIQUE", "value2");
		UniqueValue u3 = new UniqueValue("w_id_warehouse_UNIQUE", "value3");
		UniqueValue u4 = new UniqueValue("w_id_warehouse_UNIQUE", "value4");

		CoordinatorRequest request = new CoordinatorRequest();
		request.setUniqueValues(new ArrayList<UniqueValue>());

		request = new CoordinatorRequest();

		//request.addToUniqueValues(u1);
		//request.addToUniqueValues(u2);
		request.addToUniqueValues(u4);
		request.addToUniqueValues(u3);

		CoordinatorResponse response = coordinationExtenstion.coordinate(request);

		coordinationExtenstion.closeExtension();
		System.out.println("finished");

	}
}
