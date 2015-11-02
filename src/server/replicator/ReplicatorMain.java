package server.replicator;


import common.util.Environment;
import common.util.ExitCode;
import common.util.Topology;
import common.util.exception.ConfigurationLoadException;


/**
 * Created by dnlopes on 23/03/15.
 */
public class ReplicatorMain
{

	public static void main(String args[]) throws ConfigurationLoadException
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

		Replicator replicator = new Replicator(Topology.getInstance().getReplicatorConfigWithIndex(id));
	}

}
