package network.node;


import database.util.DatabaseMetadata;
import util.defaults.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Created by dnlopes on 22/03/15.
 */
public class Coordinator extends AbstractNode
{

	private DatabaseMetadata databaseMetadata;

	private Map<String, Set<String>> uniques;
	private Map<String, Long> autoIncremented;

	public Coordinator(NodeMetadata nodeInfo)
	{
		super(nodeInfo);
		databaseMetadata = Configuration.getInstance().getDatabaseMetadata();
		this.uniques = new HashMap<>();
		this.autoIncremented = new HashMap<>();
	}





}
