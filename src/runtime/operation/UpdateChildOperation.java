package runtime.operation;


import database.constraints.fk.ForeignKeyAction;
import database.constraints.fk.ForeignKeyConstraint;
import database.constraints.fk.ParentChildRelation;
import database.util.ExecutionPolicy;
import database.util.value.FieldValue;
import database.util.value.QueryFieldValue;
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
				boolean filterDeletedParent = false;

				if(constraint.getPolicy().getUpdateAction() == ForeignKeyAction.SET_NULL)
					filterDeletedParent = true;

				String query = QueryCreator.selectFieldFromRow(this.parentRows.get(constraint), relation.getParent(),
						filterDeletedParent);
				this.row.updateFieldValue(new QueryFieldValue(relation.getChild(), query));
			}
		}

		this.row.mergeUpdates();

		StringBuilder buffer = new StringBuilder();

		// insert parents back in case they were deleted concurrently
		for(Map.Entry<ForeignKeyConstraint, Row> entry : this.parentRows.entrySet())
		{
			if(entry.getKey().getPolicy().getExecutionPolicy() == ExecutionPolicy.UPDATEWINS)
			{
				buffer.setLength(0);
				String update = OperationTransformer.generateSetVisible(entry.getValue());
				buffer.append(update);
				buffer.append(" AND ");
				buffer.append(DBDefaults.CLOCKS_IS_CONCURRENT_OR_GREATER_FUNCTION);
				buffer.append("(");
				buffer.append(DBDefaults.DELETED_CLOCK_COLUMN);
				buffer.append(",");
				buffer.append(DBDefaults.CLOCK_VALUE_PLACEHOLDER);
				buffer.append(")=1");
				shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), update);
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
			// now set the tuple visibility iff all parents are visible as well
			String parentsCounterQuery = QueryCreator.countParentsVisible(this.parentRows);
			buffer.setLength(0);
			String visibleOp = OperationTransformer.generateSetVisible(this.row);
			buffer.append(visibleOp);
			buffer.append(" AND ");
			buffer.append(this.parentRows.size());
			buffer.append("=(");
			buffer.append(parentsCounterQuery);
			buffer.append(")");
		}

	}
}

