package runtime.operation;


import database.constraints.fk.ForeignKeyConstraint;
import database.constraints.fk.ParentChildRelation;
import database.util.ExecutionPolicy;
import database.util.FieldValue;
import database.util.QueryFieldValue;
import database.util.Row;
import runtime.transformer.OperationTransformer;
import runtime.transformer.QueryCreator;
import util.defaults.DBDefaults;
import util.thrift.RequestValue;
import util.thrift.ThriftShadowTransaction;

import java.util.Map;


/**
 * Created by dnlopes on 23/06/15.
 */
public class UpdateChildOperation extends UpdateOperation
{

	//@TODO: we must implement the logic where the child tuple "changes" parent
	private Map<ForeignKeyConstraint, Row> parentRows;
	
	public UpdateChildOperation(int id, ExecutionPolicy policy, Row updatedRow, Map<ForeignKeyConstraint, Row> parents)
	{
		super(id, policy, updatedRow);
		this.parentRows = parents;
	}

	@Override
	public void generateStatements(ThriftShadowTransaction shadowTransaction)
	{
		this.row.addFieldValue(
				new FieldValue(this.row.getTable().getContentClockField(), DBDefaults.CLOCK_VALUE_PLACEHOLDER));

		for(ForeignKeyConstraint constraint : this.parentRows.keySet())
		{
			//@info: we use select query instead of static values for fields that are pointing to parent
			// this way we make sure we never break the foreign key invariant
			// we also dynamically set the '_del' flag of the child to reflect the visibility of its parent
			for(ParentChildRelation relation : constraint.getFieldsRelations())
			{
				String query = QueryCreator.selectFieldFromRow(this.parentRows.get(constraint), relation.getParent(),
						true);
				this.row.updateFieldValue(new QueryFieldValue(relation.getChild(), query));
			}
		}

		this.row.mergeUpdates();

		StringBuilder buffer = new StringBuilder();

		// insert parents back in case they were deleted concurrently
		if(this.row.getTable().getExecutionPolicy() == ExecutionPolicy.UPDATEWINS)
		{
			for(Map.Entry<ForeignKeyConstraint, Row> entry : this.parentRows.entrySet())
			{
				if(entry.getKey().getPolicy().getExecutionPolicy() == ExecutionPolicy.UPDATEWINS)
				{
					buffer.setLength(0);
					String update = OperationTransformer.generateInsertBackParentRow(entry.getValue());
					buffer.append(update);
					shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), update);
				}
			}
		}

		buffer.setLength(0);
		String updateStatement = OperationTransformer.generateUpdateStatement(this.row);
		buffer.append(updateStatement);
		buffer.append(" WHERE ");
		buffer.append(this.row.getPrimaryKeyValue().getPrimaryKeyWhereClause());
		buffer.append(" AND ");
		String compareClockClause = OperationTransformer.generateContentUpdateFunctionClause(true);
		buffer.append(compareClockClause);

		String op = buffer.toString();
		shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), op);

		if(!this.isFinal)
		{
			shadowTransaction.putToTempOperations(shadowTransaction.getOperationsSize(), op);

			for(RequestValue rValue : this.requestValues)
				rValue.setOpId(shadowTransaction.getOperationsSize());
		}

		// if @UPDATEWINS, make sure that row is visible in case some concurrent operation deleted it
		if(this.tablePolicy == ExecutionPolicy.UPDATEWINS)
		{
			/*String delFieldQuery = QueryCreator.selectFieldFromRow(this.parentRows.get(constraint),
			//		constraint.getParentTable().getField(DBDefaults.DELETED_COLUMN));
			//this.row.updateFieldValue(
			//		new QueryFieldValue(constraint.getChildTable().getDeletedField(), delFieldQuery));
			String visibleOp = OperationTransformer.generateSetVisible(this.row,);
			shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), visibleOp);    */
		}

	}
}

