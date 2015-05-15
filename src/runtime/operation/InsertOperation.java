package runtime.operation;


import database.constraints.Constraint;
import database.util.*;
import runtime.RuntimeHelper;
import util.ExitCode;
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
		this.row.updateFieldValue(new FieldValue(this.row.getTable().getDeletedField(), DBDefaults.NOT_DELETED_VALUE));
		this.row.updateFieldValue(new FieldValue(this.row.getTable().getContentClockField(), DBDefaults.CONTENT_CLOCK_PLACEHOLDER));
		this.row.updateFieldValue(new FieldValue(this.row.getTable().getDeletedClockField(), DBDefaults.DELETED_CLOCK_PLACEHOLDER));
		this.row.mergeUpdates();

		StringBuilder buffer = new StringBuilder();

		String insertOrUpdateStatement = OperationTransformer.generateUpdateStatement(this.row);
		buffer.append(insertOrUpdateStatement);
		buffer.append(" AND ");
		String compareClockClause = OperationTransformer.generateContentFunctionClause(this.tablePolicy);
		buffer.append(compareClockClause);

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
