package client.proxy.hook;


import client.log.TransactionLog;
import client.proxy.Proxy;
import common.util.Topology;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by dnlopes on 09/12/15.
 */
public class TransactionsLogWritter extends Thread
{

	private List<Proxy> proxies;

	public TransactionsLogWritter()
	{
		proxies = new LinkedList<>();
	}

	public void addProxy(Proxy proxy)
	{
		proxies.add(proxy);
	}

	@Override
	public void run()
	{
		StringBuilder buffer = new StringBuilder();

		for(Proxy proxy : proxies)
		{
			TransactionLog txnLog = proxy.getTransactionLog();
			if(txnLog == null)
				continue;

			buffer.append(txnLog.toString());
		}

		PrintWriter out;
		try
		{
			String fileName = Topology.getInstance().getReplicatorsCount() + "_replicas_" + proxies.size() +
					"_clients.log";

			out = new PrintWriter(fileName);
			out.write(buffer.toString());
			out.close();
		} catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
}
