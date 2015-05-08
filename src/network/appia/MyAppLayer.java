package network.appia;


import net.sf.appia.core.Session;
import net.sf.appia.test.appl.ApplLayer;


/**
 * Created by dnlopes on 24/04/15.
 */
public class MyAppLayer extends ApplLayer
{


	public Session createSession() {
		return new MyApplSession(this);
	}

	
}
