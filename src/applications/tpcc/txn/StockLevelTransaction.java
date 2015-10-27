package applications.tpcc.txn;


import applications.BaseBenchmarkOptions;
import applications.Transaction;
import applications.tpcc.TpccStatements;
import applications.tpcc.metadata.StockLevelMetadata;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import common.util.RuntimeUtils;
import common.util.ExitCode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by dnlopes on 05/09/15.
 */
public class StockLevelTransaction implements Transaction
{

	private static final Logger logger = LoggerFactory.getLogger(NewOrderTransaction.class);

	private final BaseBenchmarkOptions options;
	private final StockLevelMetadata metadata;
	private String lastError;

	public StockLevelTransaction(StockLevelMetadata txnMetadata, BaseBenchmarkOptions options)
	{
		this.metadata = txnMetadata;
		this.options = options;

		if(this.metadata == null)
			RuntimeUtils.throwRunTimeException("failed to generate txn metadata", ExitCode.NOINITIALIZATION);
	}

	@Override
	public boolean executeTransaction(Connection con)
	{
		try
		{
			con.setReadOnly(true);
		} catch(SQLException e)
		{
			lastError = e.getMessage();
			this.rollbackQuietly(con);
			return false;
		}

		TpccStatements statements = TpccStatements.getInstance();
		PreparedStatement ps = null;
		ResultSet rs = null;

		int w_id = this.metadata.getWarehouseId();
		int d_id = this.metadata.getDistrictId();
		int level = this.metadata.getLevel();
		int d_next_o_id = 0;
		int i_count = 0;
		int ol_i_id = 0;

		try
		{
			ps = statements.createPreparedStatement(con, 32);
			ps.setInt(1, d_id);
			ps.setInt(2, w_id);

			if(logger.isTraceEnabled())
				logger.trace("SELECT d_next_o_id FROM district WHERE d_id = " + d_id + " AND d_w_id = " + w_id);

			rs = ps.executeQuery();

			if(rs.next())
			{
				d_next_o_id = rs.getInt(1);
			}

			rs.close();
		} catch(SQLException e)
		{
			lastError = e.getMessage();
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
			this.rollbackQuietly(con);
			return false;
		}

		try
		{
			ps = statements.createPreparedStatement(con, 33);
			ps.setInt(1, w_id);
			ps.setInt(2, d_id);
			ps.setInt(3, d_next_o_id);
			ps.setInt(4, d_next_o_id);

			if(logger.isTraceEnabled())
				logger.trace("SELECT DISTINCT ol_i_id FROM order_line WHERE ol_w_id = " + w_id + " AND ol_d_id =" +
						" " +
						d_id + " AND ol_o_id < " + d_next_o_id +
						" AND ol_o_id >= (" + d_next_o_id + " - 20)");

			rs = ps.executeQuery();

			while(rs.next())
			{
				ol_i_id = rs.getInt(1);
			}

			rs.close();
		} catch(SQLException e)
		{
			lastError = e.getMessage();
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
			this.rollbackQuietly(con);
			return false;
		}

		try
		{
			ps = statements.createPreparedStatement(con, 34);
			ps.setInt(1, w_id);
			ps.setInt(2, ol_i_id);
			ps.setInt(3, level);

			if(logger.isTraceEnabled())
				logger.trace("SELECT count(*) FROM stock WHERE s_w_id = " + w_id + " AND s_i_id = " + ol_i_id + " " +
						"AND" +
						" " +
						"s_quantity < " + level);

			rs = ps.executeQuery();

			if(rs.next())
			{
				i_count = rs.getInt(1);
			}

			rs.close();
		} catch(SQLException e)
		{
			lastError = e.getMessage();
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
			this.rollbackQuietly(con);
			return false;
		}

		// Commit.
		try
		{
			con.commit();
			return true;
		} catch(SQLException e)
		{
			lastError = e.getMessage();
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
			this.rollbackQuietly(con);
			return false;
		}
	}

	@Override
	public String getLastError()
	{
		return this.lastError;
	}

	@Override
	public boolean isReadOnly()
	{
		return true;
	}

	@Override
	public String getName()
	{
		return "StockLevelTransaction";
	}

	private void rollbackQuietly(Connection connection)
	{
		try
		{
			connection.rollback();
		} catch(SQLException ignored)
		{

		}
	}
}
