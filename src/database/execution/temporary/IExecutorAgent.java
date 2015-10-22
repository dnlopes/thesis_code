package database.execution.temporary;


import net.sf.jsqlparser.statement.select.Select;
import util.thrift.CRDTTransaction;

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
	int executeTemporaryUpdate(net.sf.jsqlparser.statement.Statement statement, CRDTTransaction transaction)
			throws SQLException;

	ResultSet executeTemporaryQuery(Select selectOp) throws SQLException;

	void resetExecuter() throws SQLException;

	void setup(DatabaseMetaData metadata, int scratchpadId);

	interface Defaults
	{

		String DEFAULT_DATE_VALUE = new SimpleDateFormat("yyyy-MM-dd").format(
				new Date());

	}
}
