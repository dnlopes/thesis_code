package tests;

import database.parser.DDLParser;
import database.util.Database;
import database.util.DatabaseTable;
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
		Database db = Database.getInstance();
		DatabaseTable t = db.getTable("author");
	}
}
