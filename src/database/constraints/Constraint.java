package database.constraints;


import database.util.DataField;

import java.util.List;


/**
 * Created by dnlopes on 24/03/15.
 */
public interface Constraint
{

	public List<DataField> getFields();

	public ConstraintType getType();

	public void addField(DataField field);

}
