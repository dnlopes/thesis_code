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
		if(args.length != 4)
		{
			System.err.print("usage: java -jar <jarfile> <topologyFile> <annotationsFile> <environmentFile> " +
					"<id>");
			System.exit(ExitCode.WRONG_ARGUMENTS_NUMBER);
		}

		String topologyFile = args[0];
		String annotationsFile = args[1];
		String environmentFile = args[2];
		int id = Integer.parseInt(args[3]);

		Topology.setupTopology(topologyFile);
		Environment.setupEnvironment(environmentFile, annotationsFile);


		Replicator replicator = new Replicator(Topology.getInstance().getReplicatorConfigWithIndex(id));
	}

}
