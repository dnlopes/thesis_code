package database.constraints.fk;


import database.constraints.Constraint;
import database.util.DataField;
import database.util.DatabaseTable;


/**
 * Created by dnlopes on 24/03/15.
 */
public interface IForeignKeyConstraint extends Constraint
{

	public void setParentTable(DatabaseTable table);
	public void setChildTable(DatabaseTable childTable);

	public DatabaseTable getParentTable();
}
