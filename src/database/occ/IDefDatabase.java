package database.occ;

import database.occ.scratchpad.ScratchpadException;

import java.sql.ResultSet;
import java.sql.SQLException;


public interface IDefDatabase
{
	public ResultSet executeQuery( String sql) throws SQLException;
	public int executeUpdate( String sql) throws SQLException, ScratchpadException;
	public int executeOp( String sql) throws SQLException, ScratchpadException;
	public void addCleanUpToBatch(String sql) throws SQLException;

}
