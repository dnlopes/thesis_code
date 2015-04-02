package runtime.operation;


import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.update.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DBSingleOperation
{

	private static final Logger LOG = LoggerFactory.getLogger(DBSingleOperation.class);

	private Statement opStatement;
	private boolean isQuery;
	transient public String sql;
	transient String[][] table;

	public DBSingleOperation(String sql)
	{
		this.sql = sql;
	}

	public Statement getStatement()
	{
		return this.opStatement;
	}

	/*
	 * table[][0] - nome da tabela
	 * table[][1] - nome do alias
	 * table[][2] - nome da tabela uppercase
	 */
	public String[][] targetTable()
	{
		if(table != null)
			return table;
		if(opStatement == null)
			throw new RuntimeException("Not parsed yet - unexpected situation");
		if(opStatement instanceof Insert)
		{
			table = new String[][]{{((Insert) opStatement).getTable().getName(), ((Insert) opStatement).getTable()
					.getName(), ((Insert) opStatement).getTable().getName().toUpperCase()}};
		} else if(opStatement instanceof Update)
		{
			table = new String[][]{{((Update) opStatement).getTables().get(
					0).getName(), ((Update) opStatement).getTables().get(
					0).getName(), ((Update) opStatement).getTables().get(0).getName().toUpperCase()}};
		} else if(opStatement instanceof Delete)
		{
			table = new String[][]{{((Delete) opStatement).getTable().getName(), ((Delete) opStatement).getTable()
					.getName(), ((Delete) opStatement).getTable().getName().toUpperCase()}};
		} else if(opStatement instanceof Select)
		{
			SelectBody sb = ((Select) opStatement).getSelectBody();
			if(!(sb instanceof PlainSelect))
				throw new RuntimeException("Cannot process select : " + opStatement);
			PlainSelect psb = (PlainSelect) sb;
			FromItem fi = psb.getFromItem();
			if(!(fi instanceof Table))
				throw new RuntimeException("Cannot process select : " + opStatement);
			List joins = psb.getJoins();
			int nJoins = joins == null ? 0 : joins.size();
			table = new String[nJoins + 1][3];
			table[0][0] = ((Table) fi).getName();
			//table[0][1] = (fi.getAlias() == null || fi.getAlias().length() == 0) ? ((Table) fi).getName() : fi
			//		.getAlias();
			table[0][2] = ((Table) fi).getName().toUpperCase();
			if(joins != null)
			{
				Iterator it = joins.iterator();
				int i = 1;
				while(it.hasNext())
				{
					Join jT = (Join) it.next();
					table[i][0] = ((Table) jT.getRightItem()).getName();
					//table[i][1] = (((Table) jT.getRightItem()).getAlias() == null || ((Table) jT.getRightItem())
					//		.getAlias().length() == 0) ? ((Table) jT.getRightItem()).getName() : ((Table) jT
					//		.getRightItem()).getAlias();
					table[i][2] = ((Table) jT.getRightItem()).getName().toUpperCase();
					i++;
				}
			}
		} else
			throw new RuntimeException("Cannot process operation : " + opStatement);
		return table;
	}

	public boolean isQuery()
	{
		return this.isQuery;
	}

	public void parse(CCJSqlParserManager parser)
	{
		try
		{
			this.opStatement = parser.parse(new StringReader(this.sql));
			this.isQuery = this.opStatement instanceof Select;
		} catch(JSQLParserException e)
		{
			LOG.warn("failed to parse sql string {}", sql);
			e.printStackTrace();
		}
	}
}
