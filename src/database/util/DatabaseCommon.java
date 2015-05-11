package database.util;


import database.constraints.fk.ForeignKeyConstraint;
import database.scratchpad.IDBScratchPad;
import org.apache.commons.dbutils.DbUtils;
import runtime.RuntimeHelper;
import util.ExitCode;
import util.defaults.Configuration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;


/**
 * Created by dnlopes on 11/05/15.
 */
public class DatabaseCommon
{

	private static DatabaseMetadata dbMetadata = Configuration.getInstance().getDatabaseMetadata();

	public static PrimaryKeyValue getPrimaryKeyValue(ResultSet rs, String tableName) throws SQLException
	{
		PrimaryKeyValue pkValue = null;
		PrimaryKey pk = dbMetadata.getTable(tableName).getPrimaryKey();

		while(rs.next())
		{

			pkValue = new PrimaryKeyValue(tableName);

			if(!rs.isLast())
				RuntimeHelper.throwRunTimeException("found more than one parent for child",
						ExitCode.FETCH_RESULTS_ERROR);

			for(DataField field : pk.getPrimaryKeyFields().values())
			{
				String fieldValue = rs.getObject(field.getFieldName()).toString();

				if(fieldValue == null)
					RuntimeHelper.throwRunTimeException("primary key cannot be null", ExitCode.ERRORNOTNULL);

				FieldValue fValue = new FieldValue(field.getCrdtType(), field.getFieldName(), fieldValue);
				pkValue.addFieldValue(fValue);
			}
		}

		return pkValue;
	}

	public static Row[] findParentRows(Row childRow, List<ForeignKeyConstraint> constraints, IDBScratchPad pad)
			throws SQLException
	{
		Row[] parents = new Row[constraints.size()];

		for(int i = 0; i < parents.length; i++)
		{
			ForeignKeyConstraint c = constraints.get(i);
			Row parent = findParent(childRow, c, pad);

			if(parent == null)
				throw new SQLException("parent row not found. Foreing key violated");

			parents[i] = parent;
		}

		return parents;
	}

	private static Row findParent(Row childRow, ForeignKeyConstraint constraint, IDBScratchPad pad) throws SQLException
	{
		String tableName = constraint.getTableName();
		String remoteTable = constraint.getRemoteTable();

		StringBuilder buffer = new StringBuilder();
		buffer.append("SELECT ");
		buffer.append(dbMetadata.getTable(remoteTable).getPrimaryKey().getQueryClause());
		buffer.append(" FROM ");
		buffer.append(constraint.getRemoteTable());
		buffer.append(" WHERE ");

		Iterator<DataField> fieldsIt = constraint.getFields().iterator();

		while(fieldsIt.hasNext())
		{
			DataField localField = fieldsIt.next();
			buffer.append(localField.getFieldName());
			buffer.append("=");
			buffer.append(childRow.getFieldValue(localField.getFieldName()).getValue());

			if(fieldsIt.hasNext())
				buffer.append(" AND ");
		}

		ResultSet rs = pad.executeQuery(buffer.toString());
		PrimaryKeyValue parentPk = getPrimaryKeyValue(rs, remoteTable);
		DbUtils.closeQuietly(rs);

		if(parentPk == null)
			throw new SQLException("parent row not found. Foreing key violated");

		return new Row(dbMetadata.getTable(tableName), parentPk);

	}

}
