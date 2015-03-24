package runtime;


import database.invariants.CheckInvariantItem;
import database.invariants.InvariantType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.operation.ShadowOperation;
import util.thrift.CheckInvariantThrift;
import util.thrift.CheckInvariantType;
import util.thrift.ThriftOperation;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by dnlopes on 21/03/15.
 */
public class Utils
{
	private static final Logger LOG = LoggerFactory.getLogger(Utils.class);


	public static ThriftOperation encodeThriftOperation(ShadowOperation shadowOperation)
	{
		//TODO
		return new ThriftOperation(shadowOperation.getTxnId(), shadowOperation.getOperationList());
	}

	public static ShadowOperation decodeThriftOperation(ThriftOperation thriftOperation)
	{
		//TODO
		return new ShadowOperation(thriftOperation.getTxnId(), thriftOperation
				.getOperations());
	}

	public static List<CheckInvariantThrift> encodeInvariantList(List<CheckInvariantItem> checkList)
	{
		List<CheckInvariantThrift> encodedCheckList = new ArrayList<>();

		for(CheckInvariantItem item : checkList)
		{
			CheckInvariantThrift inv = new CheckInvariantThrift();
			inv.setFieldName(item.getFieldName());
			inv.setId(item.getItemId());
			inv.setTableName(item.getTableName());
			inv.setType(translateCheckType(item.getType()));
			inv.setSuccess(false);
			inv.setValue("");
			inv.setResquestedValue("");
			encodedCheckList.add(inv);

		}

		return encodedCheckList;
	}

	public static List<CheckInvariantItem> decodeInvariantList(List<CheckInvariantThrift> checkList)
	{
		List<CheckInvariantItem> encodedCheckList = new ArrayList<>();

/*		for(CheckInvariantItem item : checkList)
		{
			CheckInvariantThrift inv = new CheckInvariantThrift();
			inv.setFieldName(item.getFieldName());
			inv.setId(item.getItemId());
			inv.setTableName(item.getTableName());
			inv.setType(translateCheckType(item.getType()));
			encodedCheckList.add(inv);
		}                               */

		return encodedCheckList;
	}

	private static CheckInvariantType translateCheckType(InvariantType type)
	{
		return CheckInvariantType.valueOf(type.toString());
	}




}
