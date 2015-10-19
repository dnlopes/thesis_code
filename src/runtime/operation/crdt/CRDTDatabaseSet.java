package runtime.operation.crdt;


import database.constraints.fk.ForeignKeyConstraint;
import database.util.DatabaseMetadata;
import database.util.ExecutionPolicy;
import database.util.field.DataField;
import database.util.table.DatabaseTable;
import runtime.operation.OperationsStatements;
import util.Configuration;
import util.defaults.DatabaseDefaults;
import util.thrift.CRDTOperation;

import java.util.*;


/**
 * Created by dnlopes on 29/09/15.
 */
public class CRDTDatabaseSet
{

	private static final DatabaseMetadata METADATA = Configuration.getInstance().getDatabaseMetadata();

	public static String[] insertRow(CRDTOperation op, String clock)
	{
		op.putToNewFieldValues(DatabaseDefaults.DELETED_COLUMN, DatabaseDefaults.NOT_DELETED_VALUE);
		op.putToNewFieldValues(DatabaseDefaults.CONTENT_CLOCK_COLUMN, clock);
		op.putToNewFieldValues(DatabaseDefaults.DELETED_CLOCK_COLUMN, clock);

		String insertOp = OperationsGenerator.generateInsertOperation(METADATA.getTable(op.getTableName()).getName(),
				op.getNewFieldValues());

		String[] ops = new String[1];
		ops[0] = insertOp;

		return ops;
	}

	public static String[] insertChildRow(CRDTOperation op, String clock)
	{
		if(!op.isSetParentsMap())
			return insertRow(op, clock);

		DatabaseTable dbTable = METADATA.getTable(op.getTableName());

		Map<String, String> parentsMap = op.getParentsMap();

		String[] ops = new String[2 + parentsMap.size() * 2];
		int i = 0;

		for(Map.Entry<String, String> parentEntry : parentsMap.entrySet())
		{
			ForeignKeyConstraint foreignKeyConstraint = (ForeignKeyConstraint) dbTable.getConstraint(
					parentEntry.getKey());

			if(foreignKeyConstraint.getPolicy().getExecutionPolicy() == ExecutionPolicy.UPDATEWINS)
			{
				String parentWhereClause = parentEntry.getValue();

				String parentVisible = OperationsGenerator.generateSetParentVisible(foreignKeyConstraint,
						parentWhereClause);
				ops[i++] = parentVisible;
				String mergedClockOp = OperationsGenerator.mergeDeletedClock(parentWhereClause,
						foreignKeyConstraint.getParentTable().getName(), clock);
				ops[i++] = mergedClockOp;
			}
		}

		op.putToNewFieldValues(DatabaseDefaults.DELETED_COLUMN, DatabaseDefaults.DELETED_VALUE);
		op.putToNewFieldValues(DatabaseDefaults.CONTENT_CLOCK_COLUMN, clock);
		op.putToNewFieldValues(DatabaseDefaults.DELETED_CLOCK_COLUMN, clock);

		String insertOp = OperationsGenerator.generateInsertOperation(dbTable.getName(), op.getNewFieldValues());
		ops[i++] = insertOp;

		//TODO generate setConditionalVisibleChild
		return ops;
	}

	public static String[] updateRow(CRDTOperation op, String clock)
	{
		DatabaseTable dbTable = METADATA.getTable(op.getTableName());
		List<String> statements = new ArrayList<>();

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

		if(lwwFieldsMap.size() > 0)
		{
			String lwwOp = OperationsGenerator.generateUpdateStatement(dbTable, op.getPkWhereClause(), lwwFieldsMap,
					true);
			statements.add(lwwOp);
		}

		if(deltaFieldsMap.size() > 0)
		{
			String deltasOp = OperationsGenerator.generateUpdateStatement(dbTable, op.getPkWhereClause(),
					deltaFieldsMap, false);
			statements.add(deltasOp);
		}

		String mergeCClockStatement = OperationsGenerator.mergeContentClock(op.getPkWhereClause(), dbTable.getName(),
				clock);
		statements.add(mergeCClockStatement);

		// if @UPDATEWINS, make sure that this row is visible in case some concurrent operation deleted it
		if(dbTable.getExecutionPolicy() == ExecutionPolicy.UPDATEWINS)
		{
			String insertRowBack = OperationsGenerator.generateInsertRowBack(op.getPkWhereClause(), op.getTableName());
			String mergeClockStatement = OperationsGenerator.mergeDeletedClock(op.getPkWhereClause(), op
							.getTableName(),
					clock);
			statements.add(insertRowBack);
			statements.add(mergeClockStatement);
		}

		String[] statementsArray = new String[statements.size()];
		statementsArray = statements.toArray(statementsArray);

		return statementsArray;
	}

