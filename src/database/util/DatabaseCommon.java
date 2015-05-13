package database.util;


import database.constraints.fk.ForeignKeyConstraint;
import database.constraints.fk.ParentChildRelation;
import database.scratchpad.IDBScratchPad;
import org.apache.commons.dbutils.DbUtils;
import runtime.RuntimeHelper;
import util.ExitCode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


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

	public static List<Row> findParentRows(Row childRow, List<ForeignKeyConstraint> constraints, IDBScratchPad pad)
			throws SQLException
	{
		List<Row> parents = new ArrayList<>(constraints.size());

		for(int i = 0; i < constraints.size(); i++)
		{
			ForeignKeyConstraint c = constraints.get(i);
			Row parent = findParent(childRow, c, pad);

			if(parent == null)
				throw new SQLException("parent row not found. Foreing key violated");

			parents.add(parent);
		}

		return parents;
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
		StringBuilder buffer = new StringBuilder();

		buffer.append("SELECT * FROM ");
		buffer.append(table.getName());
		buffer.append(" WHERE ");

		Iterator<ParentChildRelation> relationsIt = relations.iterator();

		while(relationsIt.hasNext())
		{
			ParentChildRelation relation = relationsIt.next();
			buffer.append(relation.getChild().getFieldName());
			buffer.append("=");
			buffer.append(parentRow.getFieldValue(relation.getParent().getFieldName()).getValue());

			if(relationsIt.hasNext())
				buffer.append(" AND ");
		}

		String query = buffer.toString();
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
		DatabaseTable remoteTable = constraint.getParentTable();

		StringBuilder buffer = new StringBuilder();
		buffer.append("SELECT *");
		//buffer.append(remoteTable.getPrimaryKey().getQueryClause());
		buffer.append(" FROM ");
		buffer.append(constraint.getParentTable());
		buffer.append(" WHERE ");

		Iterator<ParentChildRelation> relationsIt = constraint.getFieldsRelations().iterator();

		while(relationsIt.hasNext())
		{
			ParentChildRelation relation = relationsIt.next();
			DataField childField = relation.getChild();
			DataField parentField = relation.getParent();

			buffer.append(parentField.getFieldName());
			buffer.append("=");
			buffer.append(childRow.getFieldValue(childField.getFieldName()).getValue());

			if(relationsIt.hasNext())
				buffer.append(" AND ");
		}

		ResultSet rs = pad.executeQuery(buffer.toString());
		rs.next();
		PrimaryKeyValue parentPk = getPrimaryKeyValue(rs, remoteTable);

		if(parentPk == null)
			throw new SQLException("parent row not found. Foreing key violated");

		DbUtils.closeQuietly(rs);

		return new Row(remoteTable, parentPk);
	}

}
