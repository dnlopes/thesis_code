package nodes.replicator;


import database.jdbc.ConnectionFactory;
import database.util.DatabaseMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.LogicalClock;
import util.Configuration;
import util.defaults.ReplicatorDefaults;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created by dnlopes on 10/09/15.
 */
public class GarbageCollector implements Runnable
{

	private static final Logger LOG = LoggerFactory.getLogger(GarbageCollector.class);

	private Connection connection;
	private Replicator replicator;
	private DatabaseMetadata metadata;
	private Statement statement;

	public GarbageCollector(Replicator replicator)
	{
		this.replicator = replicator;
		this.metadata = Configuration.getInstance().getDatabaseMetadata();

		try
		{
			this.connection = ConnectionFactory.getDefaultConnection(replicator.getConfig());
			this.statement = this.connection.createStatement();

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
		LogicalClock clockSnapshot = this.replicator.getCurrentClock().duplicate();

		//TODO: implement


	}
}
