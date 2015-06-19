package database.util.table;


import java.sql.ResultSet;
import java.util.*;
import java.util.Map.Entry;

import database.constraints.Constraint;
import database.constraints.fk.ForeignKeyConstraint;
import database.constraints.unique.AutoIncrementConstraint;
import database.util.field.DataField;
import database.util.ExecutionPolicy;
import database.util.PrimaryKey;
import database.util.field.hidden.DeletedField;
import database.util.field.hidden.LWWField;
import database.util.field.hidden.LogicalClockField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ExitCode;
import util.debug.Debug;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import util.defaults.Configuration;
import util.defaults.DBDefaults;


/**
 * The Class DatabaseTable.
 */
public abstract class DatabaseTable
{

	private static final Logger LOG = LoggerFactory.getLogger(DatabaseTable.class);

	private ExecutionPolicy executionPolicy;
	private boolean isParentTable;
	private String name;
	private CrdtTableType tag;
	private boolean containsAutoIncrementField;
	private String primaryKeyString;
	private Set<Constraint> tableInvarists;
	private PrimaryKey primaryKey;
	private String insertColsString;
	private Set<ForeignKeyConstraint> childTablesConstraints;
	private String selectNormalFieldsForQueryString;
	protected DataField timestampField;

	protected DataField contentClockField;
	protected DataField deletedClockField;
	protected DataField deletedField;

	protected LinkedHashMap<String, DataField> fieldsMap;
	protected Map<Integer, DataField> sortedFieldsMap;
	protected Map<String, DataField> hiddenFields;
	protected Map<String, DataField> normalFields;

	protected LinkedHashMap<String, DataField> primaryKeyMap;

	protected static LWWField timestampLWW;

	protected DatabaseTable(String name, CrdtTableType tableType, LinkedHashMap<String, DataField> fieldsMap,
							ExecutionPolicy policy)
	{
		this.executionPolicy = policy;
		this.fieldsMap = fieldsMap;
		this.name = name;
		this.tag = tableType;
		this.primaryKey = new PrimaryKey();
		this.primaryKeyMap = new LinkedHashMap<>();
		this.childTablesConstraints = new HashSet<>();
		this.sortedFieldsMap = new HashMap<>();
		this.hiddenFields = new HashMap<>();
		this.normalFields = new HashMap<>();
		this.tableInvarists = new LinkedHashSet<>();
		this.containsAutoIncrementField = false;
		this.isParentTable = false;

		if(tableType != CrdtTableType.NONCRDTTABLE)
			this.addHiddenFields(name, tableType);

		for(DataField entry : this.fieldsMap.values())
		{
			this.sortedFieldsMap.put(entry.getPosition(), entry);
			this.tableInvarists.addAll(entry.getInvariants());

			if(entry.isHiddenField())
				this.hiddenFields.put(entry.getFieldName(), entry);
			else
				this.normalFields.put(entry.getFieldName(), entry);

			if(entry.isPrimaryKey())
			{
				this.primaryKey.addField(entry);
				this.addPrimaryKey(entry);
			}

			if(entry.isAutoIncrement())
			{
				Constraint autoIncrementConstraint = new AutoIncrementConstraint();
				autoIncrementConstraint.setTableName(this.name);
				autoIncrementConstraint.addField(entry);
				autoIncrementConstraint.generateIdentifier();
				this.tableInvarists.add(autoIncrementConstraint);
				entry.addInvariant(autoIncrementConstraint);
				this.containsAutoIncrementField = true;
			}
		}

		this.setPrimaryKeyString(this.assemblePrimaryKeyString());

		StringBuffer buffer = new StringBuffer();

		Iterator<DataField> fieldsIt = this.sortedFieldsMap.values().iterator();
		while(fieldsIt.hasNext())
		{
			buffer.append(fieldsIt.next().getFieldName());
			if(fieldsIt.hasNext())
				buffer.append(",");
		}

		this.insertColsString = buffer.toString();
		this.generateSelectFieldsForQuery();
	}

	public abstract String[] transform_Insert(Insert insertStatement, String insertQuery) throws JSQLParserException;

