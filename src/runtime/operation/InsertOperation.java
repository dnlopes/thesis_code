package runtime.operation;


import database.constraints.Constraint;
import database.util.*;
import runtime.RuntimeHelper;
import util.ExitCode;
import util.thrift.*;

import java.util.*;


/**
 * Created by dnlopes on 11/05/15.
 */
public class InsertOperation extends AbstractOperation implements Operation
{

	public InsertOperation(int id, ExecutionPolicy policy, Row newRow)
	{
		super(id, policy, OperationType.INSERT, newRow);
	}

	@Override
	public void generateOperationStatements(List<String> shadowStatements)
	{
		StringBuilder buffer = new StringBuilder();
		StringBuilder valuesBuffer = new StringBuilder();

		buffer.append("INSERT INTO ");
		buffer.append(this.row.getTable().getName());
		buffer.append(" (");

		Iterator<FieldValue> fieldsValuesIt = this.row.getFieldValues().iterator();

		while(fieldsValuesIt.hasNext())
		{
			FieldValue fValue = fieldsValuesIt.next();

			// we add by hand the hidden columns
			if(fValue.getDataField().isHiddenField())
				continue;

			buffer.append(fValue.getDataField().getFieldName());
			valuesBuffer.append(fValue.getValue());

			if(fieldsValuesIt.hasNext())
			{
				buffer.append(",");
				valuesBuffer.append(",");
			}
		}

		// add hidden columns by hand
		buffer.append(") VALUES (");
		buffer.append(valuesBuffer.toString());
		buffer.append(")");

		shadowStatements.add(buffer.toString());
	}

	@Override
	public void createRequestsToCoordinate(CoordinatorRequest request)
	{
		for(Constraint c : this.row.getTable().getTableInvarists())
		{
			switch(c.getType())
			{
			case AUTO_INCREMENT:
				FieldValue fieldValue = this.row.getFieldValue(c.getFields().get(0).getFieldName());
				RequestValue requestValue = new RequestValue();
				requestValue.setConstraintId(c.getConstraintIdentifier());
				requestValue.setFieldName(fieldValue.getDataField().getFieldName());
				requestValue.setOpId(this.id);
				request.addToRequests(requestValue);
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
				LOG.trace("new unique check entry added for constraint {}", c.getConstraintIdentifier());
				break;
			case FOREIGN_KEY:
				break;
			default:
				RuntimeHelper.throwRunTimeException("unexpected constraint", ExitCode.UNEXPECTED_OP);
			}
		}
	}
}
