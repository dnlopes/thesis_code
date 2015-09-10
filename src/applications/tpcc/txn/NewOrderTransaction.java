package applications.tpcc.txn;


import applications.Transaction;
import applications.tpcc.TpccStatements;
import applications.tpcc.metadata.NewOrderMetadata;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by dnlopes on 05/09/15.
 */
public class NewOrderTransaction implements Transaction
{
	private static final Logger logger = LoggerFactory.getLogger(NewOrderTransaction.class);

	private static boolean JOINS_ENABLED = false;

	private final NewOrderMetadata metadata;
	private String lastError;

	public NewOrderTransaction(NewOrderMetadata txnMetadata)
	{
		this.metadata = txnMetadata;
	}

	@Override
	public boolean executeTransaction(Connection con)
	{
		TpccStatements statements = TpccStatements.getInstance();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String c_last, c_credit;
		float c_discount, w_tax;

		if(JOINS_ENABLED)
		{
			try
			{
				int column = 1;
				//final PreparedStatement pstmt0 = pStmts.getStatement(0);
				ps = statements.createPreparedStatement(con, 0);
				ps.setInt(column++, this.metadata.getWarehouseId());
				ps.setInt(column++, this.metadata.getWarehouseId());
				ps.setInt(column++, this.metadata.getDistrictId());
				ps.setInt(column++, this.metadata.getCustomerId());

				if(logger.isTraceEnabled())
					logger.trace(
							"SELECT c_discount, c_last, c_credit, w_tax FROM customer, warehouse WHERE w_id = " +
									this.metadata.getWarehouseId() + " AND c_w_id = " + this.metadata.getWarehouseId
									() + " AND c_d_id = " + this.metadata.getDistrictId() + " AND " +
									"c_id = " +
									this.metadata.getCustomerId());

				rs = ps.executeQuery();

				if(rs.next())
				{
					c_discount = rs.getFloat(1);
					c_last = rs.getString(2);
					c_credit = rs.getString(3);
					w_tax = rs.getFloat(4);
				}

				rs.close();

			} catch(SQLException e)
			{
				this.lastError = e.getMessage();
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(ps);
				this.rollbackQuietly(con);
				return false;
			}
		} else if(!JOINS_ENABLED)
		{
			//TODO: copy-paste
		}

		return false;
	}

	private void rollbackQuietly(Connection connection)
	{
		try {
			connection.rollback();
		} catch (SQLException ignored) {

		}
	}

	@Override
	public boolean isReadOnly()
	{
		return false;
	}
}
