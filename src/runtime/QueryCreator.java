package runtime;


import database.constraints.fk.ForeignKeyConstraint;
import database.constraints.fk.ParentChildRelation;
import database.util.DataField;
import database.util.DatabaseTable;
import database.util.Row;

import java.util.Iterator;
import java.util.List;


/**
 * Created by dnlopes on 16/05/15.
 */
public class QueryCreator
{

	/**
	 * Generates a SQL query that selects all child rows that are pointing to the given parent row
	 *
	 * @param parentRow
	 * @param table
	 * @param relations
	 *
	 * @return
	 */
	public static String findChildFromTableQuery(Row parentRow, DatabaseTable table,
												 List<ParentChildRelation> relations)
	{
		StringBuilder buffer = new StringBuilder();


		buffer.append("SELECT ");
		buffer.append(table.getNormalFieldsSelection());
		buffer.append(" FROM ");
		buffer.append(table.getName());
		buffer.append(" WHERE ");

		Iterator<ParentChildRelation> relationsIt = relations.iterator();

		while(relationsIt.hasNext())
		{
			ParentChildRelation relation = relationsIt.next();
			buffer.append(relation.getChild().getFieldName());
			buffer.append("=");
			buffer.append(parentRow.getFieldValue(relation.getParent().getFieldName()).getFormattedValue());

			if(relationsIt.hasNext())
				buffer.append(" AND ");
		}

		return buffer.toString();
	}

	/**
	 * Generates a SQL query to find the matching parent row for the given child row, that is associated with the
	 * given foreign key
	 *
	 * @param childRow
	 * @param constraint
	 *
	 * @return
	 */
	public static String findParent(Row childRow, ForeignKeyConstraint constraint)
	{
		DatabaseTable remoteTable = constraint.getParentTable();

		StringBuilder buffer = new StringBuilder();
		buffer.append("SELECT ");
		buffer.append(remoteTable.getNormalFieldsSelection());
		buffer.append(" FROM ");
		buffer.append(remoteTable.getName());
		buffer.append(" WHERE ");

		Iterator<ParentChildRelation> relationsIt = constraint.getFieldsRelations().iterator();

		while(relationsIt.hasNext())
		{
			ParentChildRelation relation = relationsIt.next();
			DataField childField = relation.getChild();
			DataField parentField = relation.getParent();

			buffer.append(parentField.getFieldName());
			buffer.append("=");
			buffer.append(childRow.getFieldValue(childField.getFieldName()).getFormattedValue());

			if(relationsIt.hasNext())
				buffer.append(" AND ");
		}

		return buffer.toString();
	}

	/**
	 * Generates a SQL query that SELECT the given field of the given row
	 *
	 * @param parentRow
	 * @param field
	 *
	 * @return
	 */
	public static String selectFieldFromRow(Row parentRow, DataField field)
	{
		StringBuilder buffer = new StringBuilder();

		buffer.append("SELECT ");
		buffer.append(field.getFieldName());
		buffer.append(" FROM ");
		buffer.append(parentRow.getTable().getName());
		buffer.append(" WHERE ");
		buffer.append(parentRow.getPrimaryKeyValue().getPrimaryKeyWhereClause());

		return buffer.toString();
	}
}
