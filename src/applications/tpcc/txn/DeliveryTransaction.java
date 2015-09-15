package applications.tpcc.txn;


import applications.BenchmarkOptions;
import applications.Transaction;
import applications.tpcc.TpccConstants;
import applications.tpcc.TpccStatements;
import applications.tpcc.metadata.DeliveryMetadata;
import applications.tpcc.metadata.NewOrderMetadata;
import applications.tpcc.metadata.OrderStatMetadata;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeUtils;
import util.ExitCode;

import java.sql.*;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by dnlopes on 05/09/15.
 */
public class DeliveryTransaction implements Transaction
{

	private static final Logger logger = LoggerFactory.getLogger(NewOrderTransaction.class);

	private final BenchmarkOptions options;
	private final DeliveryMetadata metadata;
	private String lastError;

	public DeliveryTransaction(DeliveryMetadata txnMetadata, BenchmarkOptions options)
	{
		this.metadata = txnMetadata;
		this.options = options;

		if(this.metadata == null)
			RuntimeUtils.throwRunTimeException("failed to generate txn metadata", ExitCode.NOINITIALIZATION);
	}

	@Override
	public boolean executeTransaction(Connection con)
	{
		TpccStatements statements = TpccStatements.getInstance();
		PreparedStatement ps = null;
		ResultSet rs = null;

		int w_id = this.metadata.getWarehouseId();
		int o_carrier_id = this.metadata.getCarrierId();
		int d_id = 0;
		int c_id = 0;
		int no_o_id = 0;
		float ol_total = 0;

		Calendar calendar = Calendar.getInstance();
		Date now = calendar.getTime();
		Timestamp currentTimeStamp = new Timestamp(now.getTime());

		for(d_id = 1; d_id <= TpccConstants.DISTRICTS_PER_WAREHOUSE; d_id++)
		{
			if(logger.isTraceEnabled())
				logger.trace("SELECT COALESCE(MIN(no_o_id),0) FROM new_orders WHERE no_d_id = " + d_id + " AND " +
						"no_w_id" +
						" " +
						"= " + w_id);
			try
			{
				ps = statements.createPreparedStatement(con, 25);
				ps.setInt(1, d_id);
				ps.setInt(2, w_id);
				rs = ps.executeQuery();

				if(rs.next())
				{
					no_o_id = rs.getInt(1);
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

			if(no_o_id == 0)
				continue;
			else
			{
				if(logger.isDebugEnabled())
					logger.debug("No_o_id did not equal 0 -> " + no_o_id);
			}

			if(logger.isTraceEnabled())
				logger.trace("DELETE FROM new_orders WHERE no_o_id = " + no_o_id + " AND no_d_id = " + d_id + " AND " +
						"no_w_id = " + w_id);
			try
			{
				ps = statements.createPreparedStatement(con, 26);
				ps.setInt(1, no_o_id);
				ps.setInt(2, d_id);
				ps.setInt(3, w_id);
				ps.executeUpdate();

			} catch(SQLException e)
			{
				lastError = e.getMessage();
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(ps);
				this.rollbackQuietly(con);
				return false;
			}

			if(logger.isTraceEnabled())
				logger.trace("SELECT o_c_id FROM orders WHERE o_id = " + no_o_id + " AND o_d_id = " + d_id + " AND " +
						"o_w_id = " + w_id);
			try
			{
				ps = statements.createPreparedStatement(con, 27);
				ps.setInt(1, no_o_id);
				ps.setInt(2, d_id);
				ps.setInt(3, w_id);
				rs = ps.executeQuery();

				if(rs.next())
				{
					c_id = rs.getInt(1);
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

			if(logger.isTraceEnabled())
				logger.trace("UPDATE orders SET o_carrier_id = " + o_carrier_id + " WHERE o_id = " + no_o_id + " AND" +
						" " +
						"o_d_id = " + d_id + " AND o_w_id = " + w_id);
			try
			{
				ps = statements.createPreparedStatement(con, 28);
				ps.setInt(1, o_carrier_id);
				ps.setInt(2, no_o_id);
				ps.setInt(3, d_id);
				ps.setInt(4, w_id);
				ps.executeUpdate();

			} catch(SQLException e)
			{
				lastError = e.getMessage();
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(ps);
				this.rollbackQuietly(con);
				return false;
			}

			if(logger.isTraceEnabled())
				logger.trace("UPDATE order_line SET ol_delivery_d = " + currentTimeStamp.toString() + " WHERE " +
						"ol_o_id" +
						" " +
						"=" +
						" " + no_o_id + " AND ol_d_id = " + d_id + " AND ol_w_id = " + w_id);
			try
			{
				ps = statements.createPreparedStatement(con, 29);
				ps.setString(1, currentTimeStamp.toString());
				ps.setInt(2, no_o_id);
				ps.setInt(3, d_id);
				ps.setInt(4, w_id);
				ps.executeUpdate();

			} catch(SQLException e)
			{
				lastError = e.getMessage();
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(ps);
				this.rollbackQuietly(con);
				return false;
			}

			if(logger.isTraceEnabled())
				logger.trace("SELECT SUM(ol_amount) FROM order_line WHERE ol_o_id = " + no_o_id + " AND ol_d_id = " +
						d_id + " AND ol_w_id = " + w_id);
			try
			{
				ps = statements.createPreparedStatement(con, 30);
				ps.setInt(1, no_o_id);
				ps.setInt(2, d_id);
				ps.setInt(3, w_id);
				rs = ps.executeQuery();
				if(rs.next())
				{
					ol_total = rs.getFloat(1);
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

			if(logger.isTraceEnabled())
				logger.trace("UPDATE customer SET c_balance = c_balance + " + ol_total + ", c_delivery_cnt = " +
						"c_delivery_cnt + 1 WHERE c_id = " + c_id + " AND c_d_id = " + d_id + " AND " +
						"c_w_id" +
						" = " + w_id);
			try
			{
				ps = statements.createPreparedStatement(con, 31);
				ps.setFloat(1, ol_total);
				ps.setInt(2, c_id);
				ps.setInt(3, d_id);
				ps.setInt(4, w_id);
				ps.executeUpdate();

			} catch(SQLException e)
			{
				lastError = e.getMessage();
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(ps);
				this.rollbackQuietly(con);
				return false;
			}
		}

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
		return false;
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
