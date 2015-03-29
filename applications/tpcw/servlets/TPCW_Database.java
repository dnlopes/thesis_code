/* 
 * TPCW_Database.java - Contains all of the code involved with database
 *                      accesses, including all of the JDBC calls. These
 *                      functions are called by many of the servlets.
 *
 ************************************************************************
 *
 * This is part of the the Java TPC-W distribution,
 * written by Harold Cain, Tim Heil, Milo Martin, Eric Weglarz, and Todd
 * Bezenek.  University of Wisconsin - Madison, Computer Sciences
 * Dept. and Dept. of Electrical and Computer Engineering, as a part of
 * Prof. Mikko Lipasti's Fall 1999 ECE 902 course.
 *
 * Copyright (C) 1999, 2000 by Harold Cain, Timothy Heil, Milo Martin, 
 *                             Eric Weglarz, Todd Bezenek.
 *
 * This source code is distributed "as is" in the hope that it will be
 * useful.  It comes with no warranty, and no author or distributor
 * accepts any responsibility for the consequences of its use.
 *
 * Everyone is granted permission to copy, modify and redistribute
 * this code under the following conditions:
 *
 * This code is distributed for non-commercial use only.
 * Please contact the maintainer for restrictions applying to 
 * commercial use of these tools.
 *
 * Permission is granted to anyone to make or distribute copies
 * of this code, either as received or modified, in any
 * medium, provided that all copyright notices, permission and
 * nonwarranty notices are preserved, and that the distributor
 * grants the recipient permission for further redistribution as
 * permitted by this document.
 *
 * Permission is granted to distribute this code in compiled
 * or executable form under the same conditions that apply for
 * source code, provided that either:
 *
 * A. it is accompanied by the corresponding machine-readable
 *    source code,
 * B. it is accompanied by a written offer, with no time limit,
 *    to give anyone a machine-readable copy of the corresponding
 *    source code in return for reimbursement of the cost of
 *    distribution.  This written offer must permit verbatim
 *    duplication by anyone, or
 * C. it is distributed by someone who received only the
 *    executable form, and is accompanied by a copy of the
 *    written offer of source code that they received concurrently.
 *
 * In other words, you are welcome to use, share and improve this codes.
 * You are forbidden to forbid anyone else to use, share and improve what
 * you give them.
 *
 ************************************************************************
 *
 * Changed 2003 by Jan Kiefer.
 *
 ************************************************************************/

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.lang.Math.*;
import java.util.*;
import java.sql.Date;
import java.sql.Timestamp;

public class TPCW_Database {
	private static Void VOID = null;

	static String driverName = "database.jdbc.CRDTDriver";
	static String jdbcPath = "jdbc:crdt://localhost:3306/tpcw";
	// Pool of *available* connections.
	static Vector availConn = new Vector(0);
	static int checkedOut = 0;
	static int totalConnections = 0;
	static int createdConnections = 0;
	static int closedConnections = 0;

	//    private static final boolean use_connection_pool = false;
	private static final boolean use_connection_pool = true;
	public static final int maxConn = 50;

	// Here's what the db line looks like for postgres
	//public static final String url = "jdbc:postgresql://eli.ece.wisc.edu/tpcwb";


	// Get a connection from the pool.
	public static synchronized Connection getConnection() {
		if (!use_connection_pool) {
			return getNewConnection();
		} else {
			Connection con = null;
			while (availConn.size() > 0) {
				// Pick the first Connection in the Vector
				// to get round-robin usage
				con = (Connection) availConn.firstElement();
				availConn.removeElementAt(0);
				try {
					if (con.isClosed()) {
						continue;
					}
				}
				catch (SQLException e) {
					e.printStackTrace();
					continue;
				}

				// Got a connection.
				checkedOut++;
				try { con.setAutoCommit(false); } catch (SQLException e) { return null; }
				return(con);
			}

			if (maxConn == 0 || checkedOut < maxConn) {
				con = getNewConnection();
				totalConnections++;
			}


			if (con != null) {
				checkedOut++;
			}

			try { con.setAutoCommit(false); } catch (SQLException e) { return null; }
			return con;
		}
	}

	// Return a connection to the pool.
	public static synchronized void returnConnection(Connection con)
			throws java.sql.SQLException
	{
		if (!use_connection_pool) {
			con.close();
		} else {
			checkedOut--;
			availConn.addElement(con);
		}
	}

