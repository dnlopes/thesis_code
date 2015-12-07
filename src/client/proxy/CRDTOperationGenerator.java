package client.proxy;


import client.execution.operation.SQLDelete;
import client.execution.operation.SQLInsert;
import client.execution.operation.SQLUpdate;
import common.database.Record;
import common.database.constraints.fk.ForeignKeyConstraint;
import common.database.field.DataField;
import common.database.table.DatabaseTable;
import common.database.util.DatabaseMetadata;
import common.database.util.ExecutionPolicy;
import common.util.Environment;
import common.util.defaults.DatabaseDefaults;

import java.util.*;


/**
 * Created by dnlopes on 06/12/15.
 */
public class CRDTOperationGenerator
{

	private static final DatabaseMetadata METADATA = Environment.DB_METADATA;

	public static String[] insertRow(SQLInsert sqlInsert, String clock)
	{
		sqlInsert.prepareForNextInput();
		sqlInsert.addRecordEntry(DatabaseDefaults.DELETED_COLUMN, DatabaseDefaults.NOT_DELETED_VALUE);
		sqlInsert.prepareForNextInput();
		sqlInsert.addRecordEntry(DatabaseDefaults.CONTENT_CLOCK_COLUMN, clock);
		sqlInsert.prepareForNextInput();
		sqlInsert.addRecordEntry(DatabaseDefaults.DELETED_CLOCK_COLUMN, clock);

		//String insertOp = OperationsGenerator.generateInsertOperation(sqlInsert);
		sqlInsert.prepareOperation();

		String[] ops = new String[1];
		ops[0] = sqlInsert.getSQLString();

		return ops;
	}

	public static String[] insertChildRow(SQLInsert sqlInsert, String clock)
	{
		//TODO implement for WriteThroughProxy
		return insertRow(sqlInsert, clock);
		/*
		op.putToNewFieldValues(DatabaseDefaults.DELETED_COLUMN, DatabaseDefaults.DELETED_VALUE);
		op.putToNewFieldValues(DatabaseDefaults.CONTENT_CLOCK_COLUMN, clock);
		op.putToNewFieldValues(DatabaseDefaults.DELETED_CLOCK_COLUMN, clock);

		DatabaseTable dbTable = METADATA.getTable(op.getTableName());
		Map<String, String> parentsMap = op.getParentsMap();

		String[] ops = new String[2 + parentsMap.size() * 2];
		String[] parentOps = OperationsGenerator.generateParentsVisibilityOperations(parentsMap, dbTable, clock);
		String insertOp = OperationsGenerator.generateInsertOperation(dbTable.getName(), op.getNewFieldValues());

		int opIndex = 0;
		for(String opString : parentOps)
			ops[opIndex++] = opString;

		ops[opIndex++] = insertOp;

		if(op.isSetParentsMap())
		{
			//TODO later: generate setConditionalVisibleChild
		}

		return ops;            */
	}

