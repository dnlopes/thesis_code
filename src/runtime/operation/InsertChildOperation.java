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
import util.thrift.RequestValue;
import util.thrift.ThriftShadowTransaction;

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
	public void generateStatements(ThriftShadowTransaction shadowTransaction)
	{
		this.row.addFieldValue(new FieldValue(this.row.getTable().getDeletedField(), DBDefaults.NOT_DELETED_VALUE));
		this.row.addFieldValue(
				new FieldValue(this.row.getTable().getContentClockField(), DBDefaults.CLOCK_VALUE_PLACEHOLDER));
		this.row.addFieldValue(
				new FieldValue(this.row.getTable().getDeletedClockField(), DBDefaults.CLOCK_VALUE_PLACEHOLDER));

		for(ForeignKeyConstraint constraint : this.parentRows.keySet())
		{
			// add select query instead of static values for fields that are pointing to parent
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
				shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), update);
			}
		}

		String insertOrUpdateStatement = OperationTransformer.generateInsertStatement(this.row);
		buffer.setLength(0);
		buffer.append(insertOrUpdateStatement);

		String op = buffer.toString();
		shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), op);

		if(!this.isFinal)
		{
			shadowTransaction.putToTempOperations(shadowTransaction.getOperationsSize(), op);

			for(RequestValue rValue : this.requestValues)
				rValue.setOpId(shadowTransaction.getOperationsSize());
		}
	}
}
