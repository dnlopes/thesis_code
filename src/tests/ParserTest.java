package tests;

import util.parser.DDLParser;
import database.util.DatabaseMetadata;
import database.util.DatabaseTable;
import util.defaults.DBDefaults;


/**
 * Created by dnlopes on 06/03/15.
 */
public class ParserTest
{

	public static void main(String args[])
	{
		//System.setProperty(org.slf4j.simpleLogger.defaultLogLevel, "TRACE");

		DDLParser parser = new DDLParser(DBDefaults.TPCW_FILE);
		DatabaseMetadata db = parser.parseAnnotations();
		DatabaseTable t = db.getTable("author");
	}
}
