package network.coordinator;

import net.sf.appia.protocols.total.token.TotalTokenLayer;
import net.sf.appia.protocols.total.token.TotalTokenSession;
import network.AbstractNode;

/**
 * Created by dnlopes on 23/03/15.
 */
public class CoordinatorNetwork extends AbstractNode implements ICoordinatorNetwork
{

	public CoordinatorNetwork(CoordinatorConfig node)
	{
		super(node);
	}
}
