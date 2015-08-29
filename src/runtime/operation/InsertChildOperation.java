package runtime.operation;


import database.constraints.fk.ForeignKeyConstraint;
import database.util.ExecutionPolicy;
import database.util.value.FieldValue;
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
		this.row.addFieldValue(
				new FieldValue(this.row.getTable().getDeletedClockField(), DBDefaults.CLOCK_VALUE_PLACEHOLDER));

		this.row.mergeUpdates();

		for(ForeignKeyConstraint constraint : this.parentRows.keySet())
		{
			if(constraint.getPolicy().getExecutionPolicy() == ExecutionPolicy.UPDATEWINS)
			{
				Row parent = this.parentRows.get(constraint);

				String op = OperationTransformer.generateSetParentVisible(constraint, parent);
				shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), op);
				String mergedClockOp = OperationTransformer.mergeDeletedClock(parent);
				shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), mergedClockOp);
			}
		}

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
