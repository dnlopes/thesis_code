package database.execution.temporary;


import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeUtils;
import runtime.Transaction;
import util.ExitCode;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by dnlopes on 03/09/15.
 * This class represents a temporary environment where transaction execute before being executed in main storage
 */
public class Sandbox
{

	private static final Logger LOG = LoggerFactory.getLogger(Sandbox.class);

	private final int sandboxId;

	private Scratchpad scratchpad;
	private ReadOnlyScratchpad readOnlyScratchpad;

	private boolean readOnlyMode;
	private boolean transactionIsRunning;

	private Transaction transaction;
	private int txnCounter;

	public Sandbox(int sandboxId, Connection dbConnection, CCJSqlParserManager parser)
	{
		this.sandboxId = sandboxId;
		this.readOnlyMode = false;

		try
		{
			this.scratchpad = new ImprovedScratchpad(this.sandboxId, dbConnection, parser);
			this.readOnlyScratchpad = new ThinScratchpad(dbConnection, parser);

		} catch(SQLException e)
		{
			LOG.error("failed to create sandbox with id {}", this.sandboxId, e);
			RuntimeUtils.throwRunTimeException(e.getMessage(), ExitCode.SCRATCHPAD_INIT_FAILED);
		}
	}

	public ResultSet executeQuery(String op) throws SQLException
	{
		if(this.readOnlyMode)
			return this.readOnlyScratchpad.executeQuery(op);

		//its the first op from this txn
		if(!this.transactionIsRunning)
			this.startTransaction();

		return this.scratchpad.executeQuery(op);
	}

	public int executeUpdate(String op) throws SQLException
	{
		if(this.readOnlyMode)
			throw new SQLException("update statement not acceptable under read-only mode");

		//its the first op from this txn
		if(!this.transactionIsRunning)
			this.startTransaction();

		return this.scratchpad.executeUpdate(op);
	}

	public Transaction getTransaction()
	{
		return this.transaction;
	}

	public void endTransaction()
	{
		this.transactionIsRunning = false;
	}

	private void startTransaction() throws SQLException
	{
		this.transaction = new Transaction(this.txnCounter++);
		this.transactionIsRunning = true;
		this.scratchpad.startTransaction(this.transaction);
	}

	public void setReadOnlyMode(boolean readOnlyMode)
	{
		this.readOnlyMode = readOnlyMode;
	}

	public boolean isReadOnlyMode()
	{
		return this.readOnlyMode;
	}
}
