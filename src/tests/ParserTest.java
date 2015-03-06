package tests;

import database.parser.DDLParser;


/**
 * Created by dnlopes on 06/03/15.
 */
public class ParserTest
{

	private static final String TPCW_FILE = "/Users/dnlopes/devel/thesis/code/framework/application/tpcw.sql";

	public static void main(String args[])
	{
		DDLParser parser = new DDLParser(TPCW_FILE);
		parser.parseAnnotations();
	}
}
