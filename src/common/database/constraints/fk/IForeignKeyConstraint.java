package common.database.constraints.fk;


import common.database.constraints.Constraint;
import common.database.table.DatabaseTable;


/**
 * Created by dnlopes on 24/03/15.
 */
public interface IForeignKeyConstraint extends Constraint
{

	public void setParentTable(DatabaseTable table);
	public void setChildTable(DatabaseTable childTable);

	public DatabaseTable getParentTable();
}
