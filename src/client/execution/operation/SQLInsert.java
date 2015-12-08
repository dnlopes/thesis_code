package client.execution.operation;


import common.database.Record;
import common.database.field.DataField;
import common.database.util.PrimaryKey;
import common.util.ExitCode;
import common.util.RuntimeUtils;
import common.util.exception.NotCallableException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.insert.Insert;

import java.util.*;


/**
 * Created by dnlopes on 06/12/15.
 */
public class SQLInsert extends SQLWriteOperation
{

	private final Insert sqlStat;
	private StringBuilder columnsBuffer, valuesBuffer;
	private Map<String, String> symbolsToFieldMapping;
	private Map<String, String> fieldToSymbolsMapping;

	public SQLInsert(Insert insertStat)
	{
		super(SQLOperationType.INSERT, insertStat.getTable());

		this.sqlStat = insertStat;
		this.columnsBuffer = new StringBuilder(" (");
		this.valuesBuffer = new StringBuilder("(");
		this.record = new Record(this.dbTable);
		this.symbolsToFieldMapping = new HashMap<>();
		this.fieldToSymbolsMapping = new HashMap<>();
	}

	@Override
	public void prepareOperation(boolean useWhere, String tempTableName)
	{
		StringBuilder sqlBuffer = new StringBuilder("INSERT INTO ").append(tempTableName);
		sqlBuffer.append(columnsBuffer);
		sqlBuffer.append(") VALUES ").append(valuesBuffer).append(")");

		this.sqlString = sqlBuffer.toString();
	}

	public void prepareOperation()
	{
		StringBuilder sqlBuffer = new StringBuilder("INSERT INTO ").append(dbTable.getName());
		sqlBuffer.append(columnsBuffer);
		sqlBuffer.append(") VALUES ").append(valuesBuffer).append(")");

		this.sqlString = sqlBuffer.toString();
	}

	@Override
	public SQLOperation duplicate() throws JSQLParserException
	{
		throw new NotCallableException("SQLOperation.duplicate method missing implementation");
	}

	@Override
	public void prepareForNextInput()
	{
		this.columnsBuffer.append(",");
		this.valuesBuffer.append(",");
	}

	@Override
	public void addRecordEntry(String column, String value)
	{
		this.record.addData(column, value);
		this.columnsBuffer.append(column);
		this.valuesBuffer.append(value);
	}

	public void addSymbolEntry(String symbol, String fieldName)
	{
		symbolsToFieldMapping.put(symbol, fieldName);
		fieldToSymbolsMapping.put(fieldName, symbol);
	}

	public Collection<String> getAllUsedSymbols()
	{
		return symbolsToFieldMapping.keySet();
	}

	public Set<DataField> getMissingFields()
	{
		Set<DataField> missing = new HashSet<>();

		for(DataField field : this.dbTable.getNormalFields().values())
			if(!this.record.containsEntry(field.getFieldName()))
				missing.add(field);

		return missing;
	}

	public boolean isMissingValues()
	{
		//TODO TEMPORARY small hack.
		return false;
		/*
		if(record.getRecordData().size() == this.dbTable.getMandatoryFields())
			return false;
		else
			return true;
			*/
	}

	public boolean containsSymbolForField(String fieldName)
	{
		return fieldToSymbolsMapping.containsKey(fieldName);
	}

	public String getSymbolForField(String fieldName)
	{
		return fieldToSymbolsMapping.get(fieldName);
	}

	public Insert getInsert()
	{
		return sqlStat;
	}

	public PrimaryKey getPk()
	{
		return pk;
	}
}
