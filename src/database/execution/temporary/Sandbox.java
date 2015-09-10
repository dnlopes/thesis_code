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
	private boolean transactionIsRunning;
	private int txnCounter;

	public Sandbox(int sandboxId, Connection dbConnection, CCJSqlParserManager parser)
	{
		this.sandboxId = sandboxId;

		try
		{
			this.scratchpad = new ImprovedScratchpad(this.sandboxId, dbConnection, parser);

		} catch(SQLException e)
		{
			LOG.error("failed to create sandbox with id {}", this.sandboxId, e);
			RuntimeUtils.throwRunTimeException(e.getMessage(), ExitCode.SCRATCHPAD_INIT_FAILED);
		}
	}

	public ResultSet executeQuery(String op) throws SQLException
	{
		if(!this.transactionIsRunning)
		{
			this.scratchpad.startTransaction(this.txnCounter++);
			this.transactionIsRunning = true;
		}

		return this.scratchpad.executeQuery(op);
	}

	public int executeUpdate(String op) throws SQLException
	{
		if(!this.transactionIsRunning)
		{
			this.scratchpad.startTransaction(this.txnCounter++);
			this.transactionIsRunning = true;
		}

		return this.scratchpad.executeUpdate(op);
	}

	public Transaction getActiveTransaction()
	{
		return this.scratchpad.getActiveTransaction();
	}

}
