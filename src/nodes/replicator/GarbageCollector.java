package nodes.replicator;


import database.jdbc.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.defaults.ReplicatorDefaults;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * Created by dnlopes on 10/09/15.
 */
public class GarbageCollector implements Runnable
{

	private static final Logger LOG = LoggerFactory.getLogger(GarbageCollector.class);

	private Connection connection;

	public GarbageCollector(Replicator replicator)
	{
		try
		{
			this.connection = ConnectionFactory.getDefaultConnection(replicator.getConfig());
		} catch(SQLException e)
		{
			if(LOG.isWarnEnabled())
				LOG.warn("failed to create connection for GarbageCollector agent", e);
		}

		if(LOG.isInfoEnabled())
			LOG.info("garbage collection agent initialized with schedule interval of {}",
					ReplicatorDefaults.GARBAGE_COLLECTOR_THREAD_INTERVAL);

	}

	@Override
	public void run()
	{
		//TODO: implement
	}
}
