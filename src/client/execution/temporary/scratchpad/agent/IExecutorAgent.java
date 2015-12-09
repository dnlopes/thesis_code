package client.execution.temporary.scratchpad.agent;


import client.execution.operation.SQLSelect;
import client.execution.operation.SQLWriteOperation;
import client.execution.temporary.scratchpad.ScratchpadException;
import common.database.Record;
import net.sf.jsqlparser.statement.select.Select;
import common.thrift.CRDTTransaction;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * Created by dnlopes on 18/09/15.
 * Executor agent responsible for executing SQL operations in a temporary sandbox (in-memory tables)
 * Each agent is responsible for a single database table
 */
public interface IExecutorAgent
{

	int executeTemporaryUpdate(SQLWriteOperation sqlOp)
			throws SQLException;
	ResultSet executeTemporaryQuery(SQLSelect selectOp) throws SQLException;
	void clearExecutor() throws SQLException;
	void setup(DatabaseMetaData metadata, int scratchpadId) throws ScratchpadException;
	void scanTemporaryTables(List<Record> recordsList) throws SQLException;

	interface Defaults
	{

		String DEFAULT_DATE_VALUE = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

	}
}
