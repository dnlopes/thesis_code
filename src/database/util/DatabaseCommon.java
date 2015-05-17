package database.util;


import database.constraints.fk.ForeignKeyConstraint;
import database.constraints.fk.ParentChildRelation;
import database.scratchpad.IDBScratchPad;
import org.apache.commons.dbutils.DbUtils;
import runtime.QueryCreator;
import runtime.RuntimeHelper;
import util.ExitCode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by dnlopes on 11/05/15.
 */
public class DatabaseCommon
{

	public static PrimaryKeyValue getPrimaryKeyValue(ResultSet rs, DatabaseTable dbTable) throws SQLException
	{
		PrimaryKeyValue pkValue = new PrimaryKeyValue(dbTable.getName());
		PrimaryKey pk = dbTable.getPrimaryKey();

		for(DataField field : pk.getPrimaryKeyFields().values())
		{
			String fieldValue = rs.getObject(field.getFieldName()).toString();

			if(fieldValue == null)
				RuntimeHelper.throwRunTimeException("primary key cannot be null", ExitCode.ERRORNOTNULL);

			FieldValue fValue = new FieldValue(field, fieldValue);
			pkValue.addFieldValue(fValue);
		}

		return pkValue;
	}

	public static Map<ForeignKeyConstraint, Row> findParentRows(Row childRow, List<ForeignKeyConstraint> constraints, IDBScratchPad pad)
			throws SQLException
	{
		Map<ForeignKeyConstraint, Row> parentByConstraint = new HashMap<>();


		for(int i = 0; i < constraints.size(); i++)
		{
			ForeignKeyConstraint c = constraints.get(i);
			Row parent = findParent(childRow, c, pad);
			parentByConstraint.put(c, parent);

			if(parent == null)
				throw new SQLException("parent row not found. Foreing key violated");

		}

		return parentByConstraint;
	}

	public static Row getFullRow(ResultSet rs, DatabaseTable dbTable) throws SQLException
	{
		PrimaryKeyValue pkValue = getPrimaryKeyValue(rs, dbTable);
		Row row = new Row(dbTable, pkValue);

		for(DataField field : dbTable.getNormalFields().values())
		{
			String fieldValue = rs.getObject(field.getFieldName()).toString();

			if(fieldValue == null)
				RuntimeHelper.throwRunTimeException("field value is null", ExitCode.ERRORNOTNULL);

			FieldValue fValue = new FieldValue(field, fieldValue);
			row.addFieldValue(fValue);
		}

		return row;
	}

	public static List<Row> findChildsFromTable(Row parentRow, DatabaseTable table, List<ParentChildRelation>
			relations,
												IDBScratchPad pad) throws SQLException
	{
		List<Row> childs = new ArrayList<>();

		String query = QueryCreator.findChildFromTableQuery(parentRow, table, relations);

		ResultSet rs = pad.executeQuery(query);

		while(rs.next())
		{
			Row aChild = getFullRow(rs, table);
			childs.add(aChild);
		}

		DbUtils.closeQuietly(rs);
		return childs;
	}

	private static Row findParent(Row childRow, ForeignKeyConstraint constraint, IDBScratchPad pad) throws SQLException
	{
		String query = QueryCreator.findParent(childRow, constraint);

		ResultSet rs = pad.executeQuery(query);
		if(!rs.isBeforeFirst())
		{
			DbUtils.closeQuietly(rs);
			throw new SQLException("parent row not found. Foreing key violated");
		}

		rs.next();
		DatabaseTable remoteTable = constraint.getParentTable();
		PrimaryKeyValue parentPk = getPrimaryKeyValue(rs, remoteTable);
		DbUtils.closeQuietly(rs);

		return new Row(remoteTable, parentPk);
	}

}
