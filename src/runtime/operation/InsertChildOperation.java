package runtime.operation;


import database.constraints.fk.ForeignKeyConstraint;
import database.constraints.fk.ParentChildRelation;
import database.util.ExecutionPolicy;
import database.util.FieldValue;
import database.util.QueryFieldValue;
import database.util.Row;
import runtime.OperationTransformer;
import runtime.QueryCreator;
import util.defaults.DBDefaults;

import java.util.List;
import java.util.Map;


/**
 * Created by dnlopes on 11/05/15.
 */
public class InsertChildOperation extends InsertOperation
{

	private Map<ForeignKeyConstraint, Row> parentRows;

	public InsertChildOperation(int id, ExecutionPolicy policy, Map<ForeignKeyConstraint, Row> parents, Row newRow)
	{
		super(id, policy, newRow);
		this.parentRows = parents;
	}

	@Override
	public void generateOperationStatements(List<String> shadowStatements)
	{
		this.row.addFieldValue(new FieldValue(this.row.getTable().getDeletedField(), DBDefaults.NOT_DELETED_VALUE));
		this.row.addFieldValue(
				new FieldValue(this.row.getTable().getContentClockField(), DBDefaults.CLOCK_VALUE_PLACEHOLDER));
		this.row.addFieldValue(
				new FieldValue(this.row.getTable().getDeletedClockField(), DBDefaults.CLOCK_VALUE_PLACEHOLDER));

		for(ForeignKeyConstraint constraint : this.parentRows.keySet())
		{
			// add select query instead of real values for fields that are pointing to parent
			// we do this only in the case of DELETE WINS,
			if(constraint.getPolicy().getExecutionPolicy() == ExecutionPolicy.DELETEWINS)
				for(ParentChildRelation relation : constraint.getFieldsRelations())
				{
					String query = QueryCreator.selectFieldFromRow(this.parentRows.get(constraint),
							relation.getParent());
					this.row.updateFieldValue(new QueryFieldValue(relation.getChild(), query));
				}
		}

		this.row.mergeUpdates();
		StringBuilder buffer = new StringBuilder();

		if(this.tablePolicy == ExecutionPolicy.UPDATEWINS)
		{
			for(Row parent : this.parentRows.values())
			{
				buffer.setLength(0);
				String update = OperationTransformer.generateInsertBackParentRow(parent);
				buffer.append(update);
				shadowStatements.add(update);
			}
		}

		String insertOrUpdateStatement = OperationTransformer.generateInsertStatement(this.row);
		buffer.setLength(0);
		buffer.append(insertOrUpdateStatement);
		shadowStatements.add(buffer.toString());
	}
}
