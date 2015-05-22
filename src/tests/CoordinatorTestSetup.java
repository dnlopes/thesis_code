package tests;


import network.coordinator.Coordinator;
import org.xml.sax.SAXException;
import runtime.txn.Transaction;
import runtime.txn.TransactionIdentifier;
import util.defaults.Configuration;

import java.io.IOException;
import java.sql.SQLException;


/**
 * Created by dnlopes on 25/03/15.
 */
public class CoordinatorTestSetup
{

	public static void main(String args[]) throws SQLException, IOException, SAXException, ClassNotFoundException
	{

		//Coordinator coordinator = new Coordinator(Configuration.getInstance().getCoordinatorConfigWithIndex(1));
		Transaction txn = new Transaction(new TransactionIdentifier(1));
	}
}
