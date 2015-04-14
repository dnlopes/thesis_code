/*
 * This class defines methods to parse sql schema to create all table and field
 * crdts.
 */

package util.parser;


import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import database.constraints.Constraint;
import database.constraints.fk.ForeignKeyConstraint;
import database.constraints.unique.UniqueConstraint;
import database.util.DataField;
import database.util.DatabaseMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ExitCode;
import util.debug.Debug;

import database.util.table.AosetTable;
import database.util.table.ArsetTable;
import database.util.table.AusetTable;
import database.util.table.READONLY_Table;
import database.util.table.UosetTable;
import database.util.DatabaseTable;


/**
 * The Class SchemaParser.
 */
public class DDLParser
{

	static final Logger LOG = LoggerFactory.getLogger(DDLParser.class);

	/** The file name. */
	private String fileName;
	private DatabaseMetadata databaseMetadata;

	/** The table crdt form map. */
	private HashMap<String, DatabaseTable> tableCrdtFormMap;

	/**
	 * Instantiates a new schema parser.
	 *
	 * @param fileName
	 * 		the f n
	 */
	public DDLParser(String fileName)
	{
		this.fileName = fileName;
		this.databaseMetadata = new DatabaseMetadata();
		LOG.trace("parser created for schema file {}", this.fileName);
	}

	/**
	 * Gets the all create table strings.
	 *
	 * @return the all create table strings
	 */
	public Vector<String> getAllCreateTableStrings()
	{
		BufferedReader br;
		String schemaContentStr = "";
		String line;
		try
		{
			//InputStream is = getClass().getClassLoader().getResourceAsStream(this.fileName);
			//FileReader reader = new FileReader(this.fileName);
			InputStream stream = new FileInputStream(this.fileName);
			br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			while((line = br.readLine()) != null)
			{
				schemaContentStr = schemaContentStr + line;
			}
			br.close();
		} catch(IOException e)
		{
			e.printStackTrace();
			System.exit(ExitCode.FILENOTFOUND);
		}

		String[] allStrings = schemaContentStr.split(";");
		Vector<String> allCreateTableStrings = new Vector<>();

		for(int i = 0; i < allStrings.length; i++)
		{
			if(CreateStatementParser.is_Create_Table_Statement(allStrings[i]))
			{
				allCreateTableStrings.add(allStrings[i]);
			}
		}

		if(allCreateTableStrings.isEmpty())
		{
			try
			{
				throw new RuntimeException("This schema doesn't contain any create statement");
			} catch(RuntimeException e)
			{
				System.exit(ExitCode.SCHEMANOCREATSTAT);
			}
		}

		return allCreateTableStrings;
	}

	/**
	 * Parses the annotations.
	 */
	public DatabaseMetadata parseAnnotations()
	{
		LOG.trace("parsing file: {}", this.fileName);
		Vector<String> allTableStrings = this.getAllCreateTableStrings();
		this.tableCrdtFormMap = new HashMap<>();

		for(int i = 0; i < allTableStrings.size(); i++)
		{
			DatabaseTable table = CreateStatementParser.createTable(allTableStrings.elementAt(i));
			if(table != null)
			{
				this.databaseMetadata.addTable(table);
				this.tableCrdtFormMap.put(table.getName(), table);
			} else
			{
				throw new RuntimeException(
						"Cannot create a tableinstance for this table " + allTableStrings.elementAt(i));
			}
		}

		if(this.tableCrdtFormMap.isEmpty())
		{
			try
			{
				throw new RuntimeException("No CRDT tables are created!");
			} catch(RuntimeException e)
			{
				e.printStackTrace();
				System.exit(ExitCode.SCHEMANOCRDTTABLE);
			}
		}

		this.fillMissingInfo();
		return databaseMetadata;
	}

	/**
	 * Prints the out.
	 */
	public void printOut()
	{
		Debug.println("Now Print Out all Table information");
		for(Map.Entry<String, DatabaseTable> entry : this.tableCrdtFormMap.entrySet())
		{
			DatabaseTable dT = entry.getValue();
			if(dT instanceof AosetTable)
			{
				Debug.println(dT.toString());
			} else if(dT instanceof ArsetTable)
			{
				Debug.println(dT.toString());
			} else if(dT instanceof UosetTable)
			{
				Debug.println(dT.toString());
			} else if(dT instanceof AusetTable)
			{
				Debug.println(dT.toString());
			} else if(dT instanceof READONLY_Table)
			{
				Debug.println(dT.toString());
			} else
			{
				try
				{
					throw new RuntimeException(
							"The type of CRDT table " + dT.getTableType() + "is not supported by our framework!");
				} catch(RuntimeException e)
				{
					e.printStackTrace();
					System.exit(ExitCode.NOTDEFINEDCRDTTABLE);
				}
			}
		}
		Debug.println("End Print Out all Table information");
	}

	private void fillMissingInfo()
	{
		// add referenced by fields

		for(DatabaseTable table : databaseMetadata.getAllTables())
		{
			for(DataField field : table.getFieldsList())
			{
				if(field.isForeignKey())
				{
					for(Constraint constraint : field.getInvariants())
					{
						if(constraint instanceof ForeignKeyConstraint)
						{
							String remoteTableString = ((ForeignKeyConstraint) constraint).getRemoteTable();
							for(String remoteFieldString : ((ForeignKeyConstraint) constraint).getRemoteFields())
							{
								DataField originField = this.databaseMetadata.getTable(remoteTableString).getField(
										remoteFieldString);
								originField.addReferencedByField(field);
							}
						}
					}
				}
			}
		}
	}
}
