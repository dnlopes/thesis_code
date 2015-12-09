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
		String jdbc = System.getProperty("jdbc");
		String emulatorId = System.getProperty("emulatorId");

		StringBuilder buffer = new StringBuilder();

		buffer.append("proxyid").append(",");
		buffer.append("exec").append(",");
		buffer.append("commit").append(",");
		buffer.append("inserts").append(",");
		buffer.append("updates").append(",");
		buffer.append("deletes").append(",");
		buffer.append("selects").append(",");
		buffer.append("parsing").append(",");
		buffer.append("generate_crdt").append(",");
		buffer.append("load_from_main").append("\n");

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
			String fileName = Topology.getInstance().getReplicatorsCount() + "_replicas_" + proxies.size() * Topology
					.getInstance().getReplicatorsCount() + "_users_" + jdbc + "_jdbc_emulator" + emulatorId +
					"_txns.csv";

			out = new PrintWriter(fileName);
			out.write(buffer.toString());
			out.close();
		} catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
}
