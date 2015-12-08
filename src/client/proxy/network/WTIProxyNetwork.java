package client.proxy.network;


import client.execution.TransactionContext;
import common.thrift.RequestValue;


/**
 * Created by dnlopes on 07/12/15.
 */
public interface WTIProxyNetwork
{
	int requestNextId(RequestValue requestValue);
	String getTransactionClock();
	void sendToRemoteReplicators(TransactionContext txnInfo);
}
