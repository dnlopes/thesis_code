package common.thrift;


import client.execution.TransactionContext;
import common.database.constraints.unique.AutoIncrementConstraint;
import common.database.field.DataField;
import common.database.table.DatabaseTable;
import common.database.util.SemanticPolicy;
import org.apache.thrift.TBase;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.util.CRDTDatabaseSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by dnlopes on 16/07/15.
 */
public class ThriftUtils
{

	private static final Logger LOG = LoggerFactory.getLogger(ThriftUtils.class);

	public static CoordinatorRequest decodeCoordinatorRequest(byte[] requestByteArray)
	{
		TDeserializer deserializer = new TDeserializer(new TBinaryProtocol.Factory());
		CoordinatorRequest req = new CoordinatorRequest();
		try
		{
			deserializer.deserialize(req, requestByteArray);
			return req;
		} catch(TException e)
		{
			return null;
		}
	}

	public static byte[] encodeThriftObject(TBase request)
	{
		TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
		try
		{
			return serializer.serialize(request);
		} catch(TException e)
		{
			return null;
		}
	}

	public static CRDTCompiledTransaction compileCRDTTransaction(CRDTTransaction txn)
	{
		String txnClock = txn.getTxnClock();
		List<String> compiledOpsList = new ArrayList<>();

		for(CRDTOperation op : txn.getOpsList())
		{
			String[] compiledOps;

			switch(op.getOpType())
			{
			case INSERT:
				compiledOps = CRDTDatabaseSet.insertRow(op, txnClock);
				break;
			case INSERT_CHILD:
				compiledOps = CRDTDatabaseSet.insertChildRow(op, txnClock);
				break;
			case UPDATE:
				compiledOps = CRDTDatabaseSet.updateRow(op, txnClock);
				break;
			case UPDATE_CHILD:
				compiledOps = CRDTDatabaseSet.updateChildRow(op, txnClock);
				break;
			case DELETE:
				compiledOps = CRDTDatabaseSet.deleteRow(op, txnClock);
				break;
			case DELETE_PARENT:
				compiledOps = CRDTDatabaseSet.deleteParentRow(op, txnClock);
				break;
			default:
				return null;
			}

			compiledOpsList.addAll(Arrays.asList(compiledOps));
		}

		CRDTCompiledTransaction compiledTxn = new CRDTCompiledTransaction();
		compiledTxn.setTxnClock(txn.getTxnClock());
		compiledTxn.setId(txn.getId());
		compiledTxn.setReplicatorId(txn.getReplicatorId());
		compiledTxn.setOpsList(compiledOpsList);

		return compiledTxn;
	}

	public static void createSymbolEntry(TransactionContext context, String symbol, DataField dField, DatabaseTable
			table)
	{
		SymbolEntry symbolEntry = new SymbolEntry();
		symbolEntry.setSymbol(symbol);
		symbolEntry.setTableName(dField.getTableName());
		symbolEntry.setFieldName(dField.getFieldName());
		symbolEntry.setRequiresCoordination(false);

		context.getPreCompiledTxn().putToSymbolsMap(symbol, symbolEntry);

		if(!dField.isAutoIncrement())
			LOG.warn("field with semantic value must either be auto_increment or " + "given by the " +
					"application" +
					" " +
					"level");

		if(dField.getSemantic() == SemanticPolicy.SEMANTIC)
		{
			symbolEntry.setRequiresCoordination(true);

			AutoIncrementConstraint autoIncrementConstraint = table.getAutoIncrementConstraint(
					dField.getFieldName());
			RequestValue request = new RequestValue();

			request.setConstraintId(autoIncrementConstraint.getConstraintIdentifier());
			request.setTempSymbol(symbol);
			request.setFieldName(dField.getFieldName());

			context.getCoordinatorRequest().addToRequests(request);
		}
	}
}