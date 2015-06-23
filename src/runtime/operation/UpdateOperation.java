package runtime.operation;


import database.constraints.Constraint;
import database.constraints.check.CheckConstraint;
import database.util.*;
import database.util.field.DataField;
import runtime.transformer.OperationTransformer;
import runtime.RuntimeUtils;
import util.ExitCode;
import util.defaults.Configuration;
import util.defaults.DBDefaults;
import util.thrift.*;

import java.util.*;


/**
 * Created by dnlopes on 12/05/15.
 */
public class UpdateOperation extends AbstractOperation implements ShadowOperation
{
	protected List<RequestValue> requestValues;

	public UpdateOperation(int id, ExecutionPolicy policy, Row updatedRow)
	{
		super(id, policy, OperationType.UPDATE, updatedRow);
	}

	@Override
	public void generateStatements(ThriftShadowTransaction shadowTransaction)
	{
		this.row.updateFieldValue(
				new FieldValue(this.row.getTable().getContentClockField(), DBDefaults.CLOCK_VALUE_PLACEHOLDER));

		this.row.mergeUpdates();

		StringBuilder buffer = new StringBuilder();

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
				rValue.setOpId(shadowTransaction.getOperations().size());
		}

		// if @UPDATEWINS, make sure that row is visible in case some concurrent operation deleted it
		if(this.tablePolicy == ExecutionPolicy.UPDATEWINS)
		{
			String visibleOp = OperationTransformer.generateSetVisible(this.row);
			shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), visibleOp);
		}
	}

	@Override
	public void createRequestsToCoordinate(CoordinatorRequest request)
	{
		for(Constraint c : this.row.getContraintsToCheck())
		{
			switch(c.getType())
			{
			case AUTO_INCREMENT:
				// we do not consider updates at auto incremented fields
				RuntimeUtils.throwRunTimeException("update on AUTO INCREMENT field not valid", ExitCode.INVALIDUSAGE);
				break;
			case UNIQUE:
				StringBuilder buffer = new StringBuilder();
				Iterator<DataField> it = c.getFields().iterator();
				boolean shouldCoordinate = false;
				while(it.hasNext())
				{
					DataField currField = it.next();

					if(this.row.containsNewField(currField.getFieldName()))
					{
						shouldCoordinate = true;
						buffer.append(this.row.getUpdateFieldValue(currField.getFieldName()));
					} else
						buffer.append(this.row.getFieldValue(currField.getFieldName()));

					if(it.hasNext())
						buffer.append(",");
				}
				if(!shouldCoordinate)
					break;
				UniqueValue uniqueValue = new UniqueValue(c.getConstraintIdentifier(), buffer.toString());
				request.addToUniqueValues(uniqueValue);
				if(Configuration.TRACE_ENABLED)
					LOG.trace("new unique check entry added for constraint {}", c.getConstraintIdentifier());
				break;
			case CHECK:
				DataField currField = c.getFields().get(0);
				FieldValue oldFieldValue = this.row.getFieldValue(currField.getFieldName());
				FieldValue newFieldValue = this.row.getUpdateFieldValue(currField.getFieldName());

				String deltaValue = ((CheckConstraint) c).calculateDelta(newFieldValue.getFormattedValue(),
						oldFieldValue.getFormattedValue());
				ApplyDelta applyDeltaRequest = new ApplyDelta();
				applyDeltaRequest.setConstraintId(c.getConstraintIdentifier());
				applyDeltaRequest.setDeltaValue(deltaValue);
				applyDeltaRequest.setRowId(this.row.getPrimaryKeyValue().getUniqueValue());

				if(((CheckConstraint) c).mustCoordinate(newFieldValue.getFormattedValue(),
						oldFieldValue.getFormattedValue()))
				{
					applyDeltaRequest.setMustCoordinate(true);
					if(Configuration.TRACE_ENABLED)
						LOG.trace("new delta check entry added");
				}

				request.addToDeltaValues(applyDeltaRequest);
				break;
			default:
				RuntimeUtils.throwRunTimeException("unexpected constraint", ExitCode.UNEXPECTED_OP);
			}
		}
	}

}
