package database.execution.temporary;


import net.sf.jsqlparser.statement.select.Select;
import util.thrift.CRDTOperation;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by dnlopes on 18/09/15.
 * Executor agent responsible for executing SQL operations in a temporary sandbox (in-memory tables)
 * Each agent is responsible for a single database table
 */
public interface IExecutorAgent
{
	public int executeTemporaryUpdate(net.sf.jsqlparser.statement.Statement statement, CRDTOperation crdtOperation)
			throws SQLException;

	public ResultSet executeTemporaryQuery(Select selectOp) throws SQLException;

	public void resetExecuter() throws SQLException;

	public void setup(DatabaseMetaData metadata, int scratchpadId);

	public interface Defaults
	{

		public static final String DEFAULT_DATE_VALUE = new SimpleDateFormat("yyyy-MM-dd").format(
				new Date());

	}
}
