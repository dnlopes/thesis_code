package client.execution.temporary.scratchpad;


import client.execution.operation.SQLWriteOperation;
import java.sql.SQLException;


/**
 *
 * Created by dnlopes on 25/09/15.
 * A more complete version of the ReadOnlyScratchpad
 * Besides query statements, it allows for update and insert sql statements
 *
 */
public interface ReadWriteScratchpad extends ReadOnlyInterface
{
	void clearScratchpad() throws SQLException;
	int executeUpdate(SQLWriteOperation op) throws SQLException;
}
