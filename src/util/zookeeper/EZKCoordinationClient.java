package util.zookeeper;


import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.extension.EZKExtensionRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeUtils;
import util.defaults.ZookeeperDefaults;
import util.thrift.CoordinatorRequest;
import util.thrift.CoordinatorResponse;
import util.thrift.ThriftUtils;

import java.io.File;


/**
 * Created by dnlopes on 13/07/15.
 */
public class EZKCoordinationClient implements EZKCoordinationService
{

	private static final Logger LOG = LoggerFactory.getLogger(EZKCoordinationClient.class);

	private final String privateNode;
	private final int id;
	private final ZooKeeper zooKeeper;

	public EZKCoordinationClient(ZooKeeper zooKeeper, int id)
	{
		this.id = id;
		this.privateNode = ZookeeperDefaults.ZOOKEEPER_BASE_NODE + File.separatorChar + "tmp" + File
				.separatorChar + this.id;
		this.zooKeeper = zooKeeper;
	}

	@Override
	public void init(String codeBasePath) throws KeeperException, InterruptedException
	{
		EZKExtensionRegistration.registerExtension(this.zooKeeper, EZKCoordinationExtension.class, codeBasePath);

		// create private tmp node to hold generic byte array for responses
		Stat stat = this.zooKeeper.exists(this.privateNode, false);
		if(stat == null)
			this.zooKeeper.create(this.privateNode, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

		if(LOG.isInfoEnabled())
			LOG.info("Coordination extension successfully installed at Zookeeper");
	}

	@Override
	public CoordinatorResponse coordinate(CoordinatorRequest request)
	{
		boolean singleRpc = !request.isSetRequests() || request.getRequestsSize() == 0;

		if(singleRpc)
			return this.singleRpcCoordination(request);
		else
			return this.twoRpcCoordination(request);

	}

	@Override
	public void cleanupDatabase() throws KeeperException, InterruptedException
	{
		//this.zooKeeper.setACL(EZKCoordinationExtension.CLEANUP_OP_CODE, ZooDefs.Ids.OPEN_ACL_UNSAFE, -1);
		this.zooKeeper.setData(EZKCoordinationExtension.CLEANUP_OP_CODE, new byte[0], -1);
	}

	@Override
	public void closeExtension() throws InterruptedException
	{
		this.zooKeeper.close();
	}

	private CoordinatorResponse singleRpcCoordination(CoordinatorRequest request)
	{
		byte[] bytesRequest = ThriftUtils.encodeThriftObject(request);
		CoordinatorResponse response = new CoordinatorResponse();
		response.setSuccess(false);

		try
		{
			// sync call to reserve unique values
			Stat stat = this.zooKeeper.setData(this.privateNode, bytesRequest, -1);

			if(stat.getVersion() == 0)
				response.setSuccess(true);

		} catch(KeeperException | InterruptedException e)
		{
			if(LOG.isWarnEnabled())
				LOG.warn(e.getMessage());

			response.setSuccess(false);
		}

		return response;
	}

	private CoordinatorResponse twoRpcCoordination(CoordinatorRequest request)
	{
		byte[] bytesRequest = ThriftUtils.encodeThriftObject(request);
		CoordinatorResponse response = new CoordinatorResponse();
		response.setSuccess(false);

		// async call to reserve unique values
		this.zooKeeper.setData(this.privateNode, bytesRequest, -1, null, null);
		// then, call getData to get the requested values
		this.getRequestedValues(response);

		return response;
	}

	private void getRequestedValues(CoordinatorResponse response)
	{
		try
		{
			byte[] responseByteArray = this.zooKeeper.getData(privateNode, false, null);
			CoordinatorResponse tmpResponse = RuntimeUtils.decodeCoordinatorResponse(responseByteArray);
			response.setSuccess(tmpResponse.isSuccess());
			response.setRequestedValues(tmpResponse.getRequestedValues());
		} catch(KeeperException | InterruptedException e)
		{
			response.setSuccess(false);
		}
	}

}
