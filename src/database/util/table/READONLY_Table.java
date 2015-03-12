package database.util.table;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import database.util.CrdtDataFieldType;
import database.util.CrdtTableType;
import database.util.DataField;
import database.util.DatabaseTable;
import util.ExitCode;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;

// TODO: Auto-generated Javadoc
/**
 * The Class READONLY_Table.
 */
public class READONLY_Table extends DatabaseTable {

	/**
	 * Instantiates a new rEADONL y_ table.
	 *
	 * @param tableName the t n
	 * @param dataFields the d hm
	 */
	public READONLY_Table(String declaration, String tableName, LinkedHashMap<String, DataField> dataFields) {
		super(declaration, tableName, CrdtTableType.NONCRDTTABLE, dataFields);
		// TODO Auto-generated constructor stub
		Iterator<Map.Entry<String, DataField>> it = dataFields.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, DataField> entry = it
					.next();
			DataField df = entry.getValue();
			CrdtDataFieldType crdtType = df.getCrdtType();
			if (!(crdtType == CrdtDataFieldType.NONCRDTFIELD
					|| crdtType == CrdtDataFieldType.NORMALBOOLEAN 
					|| crdtType == CrdtDataFieldType.NORMALDATETIME
					|| crdtType == CrdtDataFieldType.NORMALDOUBLE
					|| crdtType == CrdtDataFieldType.NORMALFLOAT
					|| crdtType == CrdtDataFieldType.NORMALINTEGER
					|| crdtType == CrdtDataFieldType.NORMALSTRING)) {
				try {
					throw new RuntimeException(
							"Attributes in a readonly table should not be annotated!");
				} catch (RuntimeException e) {
					e.printStackTrace();
					System.exit(ExitCode.READONLYTBLWRONGANNO);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see util.crdtlib.dbannotationtypes.dbutil.DatabaseTable#toString()
	 */
	/**
	 * @see database.util.DatabaseTable#toString()
	 * @return
	 */
	public String toString() {
		return super.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * crdts.basics.Database_Table#transform_Insert(net.sf.jsqlparser.statement
	 * .insert.Insert, java.lang.String)
	 */
	/**
	 * @see database.util.DatabaseTable#transform_Insert(net.sf.jsqlparser.statement.insert.Insert, java.lang.String)
	 * @param insertStatement
	 * @param insertQuery
	 * @return
	 * @throws JSQLParserException
	 */
	@Override
	public String[] transform_Insert(Insert insertStatement, String insertQuery)
			throws JSQLParserException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see crdts.basics.Database_Table#transform_Update(java.sql.ResultSet,
	 * net.sf.jsqlparser.statement.update.Update, java.lang.String)
	 */
	/**
	 * @see database.util.DatabaseTable#transform_Update(java.sql.ResultSet, net.sf.jsqlparser.statement.update.Update, java.lang.String)
	 * @param rs
	 * @param updateStatement
	 * @param updateQuery
	 * @return
	 * @throws JSQLParserException
	 */
	@Override
	public String[] transform_Update(ResultSet rs, Update updateStatement,
			String updateQuery) throws JSQLParserException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * crdts.basics.Database_Table#transform_Delete(net.sf.jsqlparser.statement
	 * .delete.Delete, java.lang.String)
	 */
	/**
	 * @see database.util.DatabaseTable#transform_Delete(net.sf.jsqlparser.statement.delete.Delete, java.lang.String)
	 * @param deleteStatement
	 * @param deleteQuery
	 * @return
	 * @throws JSQLParserException
	 */
	@Override
	public String[] transform_Delete(Delete deleteStatement, String deleteQuery)
			throws JSQLParserException {
		// TODO Auto-generated method stub
		return null;
	}
}
