package runtime.operation;

import database.util.ExecutionPolicy;
import database.util.PrimaryKey;
import database.util.PrimaryKeyValue;
import util.defaults.DBDefaults;

import java.util.Iterator;
import java.util.List;


/**
 * Created by dnlopes on 06/05/15.
 */
public class OperationTransformer
{

	public static String buildDeleteOperation(String tableName, ExecutionPolicy tablePolicy, PrimaryKey pk,
											  List<PrimaryKeyValue> pksValues)
	{
		StringBuilder buffer = new StringBuilder();

		buffer.append("UPDATE ");
		buffer.append(tableName);
		buffer.append(" SET ");
		buffer.append(DBDefaults.DELETED_COLUMN);
		buffer.append("=1,");
		buffer.append(DBDefaults.DELETED_CLOCK_COLUMN);
		buffer.append("=");
		buffer.append(DBDefaults.CLOCK_VALUE_PLACEHOLDER);
		buffer.append(" WHERE (");
		buffer.append(pk.getQueryClause());
		buffer.append(") IN (");
		buffer.append(getPkValuesList(pksValues));
		buffer.append(") AND compareClocks(");
		buffer.append(DBDefaults.CONTENT_CLOCK_COLUMN);
		buffer.append(",'");
		buffer.append(DBDefaults.CLOCK_VALUE_PLACEHOLDER);
		buffer.append("')");
		if(tablePolicy == ExecutionPolicy.DELETEWINS)
			buffer.append(" > 0");
		else
			buffer.append(" >= 0");

		return buffer.toString();
	}

	private static String getPkValuesList(List<PrimaryKeyValue> pkValues)
	{
		StringBuilder buffer = new StringBuilder();

		Iterator<PrimaryKeyValue> pkIt = pkValues.iterator();
		while(pkIt.hasNext())
		{
			buffer.append("(");
			buffer.append(pkIt.next().getValue());
			buffer.append(")");
			if(pkIt.hasNext())
				buffer.append(",");
		}

		return buffer.toString();
	}
}
