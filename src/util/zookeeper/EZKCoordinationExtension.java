package util.zookeeper;


import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.extension.EZKBaseExtension;
import org.apache.zookeeper.server.EZKExtensionGate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.thrift.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;


/**
 * Created by dnlopes on 13/07/15.
 */
public class EZKCoordinationExtension extends EZKBaseExtension
{

	private static final Logger LOG = LoggerFactory.getLogger(EZKCoordinationExtension.class);
	public static final String BASE_DIR = "/coordination";
	public static final String TMP_DIR = BASE_DIR + File.separatorChar + "tmp";
	public static final String UNIQUE_DIR = BASE_DIR + File.separatorChar + "uniques";
	public static final String COUNTERS_DIR = BASE_DIR + File.separatorChar + "counters";
	public static final String OP_PREFIX = BASE_DIR;
	public static final String CLEANUP_OP_CODE = OP_PREFIX + File.separatorChar + "cleanup";

	public EZKCoordinationExtension()
	{
	}

	@Override
	public boolean matchesOperation(int requestType, String path)
	{
		if(requestType != ZooDefs.OpCode.setData && requestType != ZooDefs.OpCode.getData && requestType != ZooDefs
				.OpCode.setACL)
			return false;

		return path.startsWith(OP_PREFIX);
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

		stat = extensionGate.exists(COUNTERS_DIR, false);
		if(stat == null)
			this.extensionGate.create(COUNTERS_DIR, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

		LOG.info("EZKCoordination extension initialized");
	}

	@Override
	protected Stat setData(String path, byte[] data, int version) throws KeeperException
	{
		if(path.compareTo(CLEANUP_OP_CODE) == 0)
		{
			this.cleanup();
			Stat returnStatus = new Stat();
			returnStatus.setVersion(0);
			return returnStatus;
		}
		else if(path.startsWith(OP_PREFIX))
			return this.handleRequest(path, data);
		else
		{
			LOG.warn("unexpected operation code. Calling default _setData_ implementation");
			return this.extensionGate.setData(path, data, version);
		}
	}

	@Override
	protected Stat setACL(String path, List<ACL> acls, int version) throws KeeperException
	{
		if(path.compareTo(CLEANUP_OP_CODE) == 0)
		{
			try
			{
				this.cleanup();
				return EZKExtensionGate.SUCCESS_STAT;
			} catch(KeeperException e)
			{
				LOG.error("failed to cleanup zookeeper database");
				throw e;
			}

		} else
		{
			LOG.warn("unexpected operation code. Calling default _setACL_ implementation");
			return this.extensionGate.setACL(path, acls, version);
		}
	}

	private Stat handleRequest(String path, byte[] data) throws KeeperException
	{
		CoordinatorRequest request = ThriftUtils.decodeCoordinatorRequest(data);

		this.reserveValues(request.getUniqueValues());
		this.prepareRequestedValues(request.getRequests(), path, request);

		Stat returnStatus = new Stat();
		returnStatus.setVersion(0);
		return returnStatus;
	}

	private boolean reserveValues(List<UniqueValue> valuesList) throws KeeperException
	{
		if(valuesList == null || valuesList.size() == 0)
			return true;

		LOG.info("reserving {} values", valuesList.size());

		boolean success = true;
		StringBuilder buffer = new StringBuilder();

		for(UniqueValue uniqueValue : valuesList)
		{
			buffer.setLength(0);
			buffer.append(EZKCoordinationExtension.UNIQUE_DIR + File.separatorChar);
			buffer.append(uniqueValue.getConstraintId());
			buffer.append(File.separatorChar);
			buffer.append(uniqueValue.getValue());
			String nodePath = buffer.toString();

			if(!this.tryReserveValue(nodePath))
				throw new KeeperException.NodeExistsException();
		}

		LOG.info("done!");
		return success;
	}

	private void prepareRequestedValues(List<RequestValue> requestValues, String nodePath, CoordinatorRequest request)
			throws KeeperException
	{
		if(requestValues == null)
			return;

		if(request.getRequestsSize() <= 0)
			return;

		CoordinatorResponse response = new CoordinatorResponse();
		response.setSuccess(true);

		for(RequestValue requestValue : request.getRequests())
		{
			try
			{
				this.prepareValue(requestValue);
			} catch(KeeperException e)
			{
				// if something bad happens, set response success to false and leave
				response.setSuccess(false);
				break;
			}
		}

		byte[] encodedResponse = ThriftUtils.encodeThriftObject(response);
		this.extensionGate.setData(nodePath, encodedResponse, -1);
	}

	private void prepareValue(RequestValue reqValue) throws KeeperException
	{
		String pathNode = COUNTERS_DIR + File.separatorChar + reqValue.getConstraintId();
		int counter = this.incrementAndGet(pathNode);
		reqValue.setRequestedValue(String.valueOf(counter));
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

	private void cleanup() throws KeeperException
	{
		LOG.info("cleaning up database nodes");
		LOG.info("deleting {} directory...", UNIQUE_DIR);
		this.deleteDirectory(UNIQUE_DIR, false);
		LOG.info("deleting {} directory...", COUNTERS_DIR);
		this.deleteDirectory(COUNTERS_DIR, false);
		LOG.info("database cleaned");
	}

	private void deleteDirectory(String path, boolean deleteSelf) throws KeeperException
	{
		List<String> childrens;
		childrens = this.extensionGate.getChildren(path, false, null);

		if(childrens.size() > 0)
		{
			LOG.info("erasing {} nodes from {}", childrens.size(), path);

			for(String children : childrens)
				this.deleteDirectory(path + File.separatorChar + children, true);
		}

		if(deleteSelf)
			this.extensionGate.delete(path, -1);
	}

	private int incrementAndGet(String nodePath) throws KeeperException
	{
		boolean success;
		int newValue, oldValue;

		do
		{
			Stat nodeStat = new Stat();
			oldValue = fromBytes(this.extensionGate.getData(nodePath, false, nodeStat));
			newValue = oldValue + 1;
			success = this.tryIncrement(newValue, nodePath, nodeStat);

		} while(!success);

		return newValue;
	}

	public boolean tryIncrement(int newValue, String nodePath, Stat nodeStat)
	{
		try
		{
			this.extensionGate.setData(nodePath, toBytes(newValue), nodeStat.getVersion());
			return true;
		} catch(KeeperException ignore)
		{
			return false;
		}
	}

	private static byte[] toBytes(int value)
	{
		byte[] bytes = new byte[4];
		ByteBuffer.wrap(bytes).putInt(value);
		return bytes;
	}

	private static int fromBytes(byte[] bytes)
	{
		return ByteBuffer.wrap(bytes).getInt();
	}

}
