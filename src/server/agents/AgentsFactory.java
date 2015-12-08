package server.agents;


import common.util.Environment;
import common.util.exception.InvalidConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.agents.deliver.CausalDeliverAgent;
import server.agents.deliver.DeliverAgent;
import server.agents.deliver.NoOrderDeliverAgent;
import server.agents.dispatcher.BasicDispatcher;
import server.agents.dispatcher.DispatcherAgent;
import server.replicator.Replicator;


/**
 * Created by dnlopes on 28/10/15.
 */
public class AgentsFactory
{

	private static final Logger LOG = LoggerFactory.getLogger(AgentsFactory.class);

	public static DeliverAgent getDeliverAgent(Replicator replicator) throws InvalidConfigurationException
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

	public static DispatcherAgent getDispatcherAgent(Replicator replicator) throws InvalidConfigurationException
	{
		switch(Environment.DISPATCHER_AGENT)
		{
		case 1:
			throw new InvalidConfigurationException("specified dispatcher agent not yet implemented");
		case 2:
			return new BasicDispatcher(replicator);
		case 3:
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
			LOG.error("unknown deliver agent.");
			return "null";
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
			LOG.error("unknown dispatcher agent.");
			return "null";
		}
	}
}
