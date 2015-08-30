package runtime.operation;


import database.constraints.Constraint;
import database.constraints.check.CheckConstraint;
import database.util.*;
import database.util.field.DataField;
import database.util.value.FieldValue;
import runtime.transformer.OperationTransformer;
import runtime.RuntimeUtils;
import util.ExitCode;
import util.defaults.DBDefaults;
import util.thrift.*;

import java.sql.SQLException;
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
		//done
		this.row.updateFieldValue(
				new FieldValue(this.row.getTable().getContentClockField(), DBDefaults.CLOCK_VALUE_PLACEHOLDER));

		this.row.mergeUpdates();

		String updateStatement = OperationTransformer.generateUpdateStatement(this.row);
		shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), updateStatement);

		String mergeClockStatement = OperationTransformer.mergeContentClock(this.row);
		shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), mergeClockStatement);

		if(!this.isFinal)
		{
			shadowTransaction.putToTempOperations(shadowTransaction.getOperationsSize(), updateStatement);

			for(RequestValue rValue : this.requestValues)
				rValue.setOpId(shadowTransaction.getOperations().size());
		}

		// if @UPDATEWINS, make sure that row is visible in case some concurrent operation deleted it
		if(this.tablePolicy == ExecutionPolicy.UPDATEWINS)
		{
			String insertRowBack = OperationTransformer.generateInsertRowBack(this.row);
			shadowTransaction.putToTempOperations(shadowTransaction.getOperationsSize(), insertRowBack);
			mergeClockStatement = OperationTransformer.mergeDeletedClock(this.row);
			shadowTransaction.putToTempOperations(shadowTransaction.getOperationsSize(), mergeClockStatement);
		}
	}

	@Override
	public void createRequestsToCoordinate(CoordinatorRequest request) throws SQLException
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
				if(LOG.isTraceEnabled())
					LOG.trace("new unique check entry added for constraint {}", c.getConstraintIdentifier());
				break;
			case CHECK:
				DataField currField = c.getFields().get(0);
				FieldValue oldFieldValue = this.row.getFieldValue(currField.getFieldName());
				FieldValue newFieldValue = this.row.getUpdateFieldValue(currField.getFieldName());

				if(!((CheckConstraint) c).isValidValue(newFieldValue.getValue()))
					throw new SQLException("field value not valid due to a check constraint restriction");

				String deltaValue = ((CheckConstraint) c).calculateDelta(newFieldValue.getValue(),
						oldFieldValue.getValue());
				ApplyDelta applyDeltaRequest = new ApplyDelta();
				applyDeltaRequest.setConstraintId(c.getConstraintIdentifier());
				applyDeltaRequest.setDeltaValue(deltaValue);
				applyDeltaRequest.setRowId(this.row.getPrimaryKeyValue().getUniqueValue());

				if(((CheckConstraint) c).mustCoordinate(newFieldValue.getValue(),
						oldFieldValue.getValue()))
				{
					applyDeltaRequest.setMustCoordinate(true);
					if(LOG.isTraceEnabled())
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
