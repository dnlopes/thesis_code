package client.execution.temporary.scratchpad;


import client.execution.operation.SQLWriteOperation;
import client.execution.temporary.ReadOnlyInterface;
import client.execution.temporary.WriteSet;
import common.database.Record;

import java.sql.SQLException;
import java.util.List;


/**
 *
 * Created by dnlopes on 25/09/15.
 * A more complete version of the ReadOnlyScratchpad
 * Besides query statements, it allows for update and insert sql statements
 *
 */
public interface IDBScratchpad extends ReadOnlyInterface
{
	void clearScratchpad() throws SQLException;
	int executeUpdate(SQLWriteOperation op) throws SQLException;
	WriteSet getWriteSet();
	List<Record> getScratchpadSnapshot() throws SQLException;
}
