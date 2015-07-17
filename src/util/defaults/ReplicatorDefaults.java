package util.defaults;


/**
 * Created by dnlopes on 17/07/15.
 */
public class ReplicatorDefaults
{

	private static final String COORDINATOR_CONNECTIONS_DEFAULT = "100";

	public static final int COORDINATOR_CONNECTIONS = Integer.parseInt(
			System.getProperty("coord.coonections", COORDINATOR_CONNECTIONS_DEFAULT));

}
