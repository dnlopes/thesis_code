package runtime.operation;


import database.constraints.fk.ForeignKeyAction;
import database.constraints.fk.ForeignKeyConstraint;
import database.constraints.fk.ParentChildRelation;
import database.util.ExecutionPolicy;
import database.util.Row;
import runtime.transformer.QueryCreator;
import util.defaults.DBDefaults;
import util.thrift.ThriftShadowTransaction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


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
		if(this.row.hasSideEffects())
		{
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
					buffer.append(relation.getChild().getFieldName());
					buffer.append("=");

					if(fkConstraint.getPolicy().getUpdateAction() == ForeignKeyAction.SET_NULL)
						buffer.append("NULL");

					else
					{
						if(this.row.containsNewField(relation.getParent().getFieldName()))
							buffer.append(this.row.getUpdateFieldValue(
									relation.getParent().getFieldName()).getFormattedValue());
						else
							buffer.append(
									this.row.getFieldValue(relation.getParent().getFieldName()).getFormattedValue());
					}

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
						this.row.getTable().getField(DBDefaults.CONTENT_CLOCK_COLUMN));

				buffer.append(DBDefaults.COMPARE_CLOCK_FUNCTION);
				buffer.append("((");
				buffer.append(selectParentClock);
				buffer.append("),");
				buffer.append(DBDefaults.CLOCK_VALUE_PLACEHOLDER);
				buffer.append(")");
				buffer.append(" >= 0");

				shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), buffer.toString());
			}
		}

		super.generateStatements(shadowTransaction);

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
