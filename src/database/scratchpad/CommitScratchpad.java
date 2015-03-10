package database.scratchpad;

import runtime.Operation;


/**
 * Created by dnlopes on 10/03/15.
 */
public interface CommitScratchpad
{

	/**
	 * Returns the operation associated with this scratchpad
	 */
	public Operation getOperation();

	public void setOperation(Operation op);

	/**
	 * Commits the operation to the database
	 */
	public void commit();

}
