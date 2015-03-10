package database.scratchpad;

import database.jdbc.ConnectionFactory;
import util.Defaults;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by dnlopes on 10/03/15.
 */
public class DBExecuteScratchpad implements ExecuteScratchpad
{

	private boolean readOnly;
	private int id;
	private Connection conn;

	public DBExecuteScratchpad(int id) throws SQLException
	{
		this.id = id;
		this.readOnly = false;
		this.conn = ConnectionFactory.getInstance().getDefaultConnection(Defaults.TPCW_DB_NAME);

	}

	@Override
	public boolean isReadOnly()
	{
		return this.readOnly;
	}

	@Override
	public ResultSet executeQuery(String op) throws SQLException
	{
		//TODO
		return null;
	}

	@Override
	public int executeUpdate(String op) throws SQLException
	{
		//TODO
		return 0;
	}

	@Override
	public void addToBatchUpdate(String op) throws SQLException
	{
		//TODO
	}

	@Override
	public void executeBatch() throws SQLException
	{
		//TODO
	}

	@Override
	public void cleanState()
	{

	}
}
