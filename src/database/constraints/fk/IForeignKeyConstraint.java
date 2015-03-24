package database.constraints.fk;


import database.constraints.Constraint;
import database.util.DataField;


/**
 * Created by dnlopes on 24/03/15.
 */
public interface IForeignKeyConstraint extends Constraint
{

	public void addPair(DataField origin, String remote);

	public void setRemoteTable(String table);

	public String getRemoteTable();
}
