package util.zookeeper;


import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.extension.EZKExtensionRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeUtils;
import util.thrift.CoordinatorRequest;
import util.thrift.CoordinatorResponse;

import java.io.File;
import java.io.IOException;


/**
 * Created by dnlopes on 13/07/15.
 */
public class EZKOperationCoordination implements OperationCoordinationService
{
	private static final int SESSION_TIMEOUT = 2000;
	private static final Logger LOG = LoggerFactory.getLogger(EZKOperationCoordination.class);
	public static final String BASE_DIR = "/coordination";
	private final String PRIVATE_TMP_NODE;
	private final int id;

	protected final ZooKeeper zooKeeper;

	public EZKOperationCoordination(String serverAddress, int id) throws IOException
	{
		this.id = id;
		this.PRIVATE_TMP_NODE = BASE_DIR + File.separatorChar + "tmp" + File.separatorChar + this.id;

		this.zooKeeper = new ZooKeeper(serverAddress, SESSION_TIMEOUT, null);
	}

	public void init(String codeBasePath) throws Exception
	{
		EZKExtensionRegistration.registerExtension(zooKeeper, EZKCoordinationExtension.class, codeBasePath);
		// create private tmp node to hold generic byte array for responses
		zooKeeper.create(PRIVATE_TMP_NODE, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

		LOG.info("Extension client successfully binded to Zookeeper service");
	}

	public CoordinatorResponse coordinate(CoordinatorRequest request)
	{
		boolean singleRpc = !request.isSetRequests() || request.getRequestsSize() == 0;

		if(singleRpc)
			return this.singleRpcCoordination(request);
		else
			return this.twoRpcCoordination(request);

	}

	private CoordinatorResponse singleRpcCoordination(CoordinatorRequest request)
	{
		byte[] bytesRequest = RuntimeUtils.encodeThriftObject(request);
		CoordinatorResponse response = new CoordinatorResponse();
		response.setSuccess(false);

		try
		{
			// sync call to reserve unique values
			Stat stat = this.zooKeeper.setData(PRIVATE_TMP_NODE, bytesRequest, -1);

			if(stat.getVersion() == 0)
				response.setSuccess(true);

		} catch(KeeperException e)
		{
		} catch(InterruptedException e)
		{
		}

		return response;
	}

	private CoordinatorResponse twoRpcCoordination(CoordinatorRequest request)
	{
		byte[] bytesRequest = RuntimeUtils.encodeThriftObject(request);
		CoordinatorResponse response = new CoordinatorResponse();
		response.setSuccess(false);

		// async call to reserve unique values
		this.zooKeeper.setData(PRIVATE_TMP_NODE, bytesRequest, -1, null, null);
		// then, we call getData to get the requested values
		this.getRequestedValues(response);

		return response;
	}

	private void getRequestedValues(CoordinatorResponse response)
	{
		try
		{
			byte[] responseByteArray = this.zooKeeper.getData(PRIVATE_TMP_NODE, false, null);
			CoordinatorResponse tmpResponse = RuntimeUtils.decodeCoordinatorResponse(responseByteArray);
			response.setSuccess(tmpResponse.isSuccess());
			response.setRequestedValues(tmpResponse.getRequestedValues());
		} catch(KeeperException e)
		{
			response.setSuccess(false);
		} catch(InterruptedException e)
		{
			response.setSuccess(false);
		}
	}

}