	public static String[] updateRow(SQLUpdate sqlUpdate, String clock)
	{
		DatabaseTable dbTable = sqlUpdate.getDbTable();

		List<String> statements = new ArrayList<>();

		Record updatedRecord = sqlUpdate.getRecord();
		Record cachedRecord = sqlUpdate.getCachedRecord();

		updatedRecord.mergeRecords(cachedRecord);
		String recordPrimaryKeyClause = updatedRecord.getPkValue().getPrimaryKeyWhereClause();

		boolean shouldWriteLWWFields = updatedRecord.touchedLWWField();

		Map<String, String> lwwFieldsMap = new HashMap<>();
		Map<String, String> deltaFieldsMap = new HashMap<>();

		for(Map.Entry<String, String> newField : updatedRecord.getRecordData().entrySet())
		{
			DataField field = dbTable.getField(newField.getKey());

			if(field.isDeltaField())
				deltaFieldsMap.put(newField.getKey(), newField.getValue());
			else
			{
				if(shouldWriteLWWFields)
					lwwFieldsMap.put(newField.getKey(), newField.getValue());
			}
		}

		if(lwwFieldsMap.size() > 0)
		{
			String lwwOp = OperationsGenerator.generateUpdateStatement(dbTable, recordPrimaryKeyClause, lwwFieldsMap,
					true, clock);
			statements.add(lwwOp);
		}

		if(deltaFieldsMap.size() > 0)
		{
			String deltasOp = OperationsGenerator.generateUpdateStatement(dbTable, recordPrimaryKeyClause,
					deltaFieldsMap, false, clock);
			statements.add(deltasOp);
		}

		String mergeCClockStatement = OperationsGenerator.mergeContentClock(recordPrimaryKeyClause, dbTable.getName(),
				clock);
		statements.add(mergeCClockStatement);

		// if @UPDATEWINS, make sure that this row is visible in case some concurrent operation deleted it
		if(dbTable.getExecutionPolicy() == ExecutionPolicy.UPDATEWINS && dbTable.getTablePolicy().allowDeletes())
		{
			String insertRowBack = OperationsGenerator.generateInsertRowBack(recordPrimaryKeyClause, dbTable.getName(),
					clock);
			String mergeClockStatement = OperationsGenerator.mergeDeletedClock(recordPrimaryKeyClause,
					dbTable.getName(), clock);

			statements.add(insertRowBack);
			statements.add(mergeClockStatement);
		}

		String[] statementsArray = new String[statements.size()];
		statementsArray = statements.toArray(statementsArray);

		return statementsArray;
	}

	public static String[] updateChildRow(SQLUpdate sqlUpdate, String clock)
	{
		//TODO implement for WriteThroughProxy
		return updateRow(sqlUpdate, clock);
		/*
		DatabaseTable dbTable = METADATA.getTable(op.getTableName());
		List<String> statements = new ArrayList<>();

		String[] parentOps = OperationsGenerator.generateParentsVisibilityOperations(op.getParentsMap(), dbTable,
				clock);

		Collections.addAll(statements, parentOps);

		Map<String, String> lwwFieldsMap = op.getOldFieldValues();
		Map<String, String> deltaFieldsMap = new HashMap<>();

		for(Map.Entry<String, String> newField : op.getNewFieldValues().entrySet())
		{
			DataField field = dbTable.getField(newField.getKey());

			if(field.isDeltaField())
				deltaFieldsMap.put(newField.getKey(), newField.getValue());
			else
				lwwFieldsMap.put(newField.getKey(), newField.getValue());
		}

		if(deltaFieldsMap.size() > 0)
		{
			String deltasOp = OperationsGenerator.generateUpdateStatement(dbTable, op.getPkWhereClause(),
					deltaFieldsMap, false, clock);
			statements.add(deltasOp);
		}

		if(lwwFieldsMap.size() > 0)
		{
			String lwwOp = OperationsGenerator.generateUpdateStatement(dbTable, op.getPkWhereClause(), lwwFieldsMap,
					true, clock);
			statements.add(lwwOp);
		}

		String mergeCClockStatement = OperationsGenerator.mergeContentClock(op.getPkWhereClause(), dbTable.getName(),
				clock);
		statements.add(mergeCClockStatement);

		// if @UPDATEWINS, make sure that this row is visible in case some concurrent operation deleted it
		if(dbTable.getExecutionPolicy() == ExecutionPolicy.UPDATEWINS)
		{
			String insertRowBack = OperationsGenerator.generateInsertRowBack(op.getPkWhereClause(), op.getTableName(),
					clock);
			String mergeClockStatement = OperationsGenerator.mergeDeletedClock(op.getPkWhereClause(), op
							.getTableName(),
					clock);
			statements.add(insertRowBack);
			statements.add(mergeClockStatement);
		}

		String[] statementsArray = new String[statements.size()];
		statementsArray = statements.toArray(statementsArray);

		return statementsArray;   */
	}

	public static String[] deleteRow(SQLDelete sqlDelete, String clock)
	{
		//TODO implement DELETES!
		return null;
		/*
		DatabaseTable dbTable = METADATA.getTable(op.getTableName());

		ExecutionPolicy tablePolicy = dbTable.getExecutionPolicy();

		String[] ops = new String[1];

		if(tablePolicy == ExecutionPolicy.DELETEWINS)
			ops[0] = OperationsGenerator.generateDeleteDeleteWins(op.getPkWhereClause(), dbTable.getName(), clock);
		else
			ops[0] = OperationsGenerator.generateDeleteUpdateWins(op.getPkWhereClause(), dbTable.getName(), clock);

		return ops;                  */

	}

