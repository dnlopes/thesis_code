package tests.parser;


import database.constraints.Constraint;
import database.constraints.check.CheckConstraint;
import database.constraints.fk.ForeignKeyConstraint;
import database.constraints.unique.AutoIncrementConstraint;
import database.constraints.unique.UniqueConstraint;
import util.parser.DDLParser;
import database.util.DatabaseMetadata;
import database.util.DatabaseTable;

import static org.junit.Assert.assertEquals;


/**
 * Created by dnlopes on 06/03/15.
 */
public class ParserTest
{

	private static final String SCHEMA_FILE =
			"/Users/dnlopes/workspaces/thesis/code/weakdb/resources/micro-database.sql";

	public static void main(String args[])
	{

		DDLParser parser = new DDLParser(SCHEMA_FILE);
		DatabaseMetadata metadata = parser.parseAnnotations();

		// for TPCW now
		int totalInvariants = 0;
		int uniqueInvariants = 0;
		int autoIncrementInvariants = 0;
		int foreignKeyInvariants = 0;
		int checkInvariant = 0;

		//check invariants

		for(DatabaseTable table : metadata.getAllTables())
		{
			for(Constraint c : table.getTableInvarists())
			{
				totalInvariants++;

				if(c instanceof CheckConstraint)
					checkInvariant++;
				else if(c instanceof AutoIncrementConstraint)
					autoIncrementInvariants++;
				else if(c instanceof UniqueConstraint)
					uniqueInvariants++;
				else if(c instanceof ForeignKeyConstraint)
					foreignKeyInvariants++;
			}
		}

		assertEquals("total constraints", 19, totalInvariants);
		assertEquals("total unique constraints", 6, uniqueInvariants);
		assertEquals("total auto increment constraints", 4, autoIncrementInvariants);
		assertEquals("total foreign key constraints", 9, foreignKeyInvariants);
		assertEquals("total check contraints", 0, checkInvariant);
	}
}
