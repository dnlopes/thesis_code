package tests.parser;


import common.database.util.DatabaseMetadata;
import common.util.Environment;


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
		DatabaseMetadata metadata = Environment.DB_METADATA;

		int a = 0;
	}

}
