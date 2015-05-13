package runtime.operation;


import database.constraints.fk.ForeignKeyConstraint;
import database.util.Row;

import java.util.List;


/**
 * Created by dnlopes on 13/05/15.
 */
public interface ParentOperation extends Operation
{

	public void addSideEffects(ForeignKeyConstraint constraint, List<Row> childs);
	public int getNumberOfRows();

}
