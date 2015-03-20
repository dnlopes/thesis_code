package database.util;


import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.commons.lang3.StringUtils;


/**
 * Created by dnlopes on 20/03/15.
 */
public class DBUtils
{

	public static String replaceWhereClauseInSelect(Select select, String newWhereClause)
	{

		PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
		String newSelectQuery = plainSelect.toString();
		String defaultWhere = plainSelect.getWhere().toString();

		return StringUtils.replace(newSelectQuery, defaultWhere, newWhereClause);
	}
	
}
