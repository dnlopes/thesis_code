package server.agents.coordination;


import common.thrift.CRDTPreCompiledTransaction;
import common.util.exception.InvalidConfigurationException;
import common.util.exception.SocketConnectionException;
import server.util.CompilePreparationException;
import server.util.CoordinationFailureException;


/**
 * Created by dnlopes on 22/10/15.
 */
public interface CoordinationAgent
{
	void handleCoordination(CRDTPreCompiledTransaction transaction)
			throws CompilePreparationException, CoordinationFailureException, SocketConnectionException, InvalidConfigurationException;
}
