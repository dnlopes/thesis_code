package client.execution.temporary;


import client.execution.operation.SQLOperationType;
import client.execution.operation.SQLSelect;
import common.database.SQLInterface;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by dnlopes on 16/09/15.
 */
public class DBReadOnlyInterface implements ReadOnlyInterface
{

	private SQLInterface sqlInterface;

	public DBReadOnlyInterface(SQLInterface sqlInterface)
	{
		this.sqlInterface = sqlInterface;
	}

	@Override
	public ResultSet executeQuery(SQLSelect selectSQL) throws SQLException
	{

		if(selectSQL.getOpType() != SQLOperationType.SELECT)
			throw new SQLException("query statement expected");

		selectSQL.prepareOperation();

		return this.sqlInterface.executeQuery(selectSQL.getSQLString());
	}
}