	public static String[] deleteParentRow(SQLDelete sqlDelete, String clock)
	{
		//TODO later: implementation
		return null;
	}

	private static class OperationsGenerator
	{

		private static String UPDATE = "UPDATE ";
		private static String PARENT_VALUES_PARENT = ") VALUES ( ";
		private static String INSERT_INTO = "INSERT INTO ";
		private static String WHERE = " WHERE ";
		private static String AND = " AND ";
		private static String SET = " SET ";

		private static String SET_DELETED = " SET _del=1 ";
		private static String SET_NOT_DELETED = " SET _del=0 ";

		private static String MERGE_DCLOCK_OP_PREFIX = " SET _dclock=maxClock(_dclock,'";
		private static String MERGE_CCLOCK_OP_PREFIX = " SET _cclock=maxClock(_cclock,'";
		private static String DELETE_ROW_OP_SUFFIX_UPDATE_WINS = "isStrictlyGreater(_cclock,'";
		private static String DELETE_ROW_OP_SUFFIX_DELETE_WINS = "isConcurrentOrGreaterClock(_cclock,'";
		private static String VISIBLE_PARENT_OP_SUFFIX = "isConcurrentOrGreaterClock(_dclock,'";
		private static String IS_CONCURRENT_OR_GREATER_DCLOCK = "isConcurrentOrGreaterClock(_dclock,'";
		private static String CLOCK_IS_GREATER_SUFIX = "clockIsGreater(_cclock,'";

		public static String generateInsertOperation(SQLInsert sqlInsert)
		{
			return null;
			/*
			StringBuilder buffer = new StringBuilder();
			StringBuilder valuesBuffer = new StringBuilder();

			buffer.append(INSERT_INTO);
			buffer.append(tableName);
			buffer.append(" (");

			for(Map.Entry<String, String> entry : fieldsValuesMap.entrySet())
			{
				buffer.append(entry.getKey());
				buffer.append(",");
				valuesBuffer.append(entry.getValue());
				valuesBuffer.append(",");
			}

			if(buffer.charAt(buffer.length() - 1) == ',')
				buffer.setLength(buffer.length() - 1);
			if(valuesBuffer.charAt(valuesBuffer.length() - 1) == ',')
				valuesBuffer.setLength(valuesBuffer.length() - 1);

			buffer.append(PARENT_VALUES_PARENT);
			buffer.append(valuesBuffer.toString());
			buffer.append(")");

			return buffer.toString(); */
		}

		public static String generateSetParentVisible(ForeignKeyConstraint constraint, String whereClause,
													  String newClock)
		{
			StringBuilder buffer = new StringBuilder();

			buffer.append(UPDATE);
			buffer.append(constraint.getParentTable().getName());
			buffer.append(SET_NOT_DELETED);
			buffer.append(WHERE);
			buffer.append(whereClause);
			buffer.append(AND);
			buffer.append(VISIBLE_PARENT_OP_SUFFIX);
			buffer.append(newClock);
			buffer.append("')=TRUE");

			return buffer.toString();
		}

		public static String mergeDeletedClock(String whereClause, String tableName, String newClock)
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append(UPDATE);
			buffer.append(tableName);
			buffer.append(MERGE_DCLOCK_OP_PREFIX);
			buffer.append(newClock);
			buffer.append("')");
			buffer.append(WHERE);
			buffer.append(whereClause);

			return buffer.toString();
		}

		public static String mergeContentClock(String whereClause, String tableName, String newClock)
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append(UPDATE);
			buffer.append(tableName);
			buffer.append(MERGE_CCLOCK_OP_PREFIX);
			buffer.append(newClock);
			buffer.append("')");
			buffer.append(WHERE);
			buffer.append(whereClause);

