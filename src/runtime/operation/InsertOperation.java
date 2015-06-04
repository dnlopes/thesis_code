package runtime.operation;


import database.constraints.Constraint;
import database.util.*;
import runtime.OperationTransformer;
import runtime.RuntimeUtils;
import util.ExitCode;
import util.defaults.Configuration;
import util.defaults.DBDefaults;
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
		this.row.addFieldValue(new FieldValue(this.row.getTable().getDeletedField(), DBDefaults.NOT_DELETED_VALUE));
		this.row.addFieldValue(
				new FieldValue(this.row.getTable().getContentClockField(), DBDefaults.CLOCK_VALUE_PLACEHOLDER));
		this.row.addFieldValue(
				new FieldValue(this.row.getTable().getDeletedClockField(), DBDefaults.CLOCK_VALUE_PLACEHOLDER));

		StringBuilder buffer = new StringBuilder();

		String insertOrUpdateStatement = OperationTransformer.generateInsertStatement(this.row);
		buffer.append(insertOrUpdateStatement);
		shadowStatements.add(buffer.toString());
	}

	@Override
	public void createRequestsToCoordinate(CoordinatorRequest request)
	{
		for(Constraint c : this.row.getContraintsToCheck())
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
			default:
				RuntimeUtils.throwRunTimeException("unexpected constraint", ExitCode.UNEXPECTED_OP);
			}
		}
	}
}
