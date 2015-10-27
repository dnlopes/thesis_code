package applications.tpcc.txn;


import applications.BaseBenchmarkOptions;
import applications.Transaction;
import applications.tpcc.TpccStatements;
import applications.tpcc.metadata.PaymentMetadata;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import common.util.RuntimeUtils;
import common.util.ExitCode;

import java.sql.*;


/**
 * Created by dnlopes on 05/09/15.
 */
public class PaymentTransaction implements Transaction
{

	private static final Logger logger = LoggerFactory.getLogger(NewOrderTransaction.class);

	private final BaseBenchmarkOptions options;
	private final PaymentMetadata metadata;
	private String lastError;

	public PaymentTransaction(PaymentMetadata txnMetadata, BaseBenchmarkOptions options)
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
			con.setReadOnly(false);
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
		int c_id = this.metadata.getCustomerId();
		String w_name = null;
		String w_street_1 = null;
		String w_street_2 = null;
		String w_city = null;
		String w_state = null;
		String w_zip = null;

		int c_d_id = this.metadata.getCustomerDistrictId();
		int c_w_id = this.metadata.getCustomerWarehouseId();
		String c_first = null;
		String c_middle = null;
		String c_last = null;
		String c_street_1 = null;
		String c_street_2 = null;
		String c_city = null;
		String c_state = null;
		String c_zip = null;
		String c_phone = null;
		String c_since = null;
		String c_credit = null;

		int c_credit_lim = 0;
		float c_discount = 0;
		float c_balance = 0;
		String c_data = null;
		String c_new_data = null;

		float h_amount = this.metadata.getH_amount();
		String h_data = null;
		String d_name = null;
		String d_street_1 = null;
		String d_street_2 = null;
		String d_city = null;
		String d_state = null;
		String d_zip = null;

		int namecnt = 0;
		int n;
		int proceed = 0;

		//Time Stamp
		final Timestamp currentTimeStamp = new Timestamp(System.currentTimeMillis());

		proceed = 1;

		try
		{
			ps = statements.createPreparedStatement(con, 9);
			ps.setFloat(1, h_amount);
			ps.setInt(2, w_id);

			if(logger.isTraceEnabled())
				logger.trace("UPDATE warehouse SET w_ytd = w_ytd + " + h_amount + " WHERE w_id = " + w_id);

			ps.executeUpdate();

		} catch(SQLException e)
		{
			lastError = e.getMessage();
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
			this.rollbackQuietly(con);
			return false;
		}
		proceed = 2;

