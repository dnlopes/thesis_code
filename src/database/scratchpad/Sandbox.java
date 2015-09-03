package database.scratchpad;


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
 */
public class Sandbox
{

	private static final Logger LOG = LoggerFactory.getLogger(Sandbox.class);

	private final int sandboxId;
	private IDBScratchPad scratchPad;
	private boolean transactionIsRunning;
	private int txnCounter;

	public Sandbox(int sandboxId, Connection dbConnection, CCJSqlParserManager parser)
	{
		this.sandboxId = sandboxId;

		try
		{
			this.scratchPad = new ImprovedScratchPad(this.sandboxId, dbConnection, parser);

		} catch(ScratchpadException | SQLException e)
		{
			LOG.error("failed to create sandbox with id {}", this.sandboxId, e);
			RuntimeUtils.throwRunTimeException(e.getMessage(), ExitCode.SCRATCHPAD_INIT_FAILED);
		}

	}

	public ResultSet executeQuery(String op) throws SQLException
	{
		if(!this.transactionIsRunning)
		{
			this.scratchPad.startTransaction(this.txnCounter++);
			this.transactionIsRunning = true;
		}

		return this.scratchPad.executeQuery(op);
	}

	public int executeUpdate(String op) throws SQLException
	{
		if(!this.transactionIsRunning)
		{
			this.scratchPad.startTransaction(this.txnCounter++);
			this.transactionIsRunning = true;
		}

		return this.scratchPad.executeUpdate(op);
	}

	public Transaction getActiveTransaction()
	{
		return this.scratchPad.getActiveTransaction();
	}

}