	// Get a new connection to DB2
	public static Connection getNewConnection() {
		try {
			Class.forName(driverName);
			// Class.forName("postgresql.Driver");

			// Create URL for specifying a DBMS
			Connection con;
			while(true) {
				try {
					con = DriverManager.getConnection(jdbcPath, "sa", "101010");
					//con = DriverManager.getConnection(jdbcPath);
					break;
				} catch (java.sql.SQLException ex) {
					System.err.println("Error getting connection: " +
							ex.getMessage() + " : " +
							ex.getErrorCode() +
							": trying to get connection again.");
					ex.printStackTrace();
					java.lang.Thread.sleep(1000);
				}
			}
			con.setAutoCommit(false);
			createdConnections++;
			return con;
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static <T> T withTransaction(tx.TransactionalCommand<T> command) {
		try {
			while (true) {
				Connection con = getConnection();
				boolean txFinished = false;
				try {
					T result = command.doIt(con);
					con.commit();
					returnConnection(con);
					txFinished = true;
					return result;
				} catch (SQLException sqle) {
					// System.out.println("Restarting TX because of a database problem (hopefully just a conflict)");
					con.rollback();
					returnConnection(con);
					txFinished = true;
				} finally {
					if (!txFinished) {
						con.rollback();
						returnConnection(con);
					}
				}
			}
		} catch (SQLException sqle) {
			// exception occurred either rolling back or releasing resources.  Not much we can do here
			throw new RuntimeException(sqle);
		}
	}

	public static String[] getName(final int c_id) {
		String[] name = withTransaction(new tx.TransactionalCommand<String[]>() {
			public String[] doIt(Connection con) throws SQLException {
				return getName(con, c_id);
			}
		});
		return name;
	}
	private static String[] getName(Connection con, int c_id) throws SQLException {
		String name[] = new String[2];
		PreparedStatement get_name = con.prepareStatement
				("SELECT c_fname,c_lname FROM customer WHERE c_id = ?");

		// Set parameter
		get_name.setInt(1, c_id);
		// 	    out.println("About to execute query!");
		//            out.flush();

		ResultSet rs = get_name.executeQuery();

		// Results
		rs.next();
		name[0] = rs.getString("c_fname");
		name[1] = rs.getString("c_lname");
		rs.close();
		get_name.close();
		return name;
	}

	public static Book getBook(final int i_id) {
		Book book = withTransaction(new tx.TransactionalCommand<Book>() {
			public Book doIt(Connection con) throws SQLException {
				return getBook(con, i_id);
			}
		});
		return book;
	}
	private static Book getBook(Connection con, int i_id) throws SQLException {
		Book book = null;
		PreparedStatement statement = con.prepareStatement
				("SELECT * FROM item,author WHERE item.i_a_id = author.a_id AND i_id = ?");

		// Set parameter
		statement.setInt(1, i_id);
		ResultSet rs = statement.executeQuery();

		// Results
		rs.next();
		book = new Book(rs);
		rs.close();
		statement.close();
		return book;
	}

	public static Customer getCustomer(final String UNAME){
		Customer customer = withTransaction(new tx.TransactionalCommand<Customer>() {
			public Customer doIt(Connection con) throws SQLException {
				return getCustomer(con, UNAME);
			}
		});
		return customer;
	}
	private static Customer getCustomer(Connection con, String UNAME) throws SQLException {
		Customer cust = null;
		PreparedStatement statement = con.prepareStatement
				("SELECT * FROM customer, address, country WHERE customer.c_addr_id = address.addr_id AND address.addr_co_id = country.co_id AND customer.c_uname = ?");

		// Set parameter
		statement.setString(1, UNAME);
		ResultSet rs = statement.executeQuery();

		// Results
		if(rs.next())
			cust = new Customer(rs);
		else {
			System.err.println("ERROR: NULL returned in getCustomer!");
			rs.close();
			statement.close();
			return null;
		}

		statement.close();
		return cust;
	}

	public static Vector doSubjectSearch(final String search_key) {
		Vector Vector = withTransaction(new tx.TransactionalCommand<Vector>() {
			public Vector doIt(Connection con) throws SQLException {
				return doSubjectSearch(con, search_key);
			}
		});
		return Vector;
	}
	private static Vector doSubjectSearch(Connection con, String search_key) throws SQLException {
		Vector vec = new Vector();
		PreparedStatement statement = con.prepareStatement
				("SELECT * FROM item, author WHERE item.i_a_id = author.a_id AND item.i_subject = ? ORDER BY item.i_title limit 50");

		// Set parameter
		statement.setString(1, search_key);
		ResultSet rs = statement.executeQuery();

		// Results
		while(rs.next()) {
			vec.addElement(new Book(rs));
		}
		rs.close();
		statement.close();
		return vec;
	}

	public static Vector doTitleSearch(final String search_key) {
		Vector vector = withTransaction(new tx.TransactionalCommand<Vector>() {
			public Vector doIt(Connection con) throws SQLException {
				return doTitleSearch(con, search_key);
			}
		});
		return vector;
	}
	private static Vector doTitleSearch(Connection con, String search_key) throws SQLException {
		Vector vec = new Vector();
		PreparedStatement statement = con.prepareStatement
				("SELECT * FROM item, author WHERE item.i_a_id = author.a_id AND substring(soundex(item.i_title),1,4)=substring(soundex(?),1,4) ORDER BY item.i_title limit 50");

		// Set parameter
		statement.setString(1, search_key+"%");
		ResultSet rs = statement.executeQuery();

		// Results
		while(rs.next()) {
			vec.addElement(new Book(rs));
		}
		rs.close();
		statement.close();
		return vec;
	}

	public static Vector doAuthorSearch(final String search_key) {
		Vector vector = withTransaction(new tx.TransactionalCommand<Vector>() {
			public Vector doIt(Connection con) throws SQLException {
				return doAuthorSearch(con, search_key);
			}
		});
		return vector;
	}
	private static Vector doAuthorSearch(Connection con, String search_key) throws SQLException {
		Vector vec = new Vector();
		PreparedStatement statement = con.prepareStatement
				("SELECT * FROM author, item WHERE substring(soundex(author.a_lname),1,4)=substring(soundex(?),1,4) AND item.i_a_id = author.a_id ORDER BY item.i_title limit 50");

		// Set parameter
		statement.setString(1, search_key+"%");
		ResultSet rs = statement.executeQuery();

		// Results
		while(rs.next()) {
			vec.addElement(new Book(rs));
		}
		rs.close();
		statement.close();
		return vec;
	}

	public static Vector getNewProducts(final String subject) {
		Vector vector = withTransaction(new tx.TransactionalCommand<Vector>() {
			public Vector doIt(Connection con) throws SQLException {
				return getNewProducts(con, subject);
			}
		});
		return vector;
	}
	private static Vector getNewProducts(Connection con, String subject) throws SQLException {
		Vector vec = new Vector();  // Vector of Books
		PreparedStatement statement = con.prepareStatement
				("SELECT i_id, i_title, a_fname, a_lname " +"FROM item, author " +"WHERE item.i_a_id = author.a_id " +"AND item.i_subject = ? " +"ORDER BY item.i_pub_date DESC,item.i_title " +"limit 50");

		// Set parameter
		statement.setString(1, subject);
		ResultSet rs = statement.executeQuery();

		// Results
		while(rs.next()) {
			vec.addElement(new ShortBook(rs));
		}
		rs.close();
		statement.close();
		return vec;
	}

	public static Vector getBestSellers(final String subject) {
		Vector vector = withTransaction(new tx.TransactionalCommand<Vector>() {
			public Vector doIt(Connection con) throws SQLException {
				return getBestSellers(con, subject);
			}
		});
		return vector;
	}
	private static Vector getBestSellers(Connection con, String subject) throws SQLException {
		Vector vec = new Vector();  // Vector of Books
		//The following is the original, unoptimized best sellers query.
		PreparedStatement statement = con.prepareStatement
				("SELECT i_id, i_title, a_fname, a_lname " +"FROM item, author, order_line " +"WHERE item.i_id = order_line.ol_i_id " +"AND item.i_a_id = author.a_id " +"AND order_line.ol_o_id > (SELECT MAX(o_id)-3333 FROM orders) " +"AND item.i_subject = ? " +"GROUP BY i_id, i_title, a_fname, a_lname " +"ORDER BY SUM(ol_qty) DESC " +"limit 50");
		//This is Mikko's optimized version, which depends on the fact that
		//A table named "bestseller" has been created.
	    /*PreparedStatement statement = con.prepareStatement
		("SELECT bestseller.i_id, i_title, a_fname, a_lname, ol_qty " + 
		 "FROM item, bestseller, author WHERE item.i_subject = ?" +
		 " AND item.i_id = bestseller.i_id AND item.i_a_id = author.a_id " + 
		 " ORDER BY ol_qty DESC FETCH FIRST 50 ROWS ONLY");*/

		// Set parameter
		statement.setString(1, subject);
		ResultSet rs = statement.executeQuery();

		// Results
		while(rs.next()) {
			vec.addElement(new ShortBook(rs));
		}
		rs.close();
		statement.close();
		return vec;
	}

	public static void getRelated(final int i_id, final Vector i_id_vec, final Vector i_thumbnail_vec) {
		withTransaction(new tx.TransactionalCommand<Void>() {
			public Void doIt(Connection con) throws SQLException {
				getRelated(con, i_id, i_id_vec, i_thumbnail_vec);
				return VOID;
			}
		});
	}
	private static void getRelated(Connection con, int i_id, Vector i_id_vec, Vector i_thumbnail_vec) throws SQLException {
		PreparedStatement statement = con.prepareStatement
				("SELECT J.i_id,J.i_thumbnail from item I, item J where (I.i_related1 = J.i_id or I.i_related2 = J.i_id or I.i_related3 = J.i_id or I.i_related4 = J.i_id or I.i_related5 = J.i_id) and I.i_id = ?");

		// Set parameter
		statement.setInt(1, i_id);
		ResultSet rs = statement.executeQuery();

		// Clear the vectors
		i_id_vec.removeAllElements();
		i_thumbnail_vec.removeAllElements();

		// Results
		while(rs.next()) {
			i_id_vec.addElement(new Integer(rs.getInt(1)));
			i_thumbnail_vec.addElement(rs.getString(2));
		}
		rs.close();
		statement.close();
	}

	public static void adminUpdate(final int i_id, final double cost, final String image, final String thumbnail) {
		withTransaction(new tx.TransactionalCommand<Void>() {
			public Void doIt(Connection con) throws SQLException {
				adminUpdate(con, i_id, cost, image, thumbnail);
				return VOID;
			}
		});
	}
	private static void adminUpdate(Connection con, int i_id, double cost, String image, String thumbnail) throws SQLException {
		PreparedStatement statement = con.prepareStatement
				("UPDATE item SET i_cost = ?, i_image = ?, i_thumbnail = ?, i_pub_date = CURRENT_DATE WHERE i_id = ?");

		// Set parameter
		statement.setDouble(1, cost);
		statement.setString(2, image);
		statement.setString(3, thumbnail);
		statement.setInt(4, i_id);
		statement.executeUpdate();
		statement.close();
		PreparedStatement related = con.prepareStatement
				("SELECT ol_i_id " +"FROM orders, order_line " + "WHERE orders.o_id = order_line.ol_o_id " +"AND NOT (order_line.ol_i_id = ?) " +"AND orders.o_c_id IN (SELECT o_c_id " +"                      FROM orders, order_line " +"                      WHERE orders.o_id = order_line.ol_o_id " +"                      AND orders.o_id > (SELECT MAX(o_id)-10000 FROM orders)" + "                      AND order_line.ol_i_id = ?) " +"GROUP BY ol_i_id " +"ORDER BY SUM(ol_qty) DESC " +"limit 5");

		// Set parameter
		related.setInt(1, i_id);
		related.setInt(2, i_id);
		ResultSet rs = related.executeQuery();

		int[] related_items = new int[5];
		// Results
		int counter = 0;
		int last = 0;
		while(rs.next()) {
			last = rs.getInt(1);
			related_items[counter] = last;
			counter++;
		}

		// This is the case for the situation where there are not 5 related books.
		for (int i=counter; i<5; i++) {
			last++;
			related_items[i] = last;
		}
		rs.close();
		related.close();

		{
			// Prepare SQL
			statement = con.prepareStatement
					("UPDATE item SET i_related1 = ?, i_related2 = ?, i_related3 = ?, i_related4 = ?, i_related5 = ? WHERE i_id = ?");

			// Set parameter
			statement.setInt(1, related_items[0]);
			statement.setInt(2, related_items[1]);
			statement.setInt(3, related_items[2]);
			statement.setInt(4, related_items[3]);
			statement.setInt(5, related_items[4]);
			statement.setInt(6, i_id);
			statement.executeUpdate();
		}
		statement.close();
	}

	public static String GetUserName(final int C_ID){
		String string = withTransaction(new tx.TransactionalCommand<String>() {
			public String doIt(Connection con) throws SQLException {
				return GetUserName(con, C_ID);
			}
		});
		return string;
	}
	private static String GetUserName(Connection con, int C_ID)throws SQLException {
		String u_name = null;
		PreparedStatement get_user_name = con.prepareStatement
				("SELECT c_uname FROM customer WHERE c_id = ?");

		// Set parameter
		get_user_name.setInt(1, C_ID);
		ResultSet rs = get_user_name.executeQuery();

		// Results
		rs.next();
		u_name = rs.getString("c_uname");
		rs.close();

		get_user_name.close();
		return u_name;
	}

	public static String GetPassword(final String C_UNAME){
		String string = withTransaction(new tx.TransactionalCommand<String>() {
			public String doIt(Connection con) throws SQLException {
				return GetPassword(con, C_UNAME);
			}
		});
		return string;
	}
	private static String GetPassword(Connection con, String C_UNAME)throws SQLException {
		String passwd = null;
		PreparedStatement get_passwd = con.prepareStatement
				("SELECT c_passwd FROM customer WHERE c_uname = ?");

		// Set parameter
		get_passwd.setString(1, C_UNAME);
		ResultSet rs = get_passwd.executeQuery();

		// Results
		rs.next();
		passwd = rs.getString("c_passwd");
		rs.close();

		get_passwd.close();
		return passwd;
	}

	//This function gets the value of I_RELATED1 for the row of
	//the item table corresponding to I_ID
	private static int getRelated1(int I_ID, Connection con) throws SQLException {
		int related1 = -1;
		PreparedStatement statement = con.prepareStatement
				("SELECT i_related1 FROM item where i_id = ?");
		statement.setInt(1, I_ID);
		ResultSet rs = statement.executeQuery();
		rs.next();
		related1 = rs.getInt(1);//Is 1 the correct index?
		rs.close();
		statement.close();
		return related1;
	}

	public static Order GetMostRecentOrder(final String c_uname, final Vector order_lines){
		Order order = withTransaction(new tx.TransactionalCommand<Order>() {
			public Order doIt(Connection con) throws SQLException {
				return GetMostRecentOrder(con, c_uname, order_lines);
			}
		});
		return order;
	}
	private static Order GetMostRecentOrder(Connection con, String c_uname, Vector order_lines)throws SQLException {
		order_lines.removeAllElements();
		int order_id;
		Order order;

		{
			// *** Get the o_id of the most recent order for this user
			PreparedStatement get_most_recent_order_id = con.prepareStatement
					("SELECT o_id " +"FROM customer, orders " +"WHERE customer.c_id = orders.o_c_id " +"AND c_uname = ? " +"ORDER BY o_date, orders.o_id DESC " +"limit 1");

			// Set parameter
			get_most_recent_order_id.setString(1, c_uname);
			ResultSet rs = get_most_recent_order_id.executeQuery();

			if (rs.next()) {
				order_id = rs.getInt("o_id");
			} else {
				// There is no most recent order
				rs.close();
				get_most_recent_order_id.close();
				return null;
			}
			rs.close();
			get_most_recent_order_id.close();
		}

		{
			// *** Get the order info for this o_id
			PreparedStatement get_order = con.prepareStatement
					("SELECT orders.*, customer.*, " +"  cc_xacts.cx_type, " +"  ship.addr_street1 AS ship_addr_street1, " +"  ship.addr_street2 AS ship_addr_street2, " +"  ship.addr_state AS ship_addr_state, " +"  ship.addr_zip AS ship_addr_zip, " +"  ship_co.co_name AS ship_co_name, " +"  bill.addr_street1 AS bill_addr_street1, " +"  bill.addr_street2 AS bill_addr_street2, " +"  bill.addr_state AS bill_addr_state, " +"  bill.addr_zip AS bill_addr_zip, " +"  bill_co.co_name AS bill_co_name " +"FROM customer, orders, cc_xacts," +"  address AS ship, " +"  country AS ship_co, " +"  address AS bill,  " + "  country AS bill_co " +"WHERE orders.o_id = ? " +"  AND cx_o_id = orders.o_id " +"  AND customer.c_id = orders.o_c_id " +"  AND orders.o_bill_addr_id = bill.addr_id " +"  AND bill.addr_co_id = bill_co.co_id " +"  AND orders.o_ship_addr_id = ship.addr_id " +"  AND ship.addr_co_id = ship_co.co_id " +"  AND orders.o_c_id = customer.c_id");

			// Set parameter
			get_order.setInt(1, order_id);
			ResultSet rs2 = get_order.executeQuery();

			// Results
			if (!rs2.next()) {
				// FIXME - This case is due to an error due to a database population error
				rs2.close();
				//		    get_order.close();
				return null;
			}
			order = new Order(rs2);
			rs2.close();
			get_order.close();
		}

		{
			// *** Get the order_lines for this o_id
			PreparedStatement get_order_lines = con.prepareStatement
					("SELECT * " + "FROM order_line, item " + "WHERE ol_o_id = ? " +"AND ol_i_id = i_id");

			// Set parameter
			get_order_lines.setInt(1, order_id);
			ResultSet rs3 = get_order_lines.executeQuery();

			// Results
			while(rs3.next()) {
				order_lines.addElement(new OrderLine(rs3));
			}
			rs3.close();
			get_order_lines.close();
		}

		return order;
	}

	// ********************** Shopping Cart code below *************************

	// Called from: TPCW_shopping_cart_interaction
	public static int createEmptyCart(){
		Integer integer = withTransaction(new tx.TransactionalCommand<Integer>() {
			public Integer doIt(Connection con) throws SQLException {
				return createEmptyCart(con);
			}
		});
		return integer;
	}
	private static int createEmptyCart(Connection con)throws SQLException {
		int SHOPPING_ID = 0;
		//	boolean success = false;

		//while(success == false) {
		PreparedStatement get_next_id = con.prepareStatement
				("SELECT COUNT(*) FROM shopping_cart");
		// synchronized(Cart.class) {
		ResultSet rs = get_next_id.executeQuery();
		rs.next();
		SHOPPING_ID = rs.getInt(1);
		rs.close();

		PreparedStatement insert_cart = con.prepareStatement
				("INSERT into shopping_cart (sc_id, sc_time) " + "VALUES (?," + "CURRENT_TIMESTAMP)");
		insert_cart.setInt(1, SHOPPING_ID);
		insert_cart.executeUpdate();
		get_next_id.close();
		// }
		return SHOPPING_ID;
	}

	public static Cart doCart(final int SHOPPING_ID, final Integer I_ID, final Vector ids, final Vector quantities) {
		Cart cart = withTransaction(new tx.TransactionalCommand<Cart>() {
			public Cart doIt(Connection con) throws SQLException {
				return doCart(con, SHOPPING_ID, I_ID, ids, quantities);
			}
		});
		return cart;
	}
	private static Cart doCart(Connection con, int SHOPPING_ID, Integer I_ID, Vector ids, Vector quantities) throws SQLException {
		Cart cart = null;

		if (I_ID != null) {
			addItem(con, SHOPPING_ID, I_ID.intValue());
		}
		refreshCart(con, SHOPPING_ID, ids, quantities);
		addRandomItemToCartIfNecessary(con, SHOPPING_ID);
		resetCartTime(con, SHOPPING_ID);
		cart = TPCW_Database.getCart(con, SHOPPING_ID, 0.0);

		return cart;
	}

	//This function finds the shopping cart item associated with SHOPPING_ID
	//and I_ID. If the item does not already exist, we create one with QTY=1,
	//otherwise we increment the quantity.

	private static void addItem(Connection con, int SHOPPING_ID, int I_ID) throws SQLException {
		PreparedStatement find_entry = con.prepareStatement
				("SELECT scl_qty FROM shopping_cart_line WHERE scl_sc_id = ? AND scl_i_id = ?");

		// Set parameter
		find_entry.setInt(1, SHOPPING_ID);
		find_entry.setInt(2, I_ID);
		ResultSet rs = find_entry.executeQuery();

		// Results
		if(rs.next()) {
			//The shopping cart id, item pair were already in the table
			int currqty = rs.getInt("scl_qty");
			currqty+=1;
			PreparedStatement update_qty = con.prepareStatement
					("UPDATE shopping_cart_line SET scl_qty = ? WHERE scl_sc_id = ? AND scl_i_id = ?");
			update_qty.setInt(1, currqty);
			update_qty.setInt(2, SHOPPING_ID);
			update_qty.setInt(3, I_ID);
			update_qty.executeUpdate();
			update_qty.close();
		} else {//We need to add a new row to the table.

			//Stick the item info in a new shopping_cart_line
			PreparedStatement put_line = con.prepareStatement
					("INSERT into shopping_cart_line (scl_sc_id, scl_qty, scl_i_id) VALUES (?,?,?)");
			put_line.setInt(1, SHOPPING_ID);
			put_line.setInt(2, 1);
			put_line.setInt(3, I_ID);
			put_line.executeUpdate();
			put_line.close();
		}
		rs.close();
		find_entry.close();
	}

	private static void refreshCart(Connection con, int SHOPPING_ID, Vector ids,
									Vector quantities) throws SQLException {
		int i;
		for(i = 0; i < ids.size(); i++){
			String I_IDstr = (String) ids.elementAt(i);
			String QTYstr = (String) quantities.elementAt(i);
			int I_ID = Integer.parseInt(I_IDstr);
			int QTY = Integer.parseInt(QTYstr);

			if(QTY == 0) { // We need to remove the item from the cart
				PreparedStatement statement = con.prepareStatement
						("DELETE FROM shopping_cart_line WHERE scl_sc_id = ? AND scl_i_id = ?");
				statement.setInt(1, SHOPPING_ID);
				statement.setInt(2, I_ID);
				statement.executeUpdate();
				statement.close();
			}
			else { //we update the quantity
				PreparedStatement statement = con.prepareStatement
						("UPDATE shopping_cart_line SET scl_qty = ? WHERE scl_sc_id = ? AND scl_i_id = ?");
				statement.setInt(1, QTY);
				statement.setInt(2, SHOPPING_ID);
				statement.setInt(3, I_ID);
				statement.executeUpdate();
				statement.close();
			}
		}
	}

	private static void addRandomItemToCartIfNecessary(Connection con, int SHOPPING_ID) throws SQLException {
		// check and see if the cart is empty. If it's not, we do
		// nothing.
		int related_item = 0;

		// Check to see if the cart is empty
		PreparedStatement get_cart = con.prepareStatement
				("SELECT COUNT(*) from shopping_cart_line where scl_sc_id = ?");
		get_cart.setInt(1, SHOPPING_ID);
		ResultSet rs = get_cart.executeQuery();
		rs.next();
		if (rs.getInt(1) == 0) {
			// Cart is empty
			int rand_id = TPCW_Util.getRandomI_ID();
			related_item = getRelated1(rand_id,con);
			addItem(con, SHOPPING_ID, related_item);
		}

		rs.close();
		get_cart.close();
	}


	// Only called from this class
	private static void resetCartTime(Connection con, int SHOPPING_ID) throws SQLException {
		PreparedStatement statement = con.prepareStatement
				("UPDATE shopping_cart SET sc_time = CURRENT_TIMESTAMP WHERE sc_id = ?");

		// Set parameter
		statement.setInt(1, SHOPPING_ID);
		statement.executeUpdate();
		statement.close();
	}

	public static Cart getCart(final int SHOPPING_ID, final double c_discount) {
		Cart cart = withTransaction(new tx.TransactionalCommand<Cart>() {
			public Cart doIt(Connection con) throws SQLException {
				return getCart(con, SHOPPING_ID, c_discount);
			}
		});
		return cart;
	}
	//time .05s
	private static Cart getCart(Connection con, int SHOPPING_ID, double c_discount) throws SQLException {
		Cart mycart = null;
		PreparedStatement get_cart = con.prepareStatement
				("SELECT * " + "FROM shopping_cart_line, item " + "WHERE scl_i_id = item.i_id AND scl_sc_id = ?");
		get_cart.setInt(1, SHOPPING_ID);
		ResultSet rs = get_cart.executeQuery();
		mycart = new Cart(rs, c_discount);
		rs.close();
		get_cart.close();
		return mycart;
	}

	// ************** Customer / Order code below *************************

	//This should probably return an error code if the customer
	//doesn't exist, but ...
	public static void refreshSession(final int C_ID) {
		withTransaction(new tx.TransactionalCommand<Void>() {
			public Void doIt(Connection con) throws SQLException {
				refreshSession(con, C_ID);
				return VOID;
			}
		});
	}
	private static void refreshSession(Connection con, int C_ID) throws SQLException {
		PreparedStatement updateLogin = con.prepareStatement
				("UPDATE customer SET c_login = NOW(), c_expiration = (CURRENT_TIMESTAMP + INTERVAL 2 HOUR) WHERE c_id = ?");

		// Set parameter
		updateLogin.setInt(1, C_ID);
		updateLogin.executeUpdate();

		updateLogin.close();
	}

	public static Customer createNewCustomer(final Customer cust) {
		Customer customer = withTransaction(new tx.TransactionalCommand<Customer>() {
			public Customer doIt(Connection con) throws SQLException {
				return createNewCustomer(con, cust);
			}
		});
		return customer;
	}
	private static Customer createNewCustomer(Connection con, Customer cust) throws SQLException {
		// Get largest customer ID already in use.

		cust.c_discount = (int) (java.lang.Math.random() * 51);
		cust.c_balance =0.0;
		cust.c_ytd_pmt = 0.0;
		// FIXME - Use SQL CURRENT_TIME to do this
		cust.c_last_visit = new Date(System.currentTimeMillis());
		cust.c_since = new Date(System.currentTimeMillis());
		cust.c_login = new Date(System.currentTimeMillis());
		cust.c_expiration = new Date(System.currentTimeMillis() +
				7200000);//milliseconds in 2 hours
		PreparedStatement insert_customer_row = con.prepareStatement
				("INSERT into customer (c_id, c_uname, c_passwd, c_fname, c_lname, c_addr_id, c_phone, c_email, c_since, c_last_login, c_login, c_expiration, c_discount, c_balance, c_ytd_pmt, c_birthdate, c_data) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		insert_customer_row.setString(4,cust.c_fname);
		insert_customer_row.setString(5,cust.c_lname);
		insert_customer_row.setString(7,cust.c_phone);
		insert_customer_row.setString(8,cust.c_email);
		insert_customer_row.setDate(9, new
				java.sql.Date(cust.c_since.getTime()));
		insert_customer_row.setDate(10, new java.sql.Date(cust.c_last_visit.getTime()));
		insert_customer_row.setDate(11, new java.sql.Date(cust.c_login.getTime()));
		insert_customer_row.setDate(12, new java.sql.Date(cust.c_expiration.getTime()));
		insert_customer_row.setDouble(13, cust.c_discount);
		insert_customer_row.setDouble(14, cust.c_balance);
		insert_customer_row.setDouble(15, cust.c_ytd_pmt);
		insert_customer_row.setDate(16, new java.sql.Date(cust.c_birthdate.getTime()));
		insert_customer_row.setString(17, cust.c_data);

		cust.addr_id = enterAddress(con,
				cust.addr_street1,
				cust.addr_street2,
				cust.addr_city,
				cust.addr_state,
				cust.addr_zip,
				cust.co_name);
		PreparedStatement get_max_id = con.prepareStatement
				("SELECT max(c_id) FROM customer");

		// synchronized(Customer.class) {
		// Set parameter
		ResultSet rs = get_max_id.executeQuery();
		
		// Results
		rs.next();
		cust.c_id = rs.getInt(1);//Is 1 the correct index?
		rs.close();
		cust.c_id+=1;
		cust.c_uname = TPCW_Util.DigSyl(cust.c_id, 0);
		cust.c_passwd = cust.c_uname.toLowerCase();

		
		insert_customer_row.setInt(1, cust.c_id);
		insert_customer_row.setString(2,cust.c_uname);
		insert_customer_row.setString(3,cust.c_passwd);
		insert_customer_row.setInt(6, cust.addr_id);
		insert_customer_row.executeUpdate();
		insert_customer_row.close();
		// }
		get_max_id.close();
		return cust;
	}

	//BUY CONFIRM

	public static BuyConfirmResult doBuyConfirm(final int shopping_id,
												final int customer_id,
												final String cc_type,
												final long cc_number,
												final String cc_name,
												final Date cc_expiry,
												final String shipping) {
		BuyConfirmResult buyConfirmResult = withTransaction(new tx.TransactionalCommand<BuyConfirmResult>() {
			public BuyConfirmResult doIt(Connection con) throws SQLException {
				return doBuyConfirm(con, shopping_id, customer_id, cc_type, cc_number, cc_name, cc_expiry, shipping);
			}
		});
		return buyConfirmResult;
	}
	private static BuyConfirmResult doBuyConfirm(Connection con, int shopping_id, int customer_id, String cc_type, long cc_number,
												 String cc_name, Date cc_expiry, String shipping) throws SQLException {

		BuyConfirmResult result = new BuyConfirmResult();
		double c_discount = getCDiscount(con, customer_id);
		result.cart = getCart(con, shopping_id, c_discount);
		int ship_addr_id = getCAddr(con, customer_id);
		result.order_id = enterOrder(con, customer_id, result.cart, ship_addr_id, shipping, c_discount);
		enterCCXact(con, result.order_id, cc_type, cc_number, cc_name, cc_expiry, result.cart.SC_TOTAL, ship_addr_id);
		clearCart(con, shopping_id);
		return result;
	}

	public static BuyConfirmResult doBuyConfirm(final int shopping_id,
												final int customer_id,
												final String cc_type,
												final long cc_number,
												final String cc_name,
												final Date cc_expiry,
												final String shipping,
												final String street_1, final String street_2,
												final String city, final String state,
												final String zip, final String country) {
		BuyConfirmResult buyConfirmResult = withTransaction(new tx.TransactionalCommand<BuyConfirmResult>() {
			public BuyConfirmResult doIt(Connection con) throws SQLException {
				return doBuyConfirm(con, shopping_id, customer_id, cc_type, cc_number, cc_name, cc_expiry, shipping,
						street_1, street_2, city, state, zip, country);
			}
		});
		return buyConfirmResult;
	}
	private static BuyConfirmResult doBuyConfirm(Connection con, int shopping_id, int customer_id, String cc_type, long cc_number,
												 String cc_name, Date cc_expiry, String shipping, String street_1, String street_2,
												 String city, String state, String zip, String country) throws SQLException {
		BuyConfirmResult result = new BuyConfirmResult();
		double c_discount = getCDiscount(con, customer_id);
		result.cart = getCart(con, shopping_id, c_discount);
		int ship_addr_id = enterAddress(con, street_1, street_2, city, state, zip, country);
		result.order_id = enterOrder(con, customer_id, result.cart, ship_addr_id, shipping, c_discount);
		enterCCXact(con, result.order_id, cc_type, cc_number, cc_name, cc_expiry, result.cart.SC_TOTAL, ship_addr_id);
		clearCart(con, shopping_id);
		return result;
	}


	//DB query time: .05s
	public static double getCDiscount(Connection con, int c_id) throws SQLException {
		double c_discount = 0.0;
		// Prepare SQL
		PreparedStatement statement = con.prepareStatement
				("SELECT c_discount FROM customer WHERE customer.c_id = ?");

		// Set parameter
		statement.setInt(1, c_id);
		ResultSet rs = statement.executeQuery();

		// Results
		rs.next();
		c_discount = rs.getDouble(1);
		rs.close();
		statement.close();
		return c_discount;
	}

	//DB time: .05s
	public static int getCAddrID(Connection con, int c_id) throws SQLException {
		int c_addr_id = 0;
		// Prepare SQL
		PreparedStatement statement = con.prepareStatement
				("SELECT c_addr_id FROM customer WHERE customer.c_id = ?");

		// Set parameter
		statement.setInt(1, c_id);
		ResultSet rs = statement.executeQuery();

		// Results
		rs.next();
		c_addr_id = rs.getInt(1);
		rs.close();
		statement.close();
		return c_addr_id;
	}

	public static int getCAddr(Connection con, int c_id) throws SQLException {
		int c_addr_id = 0;
		// Prepare SQL
		PreparedStatement statement = con.prepareStatement
				("SELECT c_addr_id FROM customer WHERE customer.c_id = ?");

		// Set parameter
		statement.setInt(1, c_id);
		ResultSet rs = statement.executeQuery();

		// Results
		rs.next();
		c_addr_id = rs.getInt(1);
		rs.close();
		statement.close();
		return c_addr_id;
	}

	public static void enterCCXact(Connection con,
								   int o_id,        // Order id
								   String cc_type,
								   long cc_number,
								   String cc_name,
								   Date cc_expiry,
								   double total,   // Total from shopping cart
								   int ship_addr_id) throws SQLException {

		// Updates the CC_XACTS table
		if(cc_type.length() > 10)
			cc_type = cc_type.substring(0,10);
		if(cc_name.length() > 30)
			cc_name = cc_name.substring(0,30);

		// Prepare SQL
		PreparedStatement statement = con.prepareStatement
				("INSERT into cc_xacts (cx_o_id, cx_type, cx_num, cx_name, cx_expire, cx_xact_amt, cx_xact_date, cx_co_id) " + "VALUES (?, ?, ?, ?, ?, ?, CURRENT_DATE, (SELECT co_id FROM address, country WHERE addr_id = ? AND addr_co_id = co_id))");

		// Set parameter
		statement.setInt(1, o_id);           // cx_o_id
		statement.setString(2, cc_type);     // cx_type
		statement.setLong(3, cc_number);     // cx_num
		statement.setString(4, cc_name);     // cx_name
		statement.setDate(5, cc_expiry);     // cx_expiry
		statement.setDouble(6, total);       // cx_xact_amount
		statement.setInt(7, ship_addr_id);   // ship_addr_id
		statement.executeUpdate();
		statement.close();
	}

	public static void clearCart(Connection con, int shopping_id) throws SQLException {
		// Empties all the lines from the shopping_cart_line for the
		// shopping id.  Does not remove the actually shopping cart
		// Prepare SQL
		PreparedStatement statement = con.prepareStatement
				("DELETE FROM shopping_cart_line WHERE scl_sc_id = ?");

		// Set parameter
		statement.setInt(1, shopping_id);
		statement.executeUpdate();
		statement.close();
	}

	public static int enterAddress(Connection con,  // Do we need to do this as part of a transaction?
								   String street1, String street2,
								   String city, String state,
								   String zip, String country) throws SQLException {
		// returns the address id of the specified address.  Adds a
		// new address to the table if needed
		int addr_id = 0;

		// Get the country ID from the country table matching this address.

		// Is it safe to assume that the country that we are looking
		// for will be there?
		PreparedStatement get_co_id = con.prepareStatement
				("SELECT co_id FROM country WHERE co_name = ?");
		get_co_id.setString(1, country);
		ResultSet rs = get_co_id.executeQuery();
		rs.next();
		int addr_co_id = rs.getInt("co_id");
		rs.close();
		get_co_id.close();

		//Get address id for this customer, possible insert row in
		//address table
		PreparedStatement match_address = con.prepareStatement
				("SELECT addr_id FROM address " + "WHERE addr_street1 = ? " +"AND addr_street2 = ? " + "AND addr_city = ? " + "AND addr_state = ? " + "AND addr_zip = ? " + "AND addr_co_id = ?");
		match_address.setString(1, street1);
		match_address.setString(2, street2);
		match_address.setString(3, city);
		match_address.setString(4, state);
		match_address.setString(5, zip);
		match_address.setInt(6, addr_co_id);
		rs = match_address.executeQuery();
		if(!rs.next()){//We didn't match an address in the addr table
			PreparedStatement insert_address_row = con.prepareStatement
					("INSERT into address (addr_id, addr_street1, addr_street2, addr_city, addr_state, addr_zip, addr_co_id) " + "VALUES (?, ?, ?, ?, ?, ?, ?)");
			insert_address_row.setString(2, street1);
			insert_address_row.setString(3, street2);
			insert_address_row.setString(4, city);
			insert_address_row.setString(5, state);
			insert_address_row.setString(6, zip);
			insert_address_row.setInt(7, addr_co_id);

			PreparedStatement get_max_addr_id = con.prepareStatement
					("SELECT max(addr_id) FROM address");
			// synchronized(Address.class) {
			ResultSet rs2 = get_max_addr_id.executeQuery();
			rs2.next();
			addr_id = rs2.getInt(1)+1;
			rs2.close();
			//Need to insert a new row in the address table
			insert_address_row.setInt(1, addr_id);
			insert_address_row.executeUpdate();
			// }
			get_max_addr_id.close();
			insert_address_row.close();
		} else { //We actually matched
			addr_id = rs.getInt("addr_id");
		}
		match_address.close();
		rs.close();
		return addr_id;
	}


	public static int enterOrder(Connection con, int customer_id, Cart cart, int ship_addr_id, String shipping, double c_discount) throws SQLException {
		// returns the new order_id
		int o_id = 0;
		// - Creates an entry in the 'orders' table
		PreparedStatement insert_row = con.prepareStatement
				("INSERT into orders (o_id, o_c_id, o_date, o_sub_total, " + "o_tax, o_total, o_ship_type, o_ship_date, " + "o_bill_addr_id, o_ship_addr_id, o_status) " + "VALUES (?, ?, CURRENT_DATE, ?, 8.25, ?, ?, CURRENT_DATE + INTERVAL ? DAY, ?, ?, 'Pending')");
		insert_row.setInt(2, customer_id);
		insert_row.setDouble(3, cart.SC_SUB_TOTAL);
		insert_row.setDouble(4, cart.SC_TOTAL);
		insert_row.setString(5, shipping);
		insert_row.setInt(6, TPCW_Util.getRandom(7));
		insert_row.setInt(7, getCAddrID(con, customer_id));
		insert_row.setInt(8, ship_addr_id);

		PreparedStatement get_max_id = con.prepareStatement
				("SELECT count(o_id) FROM orders");
		//selecting from order_line is really slow!
		// synchronized(Order.class) {
		ResultSet rs = get_max_id.executeQuery();
		rs.next();
		o_id = rs.getInt(1) + 1;
		rs.close();
		
		insert_row.setInt(1, o_id);
		insert_row.executeUpdate();
		// }
		get_max_id.close();
		insert_row.close();

		Enumeration e = cart.lines.elements();
		int counter = 0;
		while(e.hasMoreElements()) {
			// - Creates one or more 'order_line' rows.
			CartLine cart_line = (CartLine) e.nextElement();
			addOrderLine(con, counter, o_id, cart_line.scl_i_id,
					cart_line.scl_qty, c_discount,
					TPCW_Util.getRandomString(20, 100));
			counter++;

			// - Adjusts the stock for each item ordered
			int stock = getStock(con, cart_line.scl_i_id);
			if ((stock - cart_line.scl_qty) < 10) {
				setStock(con, cart_line.scl_i_id,
						stock - cart_line.scl_qty + 21);
			} else {
				setStock(con, cart_line.scl_i_id, stock - cart_line.scl_qty);
			}
		}
		return o_id;
	}

	public static void addOrderLine(Connection con,
									int ol_id, int ol_o_id, int ol_i_id,
									int ol_qty, double ol_discount, String ol_comment) throws SQLException {
		int success = 0;
		PreparedStatement insert_row = con.prepareStatement
				("INSERT into order_line (ol_id, ol_o_id, ol_i_id, ol_qty, ol_discount, ol_comments) " + "VALUES (?, ?, ?, ?, ?, ?)");

		insert_row.setInt(1, ol_id);
		insert_row.setInt(2, ol_o_id);
		insert_row.setInt(3, ol_i_id);
		insert_row.setInt(4, ol_qty);
		insert_row.setDouble(5, ol_discount);
		insert_row.setString(6, ol_comment);
		insert_row.executeUpdate();
		insert_row.close();
	}

	public static int getStock(Connection con, int i_id) throws SQLException {
		int stock = 0;
		PreparedStatement get_stock = con.prepareStatement
				("SELECT i_stock FROM item WHERE i_id = ?");

		// Set parameter
		get_stock.setInt(1, i_id);
		ResultSet rs = get_stock.executeQuery();

		// Results
		rs.next();
		stock = rs.getInt("i_stock");
		rs.close();
		get_stock.close();
		return stock;
	}

	public static void setStock(Connection con, int i_id, int new_stock) throws SQLException {
		PreparedStatement update_row = con.prepareStatement
				("UPDATE item SET i_stock = ? WHERE i_id = ?");
		update_row.setInt(1, new_stock);
		update_row.setInt(2, i_id);
		update_row.executeUpdate();
		update_row.close();
	}

	public static void verifyDBConsistency(){
		withTransaction(new tx.TransactionalCommand<Void>() {
			public Void doIt(Connection con) throws SQLException {
				verifyDBConsistency(con);
				return VOID;
			}
		});
	}
	private static void verifyDBConsistency(Connection con) throws SQLException {
		int this_id;
		int id_expected = 1;
		//First verify customer table
		PreparedStatement get_ids = con.prepareStatement
				("SELECT c_id FROM customer");
		ResultSet rs = get_ids.executeQuery();
		while(rs.next()){
			this_id = rs.getInt("c_id");
			while(this_id != id_expected){
				System.out.println("Missing C_ID " + id_expected);
				id_expected++;
			}
			id_expected++;
		}

		id_expected = 1;
		//Verify the item table
		get_ids = con.prepareStatement
				("SELECT i_id FROM item");
		rs = get_ids.executeQuery();
		while(rs.next()){
			this_id = rs.getInt("i_id");
			while(this_id != id_expected){
				System.out.println("Missing I_ID " + id_expected);
				id_expected++;
			}
			id_expected++;
		}

		id_expected = 1;
		//Verify the address table
		get_ids = con.prepareStatement
				("SELECT addr_id FROM address");
		rs = get_ids.executeQuery();
		while(rs.next()){
			this_id = rs.getInt("addr_id");
			//		System.out.println(this_cid+"\n");
			while(this_id != id_expected){
				System.out.println("Missing ADDR_ID " + id_expected);
				id_expected++;
			}
			id_expected++;
		}
	}
}