		try
		{
			ps = statements.createPreparedStatement(con, 10);
			ps.setInt(1, w_id);

			if(logger.isTraceEnabled())
				logger.trace(
						"SELECT w_street_1, w_street_2, w_city, w_state, w_zip, w_name FROM warehouse WHERE w_id " +
								"=" +
								" " + w_id);

			rs = ps.executeQuery();

			if(rs.next())
			{
				w_street_1 = rs.getString(1);
				w_street_2 = rs.getString(2);
				w_city = rs.getString(3);
				w_state = rs.getString(4);
				w_zip = rs.getString(5);
				w_name = rs.getString(6);
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

		proceed = 3;

		try
		{
			ps = statements.createPreparedStatement(con, 11);
			ps.setFloat(1, h_amount);
			ps.setInt(2, w_id);
			ps.setInt(3, d_id);

			if(logger.isTraceEnabled())
				logger.trace("UPDATE district SET d_ytd = d_ytd + " + h_amount + " WHERE d_w_id = " + w_id + " AND " +
						"d_id" +
						" = " + d_id);
			ps.executeUpdate();

		} catch(SQLException e)
		{
			lastError = e.getMessage();
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
			this.rollbackQuietly(con);
			return false;
		}

		proceed = 4;

		try
		{
			ps = statements.createPreparedStatement(con, 12);
			ps.setInt(1, w_id);
			ps.setInt(2, d_id);
			if(logger.isTraceEnabled())
				logger.trace(
						"SELECT d_street_1, d_street_2, d_city, d_state, d_zip, d_name FROM district WHERE d_w_id" +
								" " +
								"= " + w_id + " AND d_id = " + d_id);
			rs = ps.executeQuery();
			if(rs.next())
			{
				d_street_1 = rs.getString(1);
				d_street_2 = rs.getString(2);
				d_city = rs.getString(3);
				d_state = rs.getString(4);
				d_zip = rs.getString(5);
				d_name = rs.getString(6);
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

		if(this.metadata.getByname() >= 1)
		{

			c_last = this.metadata.getLastName();

			proceed = 5;

			try
			{
				ps = statements.createPreparedStatement(con, 13);
				ps.setInt(1, c_w_id);
				ps.setInt(2, c_d_id);
				ps.setString(3, c_last);

				if(logger.isTraceEnabled())
					logger.trace("SELECT count(c_id) FROM customer WHERE c_w_id = " + c_w_id + " AND c_d_id = " +
							c_d_id + " AND c_last = " + c_last);

				rs = ps.executeQuery();
				if(rs.next())
				{
					namecnt = rs.getInt(1);
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
				ps = statements.createPreparedStatement(con, 14);
				ps.setInt(1, c_w_id);
				ps.setInt(2, c_d_id);
				ps.setString(3, c_last);

				if(logger.isTraceEnabled())
					logger.trace("SELECT c_id FROM customer WHERE c_w_id = " + c_w_id + " AND c_d_id = " + c_d_id +
							"" +
							" " +
							"AND c_last = " + c_last + " ORDER BY c_first");

				if(namecnt % 2 == 1)
				{
					namecnt++;	/* Locate midpoint customer; */
				}

				rs = ps.executeQuery();
				for(n = 0; n < namecnt / 2; n++)
				{
					if(rs.next())
					{
						//SUCCESS
						c_id = rs.getInt(1);
					} else
					{
						lastError = "illegal state exception";
						DbUtils.closeQuietly(rs);
						DbUtils.closeQuietly(ps);
						this.rollbackQuietly(con);
						return false;
					}
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

		}

		proceed = 6;

		try
		{
			ps = statements.createPreparedStatement(con, 15);
			ps.setInt(1, c_w_id);
			ps.setInt(2, c_d_id);
			ps.setInt(3, c_id);

			if(logger.isTraceEnabled())
				logger.trace("SELECT c_first, c_middle, c_last, c_street_1, c_street_2, c_city, c_state, c_zip, " +
						"c_phone, c_credit, c_credit_lim, c_discount, c_balance, c_since FROM customer " +
						"WHERE c_w_id = " + c_w_id + " AND c_d_id = " + c_d_id + " AND c_id = " + c_id +
						"" +
						" FOR UPDATE");

			rs = ps.executeQuery();
			if(rs.next())
			{
				c_first = rs.getString(1);
				c_middle = rs.getString(2);
				c_last = rs.getString(3);
				c_street_1 = rs.getString(4);
				c_street_2 = rs.getString(5);
				c_city = rs.getString(6);
				c_state = rs.getString(7);
				c_zip = rs.getString(8);
				c_phone = rs.getString(9);
				c_credit = rs.getString(10);
				c_credit_lim = rs.getInt(11);
				c_discount = rs.getFloat(12);
				c_balance = rs.getFloat(13);
				c_since = rs.getString(14);
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

		c_balance += h_amount;

		if(c_credit != null)
		{
			if(c_credit.contains("BC"))
			{
				proceed = 7;

				try
				{
					ps = statements.createPreparedStatement(con, 16);
					ps.setInt(1, c_w_id);
					ps.setInt(2, c_d_id);
					ps.setInt(3, c_id);

					if(logger.isTraceEnabled())
						logger.trace(
								"SELECT c_data FROM customer WHERE c_w_id = " + c_w_id + " AND c_d_id = " + c_d_id +
										"" +
										" AND c_id = " + c_id);

					rs = ps.executeQuery();
					if(rs.next())
					{
						c_data = rs.getString(1);
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

				//to do: c_new_data is never used - this is a bug ported exactly from the original code
				c_new_data = String.format("| %d %d %d %d %d $%f %s %s", c_id, c_d_id, c_w_id, d_id, w_id, h_amount,
						currentTimeStamp.toString(), c_data);

				//to do: fix this - causes index out of bounds exceptions
				//c_new_data = ( c_new_data + c_data.substring(0, (500 - c_new_data.length()) ) );

				proceed = 8;

				try
				{
					ps = statements.createPreparedStatement(con, 17);
					ps.setFloat(1, c_balance);
					ps.setString(2, c_data);
					ps.setInt(3, c_w_id);
					ps.setInt(4, c_d_id);
					ps.setInt(5, c_id);

					if(logger.isTraceEnabled())
						logger.trace(
								"UPDATE customer SET c_balance = " + c_balance + ", c_data = " + c_data + " WHERE" +
										" " +
										"c_w_id = " + c_w_id + " AND c_d_id = " + c_d_id + " AND c_id = " + c_id);

					ps.executeUpdate();

				} catch(SQLException e)
				{
					lastError = e.getMessage();
					DbUtils.closeQuietly(rs);
					DbUtils.closeQuietly(ps);
					this.rollbackQuietly(con);
					return false;
				}

			} else
			{
				proceed = 9;

				try
				{
					ps = statements.createPreparedStatement(con, 18);
					ps.setFloat(1, c_balance);
					ps.setInt(2, c_w_id);
					ps.setInt(3, c_d_id);
					ps.setInt(4, c_id);

					if(logger.isTraceEnabled())
						logger.trace("UPDATE customer SET c_balance = " + c_balance + " WHERE c_w_id = " + c_w_id +
								"" +
								" " +
								"AND c_d_id = " + c_d_id + " AND c_id = " + c_id);

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
		} else
		{
			proceed = 9;

			try
			{
				ps = statements.createPreparedStatement(con, 18);
				ps.setFloat(1, c_balance);
				ps.setInt(2, c_w_id);
				ps.setInt(3, c_d_id);
				ps.setInt(4, c_id);

				if(logger.isTraceEnabled())
					logger.trace("UPDATE customer SET c_balance = " + c_balance + " WHERE c_w_id = " + c_w_id + " " +
							"AND" +
							" " +
							"c_d_id = " + c_d_id + " AND c_id = " + c_id);
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

		h_data = RandomStringUtils.randomAlphanumeric(10).toUpperCase();
		proceed = 10;

		/*
		try
		{
			ps = statements.createPreparedStatement(con, 19);
			ps.setInt(1, c_d_id);
			ps.setInt(2, c_w_id);
			ps.setInt(3, c_id);
			ps.setInt(4, d_id);
			ps.setInt(5, w_id);
			ps.setString(6, currentTimeStamp.toString());
			ps.setFloat(7, h_amount);
			ps.setString(8, h_data);

			if(logger.isTraceEnabled())
				logger.trace("INSERT INTO history(h_c_d_id, h_c_w_id, h_c_id, h_d_id, h_w_id, h_date, h_amount, " +
						"h_data)" +
						" VALUES( " + c_d_id + "," + c_w_id + "," + c_id + "," + d_id + "," + w_id + "," +
						currentTimeStamp.toString() + "," + h_amount + ",");

			ps.executeUpdate();

		} catch(SQLException e)
		{
			lastError = e.getMessage();
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
			this.rollbackQuietly(con);
			return false;
		}
           */
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

	@Override
	public String getName()
	{
		return "PaymentTransaction";
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
