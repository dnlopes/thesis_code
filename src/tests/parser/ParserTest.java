package tests.parser;


import util.defaults.Configuration;
import database.util.DatabaseMetadata;

/**
 * Created by dnlopes on 06/03/15.
 */
public class ParserTest
{

	private static final String SCHEMA_FILE =
			"/Users/dnlopes/workspaces/thesis/code/weakdb/resources/configs/tpcc_localhost_1node.xml";

	public static void main(String args[])
	{
		System.setProperty("configPath", SCHEMA_FILE);
		DatabaseMetadata metadata = Configuration.getInstance().getDatabaseMetadata();

		int a = 0;
	}

}
