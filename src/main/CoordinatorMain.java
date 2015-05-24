package main;


import nodes.coordinator.Coordinator;
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
		if(args.length != 2)
		{
			LOG.error("usage: java -jar <config_file_path> <id>");
			System.exit(ExitCode.WRONG_ARGUMENTS_NUMBER);
		}

		int id = Integer.parseInt(args[1]);
		String configFilePath = args[0];

		System.setProperty("configPath", configFilePath);

		Coordinator coordinator = new Coordinator(Configuration.getInstance().getCoordinatorConfigWithIndex(id));
	}

}
