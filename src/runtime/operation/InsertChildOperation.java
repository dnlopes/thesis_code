package runtime.operation;


import database.constraints.fk.ForeignKeyAction;
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
		this.row.addFieldValue(new FieldValue(this.row.getTable().getDeletedField(), DBDefaults.DELETED_VALUE));
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

				if(constraint.getPolicy().getDeleteAction() == ForeignKeyAction.SET_NULL)
					filterDeletedParent = true;

				String query = QueryCreator.selectFieldFromRow(this.parentRows.get(constraint), relation.getParent(),
						filterDeletedParent);
				this.row.updateFieldValue(new QueryFieldValue(relation.getChild(), query));
			}
		}

		this.row.mergeUpdates();
		StringBuilder buffer = new StringBuilder();

		for(Map.Entry<ForeignKeyConstraint, Row> entry : this.parentRows.entrySet())
		{
			ForeignKeyConstraint fkConstraint = entry.getKey();
			if(fkConstraint.getPolicy().getExecutionPolicy() == ExecutionPolicy.UPDATEWINS)
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

		String insertOrUpdateStatement = OperationTransformer.generateInsertStatement(this.row);
		buffer.setLength(0);
		buffer.append(insertOrUpdateStatement);

		String op = buffer.toString();
		shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), op);

		// now set the tuple visibility
		String parentsCounterQuery = QueryCreator.countParentsVisible(this.parentRows);
		buffer.setLength(0);
		String visibleOp = OperationTransformer.generateSetVisible(this.row);
		buffer.append(visibleOp);
		buffer.append(" AND ");
		buffer.append(this.parentRows.size());
		buffer.append("=(");
		buffer.append(parentsCounterQuery);
		buffer.append(")");

		if(!this.isFinal)
		{
			shadowTransaction.putToTempOperations(shadowTransaction.getOperationsSize(), op);

			for(RequestValue rValue : this.requestValues)
				rValue.setOpId(shadowTransaction.getOperationsSize());
		}
	}
}
