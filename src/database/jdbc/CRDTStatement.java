package database.jdbc;

import database.jdbc.util.DBUpdateResult;
import database.occ.scratchpad.IDBScratchpad;
import database.occ.scratchpad.ScratchpadException;
import net.sf.jsqlparser.JSQLParserException;
import runtime.DBSingleOperation;
import runtime.MyShadowOpCreator;
import runtime.ShadowOperation;
import runtime.TransactionInfo;
import util.MissingImplementationException;

import java.sql.*;


/**
 * Created by dnlopes on 04/03/15.
 */
public class CRDTStatement implements Statement
{

	private TransactionInfo txnInfo;
	private IDBScratchpad pad;
	private MyShadowOpCreator shdOpCreator;

	public CRDTStatement(TransactionInfo txnInfo, IDBScratchpad pad, MyShadowOpCreator creator)
	{
		this.txnInfo = txnInfo;
		this.pad = pad;
		this.shdOpCreator = creator;
	}

	@Override
	public ResultSet executeQuery(String arg0) throws SQLException
	{
		if(!this.txnInfo.hasBegun())
			this.txnInfo.beginTxn();

		return pad.executeQuery(arg0);
		//TODO: implement

/*
		if( ! inTx) {
			txId = proxy.beginTxn();
			inTx = true;
		}

		ResultSet res;
		res = proxy.executeOrig( DBSingleOperation.createOperation( arg0), txId);
		return res;
*/
	}

	@Override
	public int executeUpdate(String arg0) throws SQLException
	{
		if(!this.txnInfo.hasBegun())
			this.txnInfo.beginTxn();

		String[] deter = null;
		try
		{
			deter = shdOpCreator.makeToDeterministic(arg0);
		} catch(JSQLParserException e)
		{
			e.printStackTrace();
		}

		int result = 0;

		for(String sql : deter)
		{
			result += pad.executeUpdate(sql);
		}

		for(String updateStr : deter)
		{
			DBUpdateResult res = null;
			try
			{
				res = DBUpdateResult.createResult(
						pad.execute(DBSingleOperation.createOperation(updateStr), this.txnInfo.getTxnId()).getResult());
			} catch(JSQLParserException e)
			{
				e.printStackTrace();
			} catch(ScratchpadException e)
			{
				e.printStackTrace();
			}
			result += res.getUpdateResult();

			if(this.txnInfo.getShadowOp() == null)
			{
				ShadowOperation shdOp = shdOpCreator.createEmptyShadowOperation();
				this.txnInfo.setShadowOp(shdOp);
			}
			try
			{
				shdOpCreator.addDBEntryToShadowOperation(this.txnInfo.getShadowOp(), updateStr);
			} catch(JSQLParserException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;

		//TODO: implement

/*
		if( ! inTx) {
			txId = proxy.beginTxn();
			inTx = true;
		}
		//make it deterministic
		String[] updateStatements = null;
		try {
			updateStatements = shdOpCreator.makeToDeterministic(arg0);
		} catch (JSQLParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int result = 0;
		for(String updateStr : updateStatements) {
			DBUpdateResult res = DBUpdateResult.createResult(proxy.execute( DBSingleOperation.createOperation( updateStr), txId).getResult());
			result += res.getUpdateResult();
			if(shdOp == null) {
				shdOp = shdOpCreator.createEmptyShadowOperation();
			}
			try {
				shdOpCreator.addDBEntryToShadowOperation(shdOp, updateStr);
			} catch (JSQLParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
*/
	}

/*
	NOT IMPLEMENTED METHODS START HERE
*/

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void addBatch(String arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void cancel() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void clearBatch() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void clearWarnings() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void close() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public boolean execute(String arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public boolean execute(String arg0, int arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public boolean execute(String arg0, int[] arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public boolean execute(String arg0, String[] arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int[] executeBatch() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int executeUpdate(String arg0, int arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int executeUpdate(String arg0, int[] arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int executeUpdate(String arg0, String[] arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public Connection getConnection() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int getFetchDirection() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int getFetchSize() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int getMaxFieldSize() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int getMaxRows() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public boolean getMoreResults() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public boolean getMoreResults(int arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int getQueryTimeout() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public ResultSet getResultSet() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int getResultSetConcurrency() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int getResultSetHoldability() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int getResultSetType() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int getUpdateCount() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public SQLWarning getWarnings() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public boolean isClosed() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public boolean isPoolable() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setCursorName(String arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setEscapeProcessing(boolean arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setFetchDirection(int arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setFetchSize(int arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setMaxFieldSize(int arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setMaxRows(int arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setPoolable(boolean arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setQueryTimeout(int arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	public void closeOnCompletion() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	public boolean isCloseOnCompletion() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}
}
