/********************************************************************
 Copyright (c) 2013 chengli.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the GNU Public License v2.0
 which accompanies this distribution, and is available at
 http://www.gnu.org/licenses/old-licenses/gpl-2.0.html

 Contributors:
 chengli - initial API and implementation

 Contact:
 To distribute or use this code requires prior specific permission.
 In this case, please contact chengli@mpi-sws.org.
 ********************************************************************/
/**
 *
 */

package runtime.operation;

import java.util.ArrayList;
import java.util.List;

import database.invariants.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.Transaction;


/**
 * The Class ShadowOperation.
 */
public class ShadowOperation
{

	static final Logger LOG = LoggerFactory.getLogger(ShadowOperation.class);

	private List<String> operationList;
	private List<Value> checkList;
	private Transaction transaction;

	public ShadowOperation(List<String> operations)
	{
		this.operationList = operations;
	}

	/**
	 * Instantiates a new shadow op template.
	 */
	public ShadowOperation(Transaction txn)
	{
		this.operationList = new ArrayList<>();
		this.checkList = new ArrayList<>();
		this.transaction = txn;
	}

	public List<String> getOperationList()
	{
		return this.operationList;
	}

	public void addCheckValue(Value value)
	{
		this.checkList.add(value);
	}

	public Transaction getTransaction()
	{
		return this.transaction;
	}
}
