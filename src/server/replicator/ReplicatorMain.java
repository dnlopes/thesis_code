package server.replicator;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import common.util.ExitCode;
import common.Configuration;


/**
 * Created by dnlopes on 23/03/15.
 */
public class ReplicatorMain
{
	static final Logger LOG = LoggerFactory.getLogger(ReplicatorMain.class);

	public static void main(String args[])
	{
		if(args.length != 4)
		{
			LOG.error("usage: java -jar <jarfile> <topologyFile> <annotationsFile> <environmentFile> <id>");
			System.exit(ExitCode.WRONG_ARGUMENTS_NUMBER);
		}

		String topologyFile = args[0];
		String annotationsFile = args[1];
		String environmentFile = args[2];
		int id = Integer.parseInt(args[3]);

		Configuration.setupConfiguration(topologyFile, annotationsFile, environmentFile);

		Replicator replicator = new Replicator(Configuration.getInstance().getReplicatorConfigWithIndex(id));
	}

}
