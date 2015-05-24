package nodes;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by dnlopes on 21/03/15.
 */
public abstract class AbstractNetwork
{

	private static final Logger LOG = LoggerFactory.getLogger(AbstractNetwork.class);

	protected AbstractNodeConfig me;

	public AbstractNetwork(AbstractNodeConfig node)
	{
		this.me = node;
	}
}
