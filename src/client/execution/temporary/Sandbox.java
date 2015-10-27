package client.execution.temporary;


import common.database.SQLBasicInterface;
import common.database.SQLInterface;
import client.execution.temporary.scratchpad.ReadOnlyScratchpad;
import client.execution.temporary.scratchpad.ReadWriteScratchpad;
import client.execution.temporary.scratchpad.AllOperationsScratchpad;
import client.execution.temporary.scratchpad.ReadScratchpad;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import common.util.RuntimeUtils;
import common.util.ExitCode;
import common.thrift.CRDTTransaction;

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
	private SQLInterface sqlInterface;

	private ReadWriteScratchpad scratchpad;
	private ReadOnlyScratchpad readOnlyScratchpad;

	private boolean readMode;
	private boolean transactionIsRunning;

	private CRDTTransaction transaction;
	private int txnCounter;

	public Sandbox(int sandboxId, Connection dbConnection, CCJSqlParserManager parser)
	{
		this.sandboxId = sandboxId;
		this.readMode = false;

		try
		{
			this.sqlInterface = new SQLBasicInterface(dbConnection);
			this.readOnlyScratchpad = new ReadScratchpad(this.sqlInterface, parser);
			this.scratchpad = new AllOperationsScratchpad(this.sandboxId, this.sqlInterface, parser);

		} catch(SQLException e)
		{
			LOG.error("failed to create sandbox with id {}", this.sandboxId, e);
			RuntimeUtils.throwRunTimeException(e.getMessage(), ExitCode.SCRATCHPAD_INIT_FAILED);
		}
	}

	public ResultSet executeQuery(String op) throws SQLException
	{
		if(this.readMode)
			return this.readOnlyScratchpad.executeQuery(op);

		//its the first op from this txn
		if(!this.transactionIsRunning)
			this.startTransaction();

		return this.scratchpad.executeQuery(op);
	}

	public int executeUpdate(String op) throws SQLException
	{
		if(this.readMode)
			throw new SQLException("update statement not acceptable under read-only mode");

		//its the first op from this txn
		if(!this.transactionIsRunning)
			this.startTransaction();

		return this.scratchpad.executeUpdate(op);
	}

	public CRDTTransaction getTransaction()
	{
		return this.transaction;
	}

	public void endTransaction()
	{
		this.transactionIsRunning = false;
	}

	private void startTransaction() throws SQLException
	{
		this.transaction = new CRDTTransaction();
		this.transaction.setId(this.txnCounter++);
		this.scratchpad.startTransaction(this.transaction);
		this.transactionIsRunning = true;
	}

	public void setReadOnlyMode(boolean readOnlyMode)
	{
		this.readMode = readOnlyMode;
	}

	public boolean isReadOnlyMode()
	{
		return this.readMode;
	}
}
