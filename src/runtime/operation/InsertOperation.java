package runtime.operation;


import database.constraints.Constraint;
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
 * Created by dnlopes on 11/05/15.
 */
public class InsertOperation extends AbstractOperation implements ShadowOperation
{

	protected List<RequestValue> requestValues;

	public InsertOperation(int id, ExecutionPolicy policy, Row newRow)
	{
		super(id, policy, OperationType.INSERT, newRow);
		this.requestValues = new ArrayList<>();
	}

	@Override
	public void generateStatements(ThriftShadowTransaction shadowTransaction)
	{
		this.row.addFieldValue(new FieldValue(this.row.getTable().getDeletedField(), DBDefaults.NOT_DELETED_VALUE));
		this.row.addFieldValue(
				new FieldValue(this.row.getTable().getContentClockField(), DBDefaults.CLOCK_VALUE_PLACEHOLDER));

		this.row.mergeUpdates();

		StringBuilder buffer = new StringBuilder();

		String insertOrUpdateStatement = OperationTransformer.generateInsertStatement(this.row);
		buffer.append(insertOrUpdateStatement);
		String op = buffer.toString();

		shadowTransaction.putToOperations(shadowTransaction.getOperations().size(), op);

		if(!this.isFinal)
		{
			shadowTransaction.putToTempOperations(shadowTransaction.getOperationsSize(), op);

			for(RequestValue rValue : this.requestValues)
				rValue.setOpId(shadowTransaction.getOperations().size());
		}
	}

	@Override
	public void createRequestsToCoordinate(CoordinatorRequest request)
	{
		int counter = 0;

		for(Constraint c : this.row.getContraintsToCheck())
		{
			if (!c.requiresCoordination())
				continue;

			switch(c.getType())
			{
			case AUTO_INCREMENT:
				String symbol = SYMBOL_KEY + counter;
				FieldValue fieldValue = this.row.getFieldValue(c.getFields().get(0).getFieldName());
				RequestValue requestValue = new RequestValue();
				requestValue.setConstraintId(c.getConstraintIdentifier());
				requestValue.setFieldName(fieldValue.getDataField().getFieldName());
				request.addToRequests(requestValue);
				requestValue.setTempSymbol(symbol);
				this.requestValues.add(requestValue);
				this.row.updateFieldValue(new FieldValue(fieldValue.getDataField(), symbol));
				this.isFinal = false;
				counter++;
				if(Configuration.TRACE_ENABLED)
					LOG.trace("new request id entry added for constraint {}", c.getConstraintIdentifier());
				break;
			case UNIQUE:
				StringBuilder buffer = new StringBuilder();
				Iterator<DataField> it = c.getFields().iterator();
				while(it.hasNext())
				{
					DataField currField = it.next();
					buffer.append(this.row.getFieldValue(currField.getFieldName()));
					if(it.hasNext())
						buffer.append(",");
				}
				UniqueValue uniqueValue = new UniqueValue(c.getConstraintIdentifier(), buffer.toString());
				request.addToUniqueValues(uniqueValue);
				if(Configuration.TRACE_ENABLED)
					LOG.trace("new unique check entry added for constraint {}", c.getConstraintIdentifier());
				break;
			case FOREIGN_KEY:
				break;
			case CHECK:
				DataField currField = c.getFields().get(0);
				FieldValue deltafieldValue = this.row.getFieldValue(currField.getFieldName());
				ApplyDelta applyDeltaRequest = new ApplyDelta();
				applyDeltaRequest.setConstraintId(c.getConstraintIdentifier());
				applyDeltaRequest.setDeltaValue(deltafieldValue.getValue());
				applyDeltaRequest.setRowId(this.row.getPrimaryKeyValue().getUniqueValue());
				request.addToDeltaValues(applyDeltaRequest);

				if(Configuration.TRACE_ENABLED)
					LOG.trace("new delta check entry added");
				break;
			default:
				RuntimeUtils.throwRunTimeException("unexpected constraint", ExitCode.UNEXPECTED_OP);
			}
		}
	}
}
