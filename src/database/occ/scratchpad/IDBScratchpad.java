package database.occ.scratchpad;


import database.jdbc.Result;
import database.jdbc.util.DBReadSetEntry;
import database.jdbc.util.DBWriteSetEntry;
import net.sf.jsqlparser.JSQLParserException;
import runtime.operation.DBSingleOperation;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by dnlopes on 10/03/15.
 */
public interface IDBScratchpad
{

	public int getScratchpadId();

	public boolean isReadOnly();

	public Result executeUpdate(DBSingleOperation op) throws JSQLParserException, ScratchpadException, SQLException;

	public ResultSet executeQuery(DBSingleOperation op) throws JSQLParserException, ScratchpadException, SQLException;

	public void addToBatchUpdate(String op) throws SQLException;

	public int executeBatch() throws SQLException;

	public void abort() throws SQLException;

	boolean addToWriteSet(DBWriteSetEntry entry);

	boolean addToReadSet(DBReadSetEntry readSetEntry);

	public ResultSet executeQuery(String op) throws SQLException;

	public int executeUpdate(String op) throws SQLException;

}