			return buffer.toString();
		}

		public static String generateUpdateStatement(DatabaseTable table, String rowPkWhereClause,
													 Map<String, String> newFieldsValuesMap, boolean needsWhereClause,
													 String newClock)
		{
			StringBuilder buffer = new StringBuilder();

			buffer.append(UPDATE);
			buffer.append(table.getName());
			buffer.append(SET);

			for(Map.Entry<String, String> entry : newFieldsValuesMap.entrySet())
			{
				DataField dataField = table.getField(entry.getKey());

				if(dataField.isPrimaryKey())
					continue;

				buffer.append(entry.getKey());
				buffer.append("=");
				buffer.append(entry.getValue());
				buffer.append(",");
			}

			if(buffer.charAt(buffer.length() - 1) == ',')
				buffer.setLength(buffer.length() - 1);

			buffer.append(WHERE);
			buffer.append(rowPkWhereClause);

			if(needsWhereClause)
			{
				buffer.append(AND);
				buffer.append(CLOCK_IS_GREATER_SUFIX);
				buffer.append(newClock);
				buffer.append("')=TRUE");
			}

			return buffer.toString();
		}

		public static String generateInsertRowBack(String rowWhereClause, String tableName, String newClock)
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append(generateSetVisible(rowWhereClause, tableName));
			buffer.append(AND);
			buffer.append(IS_CONCURRENT_OR_GREATER_DCLOCK);
			buffer.append(newClock);
			buffer.append("')=TRUE");

			return buffer.toString();
		}

		private static String generateSetVisible(String rowWhereClause, String tableName)
		{
			StringBuilder buffer = new StringBuilder();

			buffer.append(UPDATE);
			buffer.append(tableName);
			buffer.append(SET_NOT_DELETED);
			buffer.append(WHERE);
			buffer.append(rowWhereClause);

			return buffer.toString();
		}

		private static String[] generateParentsVisibilityOperations(Map<String, String> parentsMap,
																	DatabaseTable childTable, String clock)
		{
			String[] ops = new String[parentsMap.size() * 2];
			int i = 0;

			for(Map.Entry<String, String> parentEntry : parentsMap.entrySet())
			{
				ForeignKeyConstraint foreignKeyConstraint = (ForeignKeyConstraint) childTable.getConstraint(
						parentEntry.getKey());

				DatabaseTable parentTable = foreignKeyConstraint.getParentTable();

				if(!parentTable.getTablePolicy().allowDeletes())
					continue;

				if(foreignKeyConstraint.getPolicy().getExecutionPolicy() == ExecutionPolicy.UPDATEWINS)
				{
					String parentWhereClause = parentEntry.getValue();

					String parentVisible = OperationsGenerator.generateSetParentVisible(foreignKeyConstraint,
							parentWhereClause, clock);
					ops[i++] = parentVisible;
					String mergedClockOp = OperationsGenerator.mergeDeletedClock(parentWhereClause,
							foreignKeyConstraint.getParentTable().getName(), clock);
					ops[i++] = mergedClockOp;
				}
			}

			return ops;
		}

		//****************************************************************************************//
		//********************************* DELETE OPs
		//****************************************************************************************//

		public static String generateDeleteUpdateWins(String rowWhereClause, String tableName, String newClock)
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append(UPDATE);
			buffer.append(tableName);
			buffer.append(SET_DELETED);
			buffer.append(WHERE);
			buffer.append(rowWhereClause);
			buffer.append(AND);
			buffer.append(DELETE_ROW_OP_SUFFIX_UPDATE_WINS);
			buffer.append(newClock);
			buffer.append("')=TRUE");

			return buffer.toString();
		}

		public static String generateDeleteDeleteWins(String rowWhereClause, String tableName, String newClock)
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append(UPDATE);
			buffer.append(tableName);
			buffer.append(SET_DELETED);
			buffer.append(WHERE);
			buffer.append(rowWhereClause);
			buffer.append(AND);
			buffer.append(DELETE_ROW_OP_SUFFIX_DELETE_WINS);
			buffer.append(newClock);
			buffer.append("')=TRUE");

			return buffer.toString();
		}

	}
	
}
