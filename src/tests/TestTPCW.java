package tests;


import database.jdbc.ConnectionFactory;
import util.defaults.DBDefaults;
import util.props.DatabaseProperties;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created by dnlopes on 30/03/15.
 */
public class TestTPCW
{

	public static void main(String args[]) throws SQLException, ClassNotFoundException
	{
		Connection connection = ConnectionFactory.getConnection(
				new DatabaseProperties(DBDefaults.CRDT_TPCW_PROPERTIES));

		String sstring = "INSERT into shopping_cart (sc_id, sc_time) VALUES (8817,CURRENT_TIMESTAMP)";
		Statement s = connection.createStatement();
		s.executeUpdate(sstring);

	}
}