	public abstract String[] transform_Update(ResultSet rs, Update updateStatement, String updateQuery)
			throws JSQLParserException;

	public abstract String[] transform_Delete(Delete deleteStatement, String deleteQuery) throws JSQLParserException;

	public PrimaryKey getPrimaryKey()
	{
		return this.primaryKey;
	}

	public void addPrimaryKey(DataField field)
	{
		if(this.primaryKeyMap == null)
		{
			try
			{
				throw new RuntimeException("primary key map has not been initialized!");
			} catch(RuntimeException e)
			{
				System.exit(ExitCode.NOINITIALIZATION);
			}
		}
		if(this.primaryKeyMap.containsKey(field.getFieldName()))
		{
			try
			{
				throw new RuntimeException("primary key map has duplication!");
			} catch(RuntimeException e)
			{
				System.exit(ExitCode.HASHMAPDUPLICATE);
			}
		}

		this.primaryKeyMap.put(field.getFieldName(), field);
		if(field.isAutoIncrement())
		{
			this.containsAutoIncrementField = true;
		}
	}

	/**
	 * Gets the _ table_ name.
	 *
	 * @return the _ table_ name
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Gets the _ crd t_ table_ type.
	 *
	 * @return the _ crd t_ table_ type
	 */
	public CrdtTableType getTableType()
	{
		return this.tag;
	}

	/**
	 * Gets the _ data_ field_ list.
	 *
	 * @return the _ data_ field_ list
	 */
	public Map<String, DataField> getFieldsMap()
	{
		return this.fieldsMap;
	}

	/**
	 * Gets the data field list.
	 *
	 * @return the data field list
	 */
	public List<DataField> getFieldsList()
	{
		Collection<DataField> dataFields = this.fieldsMap.values();
		if(dataFields instanceof List)
		{
			return (List<DataField>) dataFields;
		} else
		{
			return new ArrayList<>(dataFields);
		}
	}

	/**
	 * Gets the _ primary_ key_ list.
	 *
	 * @return the _ primary_ key_ list
	 */
	public HashMap<String, DataField> getPrimaryKeysMap()
	{
		return this.primaryKeyMap;
	}

	/**
	 * Gets the primary key data field list.
	 *
	 * @return the primary key data field list
	 */
	public List<DataField> getPrimaryKeysList()
	{
		Collection<DataField> dataFields = this.primaryKeyMap.values();
		if(dataFields instanceof List)
		{
			return (List<DataField>) dataFields;
		} else
		{
			return new ArrayList<>(dataFields);
		}
	}

	/**
	 * Gets the _ primary_ key_ name_ list.
	 *
	 * @return the _ primary_ key_ name_ list
	 */
	public Set<String> getPrimaryKeysNamesList()
	{
		return this.primaryKeyMap.keySet();
	}

	/**
	 * Gets the _ data_ field_ count.
	 *
	 * @return the _ data_ field_ count
	 */
	public int getFieldCount()
	{
		int tempCount = fieldsMap.size() - this.getNumOfHiddenFields();
		if(tempCount <= 0)
		{
			throw new RuntimeException("You have zero or negative number of data fields");
		}
		return tempCount;
	}

	/**
	 * Gets the _ data_ field.
	 *
	 * @param name
	 * 		the d tn
	 *
	 * @return the _ data_ field
	 */
	public DataField getField(String name)
	{

		if(this.fieldsMap == null)
		{
			try
			{
				throw new RuntimeException("data field map has not been initialized!");
			} catch(RuntimeException e)
			{
				System.exit(ExitCode.NOINITIALIZATION);
			}
		}

		if(!fieldsMap.containsKey(name))
		{
			try
			{
				throw new RuntimeException("record is not found " + name + "!");
			} catch(RuntimeException e)
			{
				System.exit(ExitCode.HASHMAPNOEXIST);
			}
		}

		return fieldsMap.get(name);

	}

