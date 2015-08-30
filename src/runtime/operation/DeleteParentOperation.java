package runtime.operation;


import database.constraints.fk.ForeignKeyAction;
import database.constraints.fk.ForeignKeyConstraint;
import database.constraints.fk.ParentChildRelation;
import database.util.ExecutionPolicy;
import database.util.Row;
import runtime.RuntimeUtils;
import runtime.transformer.QueryCreator;
import util.ExitCode;
import util.defaults.DBDefaults;
import util.thrift.ThriftShadowTransaction;

import java.util.*;


/**
 * Created by dnlopes on 13/05/15.
 */
public class DeleteParentOperation extends DeleteOperation implements ParentOperation
{

	private Map<ForeignKeyConstraint, List<Row>> childsByConstraint;
	private int numberRows;
	private List<Row> childs;

	public DeleteParentOperation(int id, ExecutionPolicy policy, Row deletedRow)
	{
		super(id, policy, deletedRow);
		this.childsByConstraint = new HashMap<>();
		this.numberRows = 0;
		this.childs = new LinkedList<>();
	}

	@Override
	public void generateStatements(ThriftShadowTransaction shadowTransaction)
	{
		RuntimeUtils.throwRunTimeException("missing implementation", ExitCode.MISSING_IMPLEMENTATION);
		//TODO: missing implementation

		/*
		List<String> childsUpdates = new ArrayList<>();

		// delete parent row
		super.generateStatements(shadowTransaction);

		// delete childs
		for(ForeignKeyConstraint fkConstraint : this.childsByConstraint.keySet())
		{
			Iterator<ParentChildRelation> relationsIt = fkConstraint.getFieldsRelations().iterator();

			StringBuilder buffer = new StringBuilder();
			buffer.append("UPDATE ");
			buffer.append(fkConstraint.getChildTable().getName());
			buffer.append(" SET ");
			buffer.append(DBDefaults.DELETED_CLOCK_COLUMN);
			buffer.append("=");
			buffer.append(DBDefaults.CLOCK_VALUE_PLACEHOLDER);
			buffer.append(",");

			//if cascade, just set visibility flag to '1'
			if(fkConstraint.getPolicy().getDeleteAction() == ForeignKeyAction.CASCADE)
				buffer.append(SET_DELETED_EXPRESSION);
			else // if set null, update all foreign key fields with the proper query
			{
				while(relationsIt.hasNext())
				{
					ParentChildRelation relation = relationsIt.next();
					String query = QueryCreator.selectFieldFromRow(this.row, relation.getParent(), true);

					buffer.append(relation.getChild().getFieldName());
					buffer.append("=(");
					buffer.append(query);
					buffer.append(")");

					if(relationsIt.hasNext())
						buffer.append(",");
				}
			}

			relationsIt = fkConstraint.getFieldsRelations().iterator();
			buffer.append(" WHERE ");

			while(relationsIt.hasNext())
			{
				ParentChildRelation relation = relationsIt.next();
				buffer.append(relation.getChild().getFieldName());
				buffer.append("=");
				buffer.append(this.row.getFieldValue(relation.getParent().getFieldName()).getFormattedValue());
				buffer.append(" AND ");
			}

			String selectParentClock = QueryCreator.selectFieldFromRow(this.row,
					this.row.getTable().getField(DBDefaults.CONTENT_CLOCK_COLUMN), false);

			if(fkConstraint.getPolicy().getExecutionPolicy() == ExecutionPolicy.UPDATEWINS)
				buffer.append(DBDefaults.CLOCK_IS_GREATER_FUNCTION);
			else
				buffer.append(DBDefaults.CLOCKS_IS_CONCURRENT_OR_GREATER_FUNCTION);

			buffer.append("((");
			buffer.append(selectParentClock);
			buffer.append("),");
			buffer.append(DBDefaults.CLOCK_VALUE_PLACEHOLDER);
			buffer.append(")=1");

			childsUpdates.add(buffer.toString());
		}

		for(String update : childsUpdates)
			shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), update);
                          */
	}

	@Override
	public void addSideEffects(ForeignKeyConstraint constraint, List<Row> childs)
	{
		this.childsByConstraint.put(constraint, childs);
		this.childs.addAll(childs);
	}

	@Override
	public int getNumberOfRows()
	{
		return this.numberRows;
	}

}
