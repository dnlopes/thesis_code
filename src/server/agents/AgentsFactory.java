package server.agents;


import common.util.Environment;
import common.util.exception.InvalidConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.agents.deliver.CausalDeliverAgent;
import server.agents.deliver.DeliverAgent;
import server.agents.deliver.NoOrderDeliverAgent;
import server.agents.dispatcher.BasicDispatcher;
import server.agents.dispatcher.BatchDispatcher;
import server.agents.dispatcher.DispatcherAgent;
import server.replicator.Replicator;


/**
 * Created by dnlopes on 28/10/15.
 */
public class AgentsFactory
{

	private static final Logger LOG = LoggerFactory.getLogger(AgentsFactory.class);

	public static DeliverAgent createDeliverAgent(Replicator replicator) throws InvalidConfigurationException
	{
		switch(Environment.DELIVER_AGENT)
		{
		case 1:
			return new CausalDeliverAgent(replicator);
		case 2:
			return new NoOrderDeliverAgent(replicator);
		default:
			throw new InvalidConfigurationException("unknown deliver agent class");
		}
	}

	public static DispatcherAgent createDispatcherAgent(Replicator replicator) throws InvalidConfigurationException
	{
		// 1=BatchDispatcher, 2=BasicDispatcher, 3=AggregatorDispatcher
		switch(Environment.DISPATCHER_AGENT)
		{
		case 1:
			return new BatchDispatcher(replicator);
		case 2:
			return new BasicDispatcher(replicator);
		case 3:
			//TODO
			throw new InvalidConfigurationException("specified dispatcher agent not yet implemented");
		default:
			throw new InvalidConfigurationException("unknown dispatcher agent class");
		}
	}

	public static String getDeliverAgentClassAsString()
	{
		switch(Environment.DELIVER_AGENT)
		{
		case 1:
			return "CausalDeliverAgent";
		case 2:
			return "NoOrderDeliverAgent";
		default:
			LOG.error("unkown deliver agent class");
			return null;
		}
	}

	public static String getDispatcherAgentClassAsString()
	{
		switch(Environment.DISPATCHER_AGENT)
		{
		case 1:
			return "BatchDispatcher";
		case 2:
			return "BasicDispatcher";
		case 3:
			return "AggregatorDispatcher";
		default:
			LOG.error("unkown dispatcher agent class");
			return null;
		}
	}
}