	/**
	 * Gets the _ data_ field.
	 *
	 * @param dfIndex
	 * 		the df index
	 *
	 * @return the _ data_ field
	 */
	public DataField getField(int index)
	{

		if(this.sortedFieldsMap == null)
		{
			try
			{
				throw new RuntimeException("data field map has not been initialized!");
			} catch(RuntimeException e)
			{
				System.exit(ExitCode.NOINITIALIZATION);
			}
		}

		if(this.sortedFieldsMap.size() <= index)
		{
			try
			{
				throw new RuntimeException("data field index beyond the size of data file map!");
			} catch(RuntimeException e)
			{
				System.exit(ExitCode.OUTOFRANGE);
			}
		}

		if(!this.sortedFieldsMap.containsKey(new Integer(index)))
		{
			try
			{
				throw new RuntimeException("record is not found for " + index + " index!");
			} catch(RuntimeException e)
			{
				System.exit(ExitCode.HASHMAPNOEXIST);
			}
		}

		return this.sortedFieldsMap.get(new Integer(index));

	}

	/**
	 * Find mising data field.
	 *
	 * @param colList
	 * 		the col list
	 * @param valueList
	 * 		the value list
	 *
	 * @return the sets the
	 */
	public Set<String> findMisingDataField(List<String> colList, List<String> valueList)
	{
		if(getFieldCount() == colList.size() || getFieldCount() == valueList.size())
		{
			if(Configuration.TRACE_ENABLED)
				LOG.trace("no missing fields");
			return null;
		}
		if(colList.size() > 0)
		{
			assert (valueList.size() == colList.size());
			Set<String> dfNameSet = new HashSet<>();
			Set<String> colSet = new HashSet<>(colList);
			int i = 0;
			Iterator<Entry<Integer, DataField>> it = sortedFieldsMap.entrySet().iterator();

			while(it.hasNext() && i < getFieldCount())
			{
				Entry<Integer, DataField> en = it.next();
				if(!colSet.contains(en.getValue().getFieldName()))
				{
					dfNameSet.add(en.getValue().getFieldName());
				}
				i++;
			}

			Debug.println("We identify the missing data fields " + dfNameSet.toString());
			return dfNameSet;
		} else
		{
			assert (getFieldCount() == valueList.size());
			return null;
		}
	}

	/**
	 * Checks if is primary key missing from where clause.
	 *
	 * @param whereClauseStr
	 * 		the where clause str
	 *
	 * @return true, if is primary key missing from where clause
	 */
	public boolean isPrimaryKeyMissingFromWhereClause(String whereClauseStr)
	{
		HashMap<String, DataField> primaryKeysMap = this.getPrimaryKeysMap();

		for(Entry<String, DataField> stringDataFieldEntry : primaryKeysMap.entrySet())
		{
			DataField pk = stringDataFieldEntry.getValue();
			if(!whereClauseStr.contains(pk.getFieldName()))
			{
				Debug.println("You missing primary key in the where clause of this statement");
				return true;
			}
		}
		Debug.println("You didn't miss any primary key in the where clause of this statement");
		return false;
	}

	/*
	 * If an update or delete are not specified by all primary keys, we need to
	 * first fetch them from database
	 */

	/**
	 * Generated primary key query.
	 *
	 * @param whereClauseStr
	 * 		the where clause str
	 *
	 * @return the string
	 */
	public String generatedPrimaryKeyQuery(String whereClauseStr)
	{
		StringBuilder selectStr = new StringBuilder("select ");
		selectStr.append(this.getPrimaryKeyString());
		selectStr.append(" from ");
		selectStr.append(this.getName());
		selectStr.append(" ");
		selectStr.append(" where ");
		selectStr.append(whereClauseStr);
		Debug.println("Primary key selection is " + selectStr.toString());
		return selectStr.toString();
	}

	/**
	 * To string.
	 *
	 * @return the string
	 *
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		String myString = "TableName: " + this.name + " \n";
		myString += "primary key maps --> \n";

		for(Entry<String, DataField> entry : this.primaryKeyMap.entrySet())
		{
			myString += entry.getValue().toString() + " \n ";
		}

		myString += "data field maps -------->\n";

		for(Entry<String, DataField> entry : fieldsMap.entrySet())
		{
			myString += entry.getValue().toString() + " \n ";
		}

		myString += " is contained AutoIncremental fields: " + this.containsAutoIncrementField + "\n";
		if(this.timestampField != null)
		{
			myString += " logicalTimestamp: " + timestampField.toString() + "\n";
		}

		return myString;

	}

	/**
	 * Gets the num of hidden fields.
	 *
	 * @return the numOfHiddenFields
	 */
	public int getNumOfHiddenFields()
	{
		return this.hiddenFields.size();
	}

