package runtime.operation;


import database.constraints.Constraint;
import database.constraints.check.CheckConstraint;
import database.util.*;
import database.util.field.DataField;
import database.util.value.FieldValue;
import runtime.IdentifierFactory;
import runtime.transformer.OperationTransformer;
import runtime.RuntimeUtils;
import util.ExitCode;
import util.defaults.DatabaseDefaults;
import util.thrift.*;

import java.sql.SQLException;
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
		//done
		this.row.addFieldValue(new FieldValue(this.row.getTable().getDeletedField(), DatabaseDefaults.NOT_DELETED_VALUE));
		this.row.addFieldValue(
				new FieldValue(this.row.getTable().getContentClockField(), DatabaseDefaults.CLOCK_VALUE_PLACEHOLDER));
		this.row.addFieldValue(
				new FieldValue(this.row.getTable().getDeletedClockField(), DatabaseDefaults.CLOCK_VALUE_PLACEHOLDER));

		this.row.mergeUpdates();

		for(FieldValue fieldValue : this.row.getFieldValues())
			this.prepareValue(fieldValue.getDataField(), fieldValue);

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
	public void createRequestsToCoordinate(CoordinatorRequest request) throws SQLException
	{
		//int counter = 0;

		for(Constraint c : this.row.getContraintsToCheck())
		{
			if (!c.requiresCoordination())
				continue;

			switch(c.getType())
			{
			case AUTO_INCREMENT:
				/*String symbol = SYMBOL_KEY + counter;
				FieldValue fieldValue = this.row.getUpdateFieldValue(c.getFields().get(0).getFieldName());
				RequestValue requestValue = new RequestValue();
				requestValue.setConstraintId(c.getConstraintIdentifier());
				requestValue.setFieldName(fieldValue.getDataField().getFieldName());
				request.addToRequests(requestValue);
				requestValue.setTempSymbol(symbol);
				this.requestValues.add(requestValue);
				this.row.updateFieldValue(new FieldValue(fieldValue.getDataField(), symbol));
				this.isFinal = false;
				counter++;
				if(LOG.isTraceEnabled())
					LOG.trace("new request id entry added for constraint {}", c.getConstraintIdentifier());
				request.setRequiresCoordination(true);
				break;*/
				break;
			case UNIQUE:
				StringBuilder buffer = new StringBuilder();
				Iterator<DataField> it = c.getFields().iterator();
				while(it.hasNext())
				{
					DataField currField = it.next();
					buffer.append(this.row.getUpdateFieldValue(currField.getFieldName()));
					if(it.hasNext())
						buffer.append(",");
				}
				UniqueValue uniqueValue = new UniqueValue(c.getConstraintIdentifier(), buffer.toString());
				request.addToUniqueValues(uniqueValue);
				if(LOG.isTraceEnabled())
					LOG.trace("new unique check entry added for constraint {}", c.getConstraintIdentifier());
				request.setRequiresCoordination(true);
				break;
			case CHECK:
				DataField currField = c.getFields().get(0);
				FieldValue deltafieldValue = this.row.getUpdateFieldValue(currField.getFieldName());

				if(!((CheckConstraint) c).isValidValue(deltafieldValue.getValue()))
					throw new SQLException("field value not valid due to a check constraint restriction");

				ApplyDelta applyDeltaRequest = new ApplyDelta();
				applyDeltaRequest.setConstraintId(c.getConstraintIdentifier());
				applyDeltaRequest.setDeltaValue(deltafieldValue.getValue());
				applyDeltaRequest.setRowId(this.row.getPrimaryKeyValue().getUniqueValue());
				request.addToDeltaValues(applyDeltaRequest);

				if(LOG.isTraceEnabled())
					LOG.trace("new delta check entry added");
				request.setRequiresCoordination(true);
				break;
			case FOREIGN_KEY:
				break;
			default:
				RuntimeUtils.throwRunTimeException("unexpected constraint", ExitCode.UNEXPECTED_OP);
			}
		}
	}

	private void prepareValue(DataField dataField, FieldValue fieldValue)
	{
		if(dataField.isUnique())
		{
			// we cannot touch the value, it must be coordinated later on commit time
			if(dataField.getSemantic() == SemanticPolicy.SEMANTIC)
			{
				if(dataField.isAutoIncrement())
				{
					int nextId = IdentifierFactory.getNextId(dataField);
					fieldValue.setValue(String.valueOf(nextId));
				}
			} else
			{
				if(dataField.isStringField())
					fieldValue.setValue(IdentifierFactory.appendReplicaPrefix(fieldValue.getValue()));
				else if(dataField.isNumberField())
				{
					// does not matter if it has semantic or not, because the generated id will be globally
					// unique
					// and if it has semantic, we exchange this temporary id for a definitive one in the
					// coordinator
					int nextId = IdentifierFactory.getNextId(dataField);
					fieldValue.setValue(String.valueOf(nextId));
				} else
					RuntimeUtils.throwRunTimeException(
							"columns with no semantic value must be either an integer" + " or a string",
							ExitCode.INVALIDUSAGE);
			}
		}
	}
}
