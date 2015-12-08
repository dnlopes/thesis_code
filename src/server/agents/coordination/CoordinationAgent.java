package server.agents.coordination;


import common.thrift.CRDTPreCompiledTransaction;


/**
 * Created by dnlopes on 22/10/15.
 */
public interface CoordinationAgent
{
	void handleCoordination(CRDTPreCompiledTransaction transaction);
}