	/**
	 * Sets the primary key string.
	 *
	 * @param primaryKeyString
	 * 		the primaryKeyString to set
	 */
	public void setPrimaryKeyString(String primaryKeyString)
	{
		this.primaryKeyString = primaryKeyString;
	}

	/**
	 * Gets the primary key string.
	 *
	 * @return the primaryKeyString
	 */
	public String getPrimaryKeyString()
	{
		if(this.primaryKeyString.equals(""))
		{
			this.setPrimaryKeyString(this.assemblePrimaryKeyString());
		}
		return this.primaryKeyString;
	}

	/**
	 * Assemble primary key string.
	 *
	 * @return the string
	 */
	public String assemblePrimaryKeyString()
	{
		StringBuilder pkStrBuilder = new StringBuilder("");
		Iterator<String> it = this.getPrimaryKeysNamesList().iterator();
		int index = 0;

		while(it.hasNext())
		{
			String singlePkStr = it.next();
			if(index == 0)
			{
				pkStrBuilder.append(singlePkStr);
			} else
			{
				pkStrBuilder.append(",");
				pkStrBuilder.append(singlePkStr);
			}
			index++;
		}
		return pkStrBuilder.toString();
	}

	private void addHiddenFields(String tableName, CrdtTableType tableType)
	{
		DataField deletedField = new DeletedField(tableName, fieldsMap.size());
		this.fieldsMap.put(deletedField.getFieldName(), deletedField);
		this.deletedField = deletedField;
		this.deletedField.setDefaultValue("1");
		this.hiddenFields.put(deletedField.getFieldName(), deletedField);

		DataField contentClock = new LogicalClockField(tableName, fieldsMap.size(), DBDefaults.CONTENT_CLOCK_COLUMN);
		this.fieldsMap.put(contentClock.getFieldName(), contentClock);
		this.hiddenFields.put(contentClock.getFieldName(), contentClock);

		DataField deletedClock = new LogicalClockField(tableName, fieldsMap.size(), DBDefaults.DELETED_CLOCK_COLUMN);
		this.fieldsMap.put(deletedClock.getFieldName(), deletedClock);
		this.hiddenFields.put(deletedClock.getFieldName(), deletedClock);

		this.contentClockField = contentClock;
		this.deletedClockField = deletedClock;

	}

	public Set<Constraint> getTableInvarists()
	{
		return this.tableInvarists;
	}

	public String getInsertColsString()
	{
		return this.insertColsString;
	}

	public DataField getDeletedField()
	{
		return this.deletedField;
	}

	public DataField getContentClockField()
	{
		return this.contentClockField;
	}

	public ExecutionPolicy getExecutionPolicy()
	{
		return this.executionPolicy;
	}

	public Map<String, DataField> getNormalFields()
	{
		return this.normalFields;
	}

	public void setParentTable()
	{
		this.isParentTable = true;
	}

	public boolean isParentTable()
	{
		return this.isParentTable;
	}

	public void addChildTableConstraint(ForeignKeyConstraint fkConstraint)
	{
		this.childTablesConstraints.add(fkConstraint);
	}

	public Set<ForeignKeyConstraint> getChildTablesConstraints()
	{
		return this.childTablesConstraints;
	}

	public DataField getDeletedClockField()
	{
		return this.deletedClockField;
	}

	private void generateSelectFieldsForQuery()
	{
		StringBuilder buffer = new StringBuilder();

		Iterator<DataField> fieldsIt = this.normalFields.values().iterator();

		while(fieldsIt.hasNext())
		{
			buffer.append(fieldsIt.next().getFieldName());
			if(fieldsIt.hasNext())
				buffer.append(",");
		}

		this.selectNormalFieldsForQueryString = buffer.toString();
	}

	public String getNormalFieldsSelection()
	{
		return this.selectNormalFieldsForQueryString;
	}
}
