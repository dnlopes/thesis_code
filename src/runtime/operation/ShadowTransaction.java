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

import java.util.List;

import runtime.LogicalClock;


/**
 * The Class ShadowOperation.
 */
public class ShadowTransaction
{

	private List<String> operationList;
	private int txnId;
	private LogicalClock clock;
	private int replicatorId;

	public ShadowTransaction(int txnId, List<String> operations)
	{
		this.txnId = txnId;
		this.operationList = operations;
	}

	public List<String> getOperationList()
	{
		return this.operationList;
	}

	public int getTxnId()
	{
		return this.txnId;
	}

	public void setLogicalClock(LogicalClock clock)
	{
		this.clock = clock;
	}

	public LogicalClock getClock()
	{
		return this.clock;
	}

	public int getReplicatorId()
	{
		return this.replicatorId;
	}

	public void setReplicatorId(int replicatorId)
	{
		this.replicatorId = replicatorId;
	}
}
