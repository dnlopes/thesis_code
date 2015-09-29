package runtime.operation;


import database.constraints.fk.ForeignKeyConstraint;
import database.util.ExecutionPolicy;
import database.util.value.FieldValue;
import database.util.Row;
import runtime.transformer.OperationTransformer;
import util.defaults.DatabaseDefaults;
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
		//done
		this.row.addFieldValue(new FieldValue(this.row.getTable().getDeletedField(), DatabaseDefaults.DELETED_VALUE));
		this.row.addFieldValue(
				new FieldValue(this.row.getTable().getContentClockField(), DatabaseDefaults.CLOCK_VALUE_PLACEHOLDER));
		this.row.addFieldValue(
				new FieldValue(this.row.getTable().getDeletedClockField(), DatabaseDefaults.CLOCK_VALUE_PLACEHOLDER));

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

		String insertStatement = OperationTransformer.generateInsertStatement(this.row);
		shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), insertStatement);

		String childVisibilityOp = OperationTransformer.generateConditionalSetChildVisibleOnInsert(this.row,
				this.parentRows);
		shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), childVisibilityOp);

		if(!this.isFinal)
		{
			shadowTransaction.putToTempOperations(shadowTransaction.getOperationsSize(), insertStatement);

			for(RequestValue rValue : this.requestValues)
				rValue.setOpId(shadowTransaction.getOperationsSize());
		}
	}
}
