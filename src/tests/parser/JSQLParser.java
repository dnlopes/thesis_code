package tests.parser;


import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.io.StringReader;


/**
 * Created by dnlopes on 31/03/15.
 */
public class JSQLParser
{

	public static void main(String args[])
	{
		CCJSqlParserManager parser = new CCJSqlParserManager();

		String cenas = "SELECT d_next_o_id, d_tax FROM district WHERE d_id = 1 AND d_w_id = 1 FOR UPDATE";

		try
		{
			Statement stat = parser.parse(new StringReader(cenas));
		} catch(JSQLParserException e)
		{
			e.printStackTrace();
		}

	}
	
}
