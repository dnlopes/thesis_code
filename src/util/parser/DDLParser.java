/*
 * This class defines methods to parse sql schema to create all table and field
 * crdts.
 */

package util.parser;


import java.io.*;
import java.util.Vector;

import database.constraints.Constraint;
import database.constraints.fk.ForeignKeyConstraint;
import database.util.DataField;
import database.util.DatabaseMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeUtils;
import util.ExitCode;
import database.util.DatabaseTable;
import util.defaults.Configuration;


/**
 * The Class SchemaParser.
 */
public class DDLParser
{

	static final Logger LOG = LoggerFactory.getLogger(DDLParser.class);

	private String fileName;
	private DatabaseMetadata databaseMetadata;

	public DDLParser(String fileName)
	{
		this.fileName = fileName;
		this.databaseMetadata = new DatabaseMetadata();
		if(Configuration.TRACE_ENABLED)
			LOG.trace("parser created for schema file {}", this.fileName);
	}

	public DatabaseMetadata parseAnnotations()
	{
		if(Configuration.TRACE_ENABLED)
			LOG.trace("parsing file: {}", this.fileName);

		Vector<String> allTableStrings = this.getAllCreateTableStrings();

		for(int i = 0; i < allTableStrings.size(); i++)
		{
			DatabaseTable table = CreateStatementParser.createTable(this.databaseMetadata,
					allTableStrings.elementAt(i));

			if(table != null)
				this.databaseMetadata.addTable(table);
			else
				RuntimeUtils.throwRunTimeException(
						"cannot create a tableinstance for this table: " + allTableStrings.elementAt(i),
						ExitCode.SCHEMANOCRDTTABLE);
		}

		if(this.databaseMetadata.getAllTables().isEmpty())
			RuntimeUtils.throwRunTimeException("no CRDT tables are created!", ExitCode.SCHEMANOCRDTTABLE);

		this.fillMissingInfo();
		return databaseMetadata;
	}

	private Vector<String> getAllCreateTableStrings()
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
							((ForeignKeyConstraint) constraint).getParentTable().setParentTable();
							((ForeignKeyConstraint) constraint).setChildTable(
									((ForeignKeyConstraint) constraint).getFieldsRelations().get(
											0).getChild().getTable());
							String remoteTableString = ((ForeignKeyConstraint) constraint).getParentTable().getName();

							for(String remoteFieldString : ((ForeignKeyConstraint) constraint).getParentFields())
							{
								DataField originField = this.databaseMetadata.getTable(remoteTableString).getField(
										remoteFieldString);
								((ForeignKeyConstraint) constraint).addRemoteField(originField);
							}
						}
					}
				}
			}
		}
	}
}
