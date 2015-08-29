package runtime.operation;


/**
 * Created by dnlopes on 20/07/15.
 */
public interface OperationsStatements
{

	public static final String UPDATE = "UPDATE ";
	public static final String PARENT_VALUES_PARENT = ") VALUES ( ";
	public static final String INSERT_INTO = "INSERT INTO ";
	public static final String INSERT = "INSERT ";
	public static final String WHERE = " WHERE ";
	public static final String AND = " AND ";

	public static final String VISIBLE_PARENT_OP_SUFFIX = "isConcurrentOrGreaterClock(_dclock,'@clock@')=TRUE";
	public static final String SET_DELETED = " SET _del=0 ";
	public static final String MERGE_DCLOCK_OP = " SET _dclock=maxClock(_dclock,'@clock')";

}
