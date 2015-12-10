package server.replicator;


import common.nodes.NodeConfig;
import common.util.Environment;
import common.util.ExitCode;
import common.util.Topology;
import common.util.exception.ConfigurationLoadException;
import common.util.exception.InitComponentFailureException;
import common.util.exception.InvalidConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by dnlopes on 23/03/15.
 */
public class ReplicatorMain
{

	private static final Logger LOG = LoggerFactory.getLogger(ReplicatorMain.class);

	public static void main(String args[])
			throws ConfigurationLoadException, InitComponentFailureException, InvalidConfigurationException
	{
		if(args.length != 3)
		{
			System.err.print("usage: java -jar <jarfile> <topologyFile> <environmentFile> " +
					"<id>");
			System.exit(ExitCode.WRONG_ARGUMENTS_NUMBER);
		}

		String topologyFile = args[0];
		String environmentFile = args[1];
		int id = Integer.parseInt(args[2]);

		Topology.setupTopology(topologyFile);
		Environment.setupEnvironment(environmentFile);

		NodeConfig config = Topology.getInstance().getReplicatorConfigWithIndex(id);

		LOG.info("starting replicator {}", config.getId());
		Replicator replicator = new Replicator(config);
	}

}
