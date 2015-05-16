package runtime.operation;


import database.util.ExecutionPolicy;
import database.util.Row;
import util.defaults.DBDefaults;

import java.util.List;


/**
 * Created by dnlopes on 11/05/15.
 */
public class InsertChildOperation extends InsertOperation
{

	private List<Row> parentRows;

	public InsertChildOperation(int id, ExecutionPolicy policy, List<Row> parents, Row newRow)
	{
		super(id, policy, newRow);
		this.parentRows = parents;
	}

	@Override
	public void generateOperationStatements(List<String> shadowStatements)
	{
		super.generateOperationStatements(shadowStatements);

		for(Row parent : this.parentRows)
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append("UPDATE ");
			buffer.append(parent.getTable().getName());
			buffer.append(" SET ");
			buffer.append(SET_NOT_DELETED_EXPRESSION);
			buffer.append(",");
			buffer.append(DBDefaults.DELETED_COLUMN);
			buffer.append("=");
			buffer.append(DBDefaults.DELETED_CLOCK_PLACEHOLDER);
			buffer.append(" WHERE ");
			buffer.append(parent.getPrimaryKeyValue().getPrimaryKeyWhereClause());
			buffer.append(" AND compareClocks(");
			buffer.append(DBDefaults.DELETED_CLOCK_COLUMN);
			buffer.append(",");
			buffer.append(DBDefaults.CLOCK_VALUE_PLACEHOLDER);
			buffer.append(") ");

			if(this.tablePolicy == ExecutionPolicy.DELETEWINS)
			{


			} else if(this.tablePolicy == ExecutionPolicy.UPDATEWINS)
			{

			}
		}
	}
}
