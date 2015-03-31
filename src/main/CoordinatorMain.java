package main;


import network.coordinator.Coordinator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ExitCode;
import util.defaults.Configuration;


/**
 * Created by dnlopes on 23/03/15.
 */
public class CoordinatorMain
{
	static final Logger LOG = LoggerFactory.getLogger(CoordinatorMain.class);

	public static void main(String args[])
	{
		if(args.length != 1)
		{
			LOG.error("usage: java -jar CoordinatorMain <id>");
			System.exit(ExitCode.WRONG_ARGUMENTS_NUMBER);
		}

		int id = Integer.parseInt(args[0]);
		LOG.info("setup coordinator {}", id);

		Coordinator coordinator = new Coordinator(Configuration.getInstance().getCoordinatorConfigWithIndex(1));
	}

}