	public static String[] updateChildRow(CRDTOperation op, String clock)
	{
		return null;
	}

	public static String[] deleteRow(CRDTOperation op, String clock)
	{
		return null;
	}

	public static String[] deleteParentRow(CRDTOperation op, String clock)
	{
		return null;
	}

	private static class OperationsGenerator
	{

		private static String MERGE_DCLOCK_OP_PREFIX = " SET _dclock=maxClock(_dclock,";
		private static String MERGE_CCLOCK_OP_PREFIX = " SET _cclock=maxClock(_cclock,";

		public static String generateInsertOperation(String tableName, Map<String, String> fieldsValuesMap)
		{
			StringBuilder buffer = new StringBuilder();
			StringBuilder valuesBuffer = new StringBuilder();

			buffer.append(OperationsStatements.INSERT_INTO);
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

			buffer.append(OperationsStatements.PARENT_VALUES_PARENT);
			buffer.append(valuesBuffer.toString());
			buffer.append(")");

			return buffer.toString();
		}

		public static String generateSetParentVisible(ForeignKeyConstraint constraint, String whereClause)
		{
			StringBuilder buffer = new StringBuilder();

			buffer.append(OperationsStatements.UPDATE);
			buffer.append(constraint.getParentTable().getName());
			buffer.append(OperationsStatements.SET_NOT_DELETED);
			buffer.append(OperationsStatements.WHERE);
			buffer.append(whereClause);
			buffer.append(OperationsStatements.AND);
			buffer.append(OperationsStatements.VISIBLE_PARENT_OP_SUFFIX);

			return buffer.toString();
		}

		public static String mergeDeletedClock(String whereClause, String tableName, String newClock)
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append(OperationsStatements.UPDATE);
			buffer.append(tableName);
			buffer.append(MERGE_DCLOCK_OP_PREFIX);
			buffer.append(newClock);
			buffer.append(")");
			buffer.append(OperationsStatements.WHERE);
			buffer.append(whereClause);

			return buffer.toString();
		}

		public static String mergeContentClock(String whereClause, String tableName, String newClock)
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append(OperationsStatements.UPDATE);
			buffer.append(tableName);
			buffer.append(MERGE_CCLOCK_OP_PREFIX);
			buffer.append(newClock);
			buffer.append(")");
			buffer.append(OperationsStatements.WHERE);
			buffer.append(whereClause);

			return buffer.toString();
		}

		public static String generateUpdateStatement(DatabaseTable table, String rowPkWhereClause,
													 Map<String, String> newFieldsValuesMap, boolean needsWhereClause)
		{
			StringBuilder buffer = new StringBuilder();

			buffer.append(OperationsStatements.UPDATE);
			buffer.append(table.getName());
			buffer.append(OperationsStatements.SET);

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

			if(needsWhereClause)
			{
				buffer.append(OperationsStatements.WHERE);
				buffer.append(rowPkWhereClause);
				buffer.append(OperationsStatements.AND);
				buffer.append(OperationsStatements.CLOCK_IS_GREATER_SUFIX);
			}

			return buffer.toString();
		}

		public static String generateInsertRowBack(String rowWhereClause, String tableName)
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append(generateSetVisible(rowWhereClause, tableName));
			buffer.append(OperationsStatements.AND);
			buffer.append(OperationsStatements.IS_CONCURRENT_OR_GREATER_DCLOCK);

			return buffer.toString();
		}

		private static String generateSetVisible(String rowWhereClause, String tableName)
		{
			StringBuilder buffer = new StringBuilder();

			buffer.append(OperationsStatements.UPDATE);
			buffer.append(tableName);
			buffer.append(OperationsStatements.SET_NOT_DELETED);
			buffer.append(OperationsStatements.WHERE);
			buffer.append(rowWhereClause);

			return buffer.toString();
		}
	}

}
