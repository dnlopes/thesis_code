package database.occ;

import database.jdbc.util.DBSelectResult;
import database.jdbc.util.DBUpdateResult;
import database.occ.scratchpad.ScratchpadException;


public interface IDatabase
{
	public DBSelectResult executeQuery( String sql) throws ScratchpadException;
	public DBUpdateResult executeUpdate( String sql) throws ScratchpadException;

}
