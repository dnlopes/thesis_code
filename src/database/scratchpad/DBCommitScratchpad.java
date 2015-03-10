package database.scratchpad;

import database.jdbc.ConnectionFactory;
import runtime.Operation;
import util.Defaults;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * Created by dnlopes on 10/03/15.
 */
public class DBCommitScratchpad implements CommitScratchpad
{

	private int id;
	private Connection conn;
	private Operation op;

	public DBCommitScratchpad(int id) throws SQLException
	{
		this.id = id;
		this.conn = ConnectionFactory.getInstance().getDefaultConnection(Defaults.TPCW_DB_NAME);
	}

	@Override
	public Operation getOperation()
	{
		return this.op;
	}

	@Override
	public void setOperation(Operation op)
	{
		this.op = op;
	}

	@Override
	public void commit()
	{
	}
}
