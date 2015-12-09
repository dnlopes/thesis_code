package client.execution.temporary.scratchpad.agent;


import client.execution.operation.SQLSelect;
import client.execution.operation.SQLWriteOperation;
import client.execution.temporary.scratchpad.ScratchpadException;
import common.database.Record;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


/**
 * Created by dnlopes on 18/09/15.
 * Executor agent responsible for executing SQL operations in a temporary sandbox (in-memory tables)
 * Each agent is responsible for a single database table
 */
public interface IExecutorAgent
{

	int executeTemporaryUpdate(SQLWriteOperation sqlOp) throws SQLException;

	ResultSet executeTemporaryQuery(SQLSelect selectOp) throws SQLException;

	void clearExecutor() throws SQLException;

	void setup(DatabaseMetaData metadata, int scratchpadId) throws ScratchpadException;

	void scanTemporaryTables(List<Record> recordsList) throws SQLException;

}
