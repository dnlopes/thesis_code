package util.zookeeper;


import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.extension.EZKBaseExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeUtils;
import util.thrift.CoordinatorRequest;
import util.thrift.RequestValue;
import util.thrift.UniqueValue;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created by dnlopes on 13/07/15.
 */
public class EZKCoordinationExtension extends EZKBaseExtension
{

	private static final Logger LOG = LoggerFactory.getLogger(EZKCoordinationExtension.class);
	public static final String BASE_DIR = "/coordination";
	public static final String TMP_DIR = BASE_DIR + File.separatorChar + "tmp";
	public static final String UNIQUE_DIR = BASE_DIR + File.separatorChar + "uniques";

	@Override
	public boolean matchesOperation(int requestType, String path)
	{
		if(requestType != ZooDefs.OpCode.setData && requestType != ZooDefs.OpCode.getData)
			return false;

		return path.startsWith(BASE_DIR);
	}

	@Override
	public void init() throws KeeperException
	{
		Stat stat = extensionGate.exists(BASE_DIR, false);
		if(stat == null)
			this.extensionGate.create(BASE_DIR, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

		stat = extensionGate.exists(TMP_DIR, false);
		if(stat == null)
			this.extensionGate.create(TMP_DIR, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

		stat = extensionGate.exists(UNIQUE_DIR, false);
		if(stat == null)
			this.extensionGate.create(UNIQUE_DIR, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

		LOG.debug("EZKCoordination extension initialized");
	}

	@Override
	protected Stat setData(String path, byte[] data, int version) throws KeeperException
	{
		CoordinatorRequest request = RuntimeUtils.decodeCoordinatorRequest(data);
		Stat returnStatus = new Stat();

		boolean success = this.reserveValues(request.getUniqueValues());

		if(!success)
		{
			returnStatus.setVersion(-1);
			this.extensionGate.delete(path, -1);
			return returnStatus;
		}

		if(request.getRequestsSize() > 0)
			this.prepareRequestedValues(request.getRequests(), path);

		returnStatus.setVersion(0);
		return returnStatus;
	}

	@Override
	protected byte[] getData(String path, boolean watch, Stat stat) throws KeeperException
	{
		return null;
	}

	private boolean reserveValues(List<UniqueValue> valuesList)
	{
		if(valuesList == null || valuesList.size() == 0)
			return true;

		boolean success = true;
		Set<String> addedNodes = new HashSet<String>();
		StringBuilder buffer = new StringBuilder(EZKCoordinationExtension.UNIQUE_DIR + File.separatorChar);

		for(UniqueValue uniqueValue : valuesList)
		{
			buffer.append(uniqueValue.getConstraintId());
			buffer.append(File.separatorChar);
			buffer.append(uniqueValue.getValue());
			String nodePath = buffer.toString();

			if(this.tryReserveValue(nodePath))
				addedNodes.add(nodePath);
			else
			{
				success = false;
				break;
			}
		}

		if(!success)
			this.removeNodes(addedNodes);

		return success;
	}

	private void prepareRequestedValues(List<RequestValue> requestValues, String nodePath)
	{
		//TODO prepare CoordinatorResponse here and set the nodePath with the byte array corresponding to the
		// CoordinatorRespoonse object
	}

	private boolean tryReserveValue(String node)
	{
		// try to create node
		try
		{
			extensionGate.create(node, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			return true;
		} catch(KeeperException e)
		{
			LOG.warn(e.getMessage());
			return false;
		}
	}

	private void removeNodes(Set<String> addedNodes)
	{
		for(String node : addedNodes)
		{
			try
			{
				extensionGate.delete(node, -1);
			} catch(KeeperException e)
			{
				LOG.warn(e.getMessage(), e);
			}
		}
	}

}
