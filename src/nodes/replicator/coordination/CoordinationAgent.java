package nodes.replicator.coordination;


import util.thrift.CRDTTransaction;


/**
 * Created by dnlopes on 22/10/15.
 */
public interface CoordinationAgent
{
	void handleCoordination(CRDTTransaction transaction);
}
