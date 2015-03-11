package tests;

import database.parser.DDLParser;
import util.defaults.DBDefaults;


/**
 * Created by dnlopes on 06/03/15.
 */
public class ParserTest
{


	public static void main(String args[])
	{
		DDLParser parser = new DDLParser(DBDefaults.TPCW_FILE);
		parser.parseAnnotations();
	}
}
