package weaql.client.proxy.network;


import weaql.client.execution.TransactionContext;
import weaql.common.thrift.RequestValue;


/**
 * Created by dnlopes on 07/12/15.
 */
public interface WTIProxyNetwork
{
	int requestNextId(RequestValue requestValue);
	String getTransactionClock();
	void sendToRemoteReplicators(TransactionContext txnInfo);
}
