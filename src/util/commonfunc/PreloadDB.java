/***************************************************************
Project name: georeplication
Class file name: PreloadDB.java
Created at 9:15:36 PM by chengli

Copyright (c) 2014 chengli.
All rights reserved. This program and the accompanying materials
are made available under the terms of the GNU Public License v2.0
which accompanies this distribution, and is available at
http://www.gnu.org/licenses/old-licenses/gpl-2.0.html

Contributors:
    chengli - initial API and implementation

Contact:
    To distribute or use this code requires prior specific permission.
    In this case, please contact chengli@mpi-sws.org.
****************************************************************/

package util.commonfunc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// TODO: Auto-generated Javadoc
/**
 * The Class PreloadDB.
 *
 * @author chengli
 */
public class PreloadDB 
{
  
  /** The c. */
  Connection c;
  
  /** The stat. */
  java.sql.Statement stat;

  /**
   * Creates a new <code>InitDBParallel</code> instance.
   *
   */
  public PreloadDB(String dbName)
  {
    try{
    Class.forName("com.mysql.jdbc.Driver");
    c = DriverManager.getConnection("jdbc:mysql://localhost:50000/"+dbName+"?",
			"root",
			"101010");
	c.setAutoCommit(true);
	stat = c.createStatement();
   }catch(Exception e){
	e.printStackTrace();	
   }
  }
  
  /**
   * Commit tx.
   */
  public void commitTx(){
	  try{
		    c.commit();
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
  }
  
  /**
   * Preload.
   */
  public boolean preload(){
      /*try {
              stat.executeUpdate("DROP TABLE IF EXISTS BLACKHOLEbids;");
              stat.executeUpdate("CREATE TABLE BLACKHOLEbids LIKE bids;");
              stat.executeUpdate("ALTER TABLE BLACKHOLEbids ENGINE = BLACKHOLE;");
              stat.executeUpdate("INSERT INTO BLACKHOLEbids SELECT * FROM bids ORDER BY id;");
              stat.close();
              c.close();
      }catch(Exception e){
    	  e.printStackTrace();
      }*/
	  java.sql.CallableStatement cstmt = null;
	  try {
	     String SQL = "{call preload ()}";
	     cstmt = c.prepareCall (SQL);
	     cstmt.execute();
	     return true;
	  }
	  catch (SQLException e) {
	     e.printStackTrace();
	  }
	  return false;
  }

  /**
   * The main method.
   *
   * @param args the arguments
   * @throws InterruptedException the interrupted exception
   */
  public static void main(String[] args) throws InterruptedException
  {
	  	if(args.length < 1) {
	  		System.out.println("java -jar preloadDB-big.jar dbName");
	  		System.exit(-1);
	  	}
	  	String dbName = args[0];
	    System.out.println("Preloading your database");
	
	    PreloadDB pDB = new PreloadDB(dbName);
	    boolean success = pDB.preload();
	    if(success) {
	    	System.out.println("Successfully preloaded");
	    }else {
	    	System.out.println("Failed to preload");
	    }
    }
    	
   
  }
