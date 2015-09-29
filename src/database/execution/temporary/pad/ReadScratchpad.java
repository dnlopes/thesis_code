package database.execution.temporary.pad;


import database.execution.SQLInterface;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import org.apache.commons.lang3.StringUtils;

import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by dnlopes on 16/09/15.
 */
public class ReadScratchpad implements ReadOnlyScratchpad
{

	private static final String AND_NOT_DELETED_CLAUSE = " AND _del=0";
	private static final String WHERE_CLAUSE = "_del=0";

	private final CCJSqlParserManager parser;
	private SQLInterface sqlInterface;

	public ReadScratchpad(SQLInterface sqlInterface, CCJSqlParserManager parser) throws SQLException
	{
		this.sqlInterface = sqlInterface;
		this.parser = parser;
	}

	@Override
	public ResultSet executeQuery(String query) throws SQLException
	{

		net.sf.jsqlparser.statement.Statement statement;

		try
		{
			statement = this.parser.parse(new StringReader(query));

		} catch(JSQLParserException e)
		{
			throw new SQLException("parser error: " + e.getMessage());
		}

		if(!(statement instanceof Select))
			throw new SQLException("query statement expected");

		Select selectStatement = (Select) statement;

		SelectBody sb = selectStatement.getSelectBody();

		if(!(sb instanceof PlainSelect))
			throw new SQLException("Cannot process select : " + selectStatement.toString());

		PlainSelect psb = (PlainSelect) sb;
		String queryString = psb.toString();
		Expression where = psb.getWhere();

		if(where == null)
		{
			Expression myWhere = new MyWhereExpression(WHERE_CLAUSE);
			psb.setWhere(myWhere);
			queryString = psb.toString();
		} else
		{
			String defaultWhere = where.toString();
			StringBuilder rebuildWhereClause = new StringBuilder(where.toString());
			rebuildWhereClause.append(AND_NOT_DELETED_CLAUSE);
			String finalWhereClause = rebuildWhereClause.toString();

			queryString = StringUtils.replace(queryString, defaultWhere, finalWhereClause);
		}

		return this.sqlInterface.executeQuery(queryString);
	}

	private class MyWhereExpression implements Expression
	{

		private final String value;

		public MyWhereExpression(String value)
		{
			this.value = value;
		}

		@Override
		public String toString()
		{
			return this.value;
		}

		@Override
		public void accept(ExpressionVisitor expressionVisitor)
		{
		}
	}
}
