package nodes.replicator;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dnlopes on 19/06/15.
 */
public class GarbageCollector implements Runnable
{

	private static final Logger LOG = LoggerFactory.getLogger(Replicator.class);
	private IReplicatorNetwork  network;

	public GarbageCollector(IReplicatorNetwork network)
	{
		this.network = network;
	}

	@Override
	public void run()
	{
		boolean hasDelivered = false;

		do
		{
			//TODO
		} while(hasDelivered);
	}
}
