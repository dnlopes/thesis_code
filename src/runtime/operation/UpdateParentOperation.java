package runtime.operation;


import database.constraints.fk.ForeignKeyConstraint;
import database.util.ExecutionPolicy;
import database.util.Row;
import runtime.RuntimeUtils;
import util.ExitCode;
import util.thrift.ThriftShadowTransaction;

import java.util.*;


/**
 * Created by dnlopes on 11/05/15.
 */
public class UpdateParentOperation extends UpdateOperation implements ParentOperation
{

	private Map<ForeignKeyConstraint, List<Row>> childsByConstraint;
	private int numberRows;

	public UpdateParentOperation(int id, ExecutionPolicy policy, Row updatedRow)
	{
		super(id, policy, updatedRow);
		this.childsByConstraint = new HashMap<>();
		this.numberRows = 0;
	}

	@Override
	public void generateStatements(ThriftShadowTransaction shadowTransaction)
	{

		RuntimeUtils.throwRunTimeException("updates cannot have side effects", ExitCode.INVALIDUSAGE);
		//INFO: currently updates cannot have side effects, thus updating a parent is the equivalent of updating a
		// child tuple

		//super.generateStatements(shadowTransaction);

		/*
		List<String> childsUpdates = new ArrayList<>();

		//@info: for now we always update the childs, because some concurrent update may "revert" the changes to the
		// update childs
		for(ForeignKeyConstraint fkConstraint : this.childsByConstraint.keySet())
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append("UPDATE ");
			buffer.append(fkConstraint.getChildTable().getName());
			buffer.append(" SET ");

			Iterator<ParentChildRelation> relationsIt = fkConstraint.getFieldsRelations().iterator();

			while(relationsIt.hasNext())
			{
				ParentChildRelation relation = relationsIt.next();

				boolean filterDeletedParent = false;

				if(fkConstraint.getPolicy().getUpdateAction() == ForeignKeyAction.SET_NULL)
					filterDeletedParent = true;

				String query = QueryCreator.selectFieldFromRow(this.row, relation.getParent(),
						filterDeletedParent);

				buffer.append(relation.getChild().getFieldName());
				buffer.append("=(");
				buffer.append(query);
				buffer.append(")");

				if(relationsIt.hasNext())
					buffer.append(",");
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

			buffer.append(DBDefaults.CLOCK_IS_GREATER_FUNCTION);
			buffer.append("((");
			buffer.append(selectParentClock);
			buffer.append("),");
			buffer.append(DBDefaults.CLOCK_VALUE_PLACEHOLDER);
			buffer.append(")=1");

			childsUpdates.add(buffer.toString());
		}

		super.generateStatements(shadowTransaction);

		for(String update : childsUpdates)
			shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), update);

			*/
	}

	@Override
	public void addSideEffects(ForeignKeyConstraint constraint, List<Row> childs)
	{
		this.childsByConstraint.put(constraint, childs);
		this.numberRows += childs.size();
	}

	@Override
	public int getNumberOfRows()
	{
		return this.numberRows;
	}
}
