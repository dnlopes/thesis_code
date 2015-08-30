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
	public static final String SET = " SET ";

	public static final String VISIBLE_PARENT_OP_SUFFIX = "isConcurrentOrGreaterClock(_dclock,'@clock@')=TRUE";
	public static final String IS_CONCURRENT_OR_GREATER_DCLOCK = "isConcurrentOrGreaterClock(_dclock,'@clock@')=TRUE";

	public static final String SET_DELETED = " SET _del=1 ";
	public static final String SET_NOT_DELETED = " SET _del=0 ";
	public static final String MERGE_DCLOCK_OP = " SET _dclock=maxClock(_dclock,'@clock@')";
	public static final String MERGE_CCLOCK_OP = " SET _cclock=maxClock(_cclock,'@clock@')";

	public static final String DELETE_ROW_OP_SUFFIX_UPDATE_WINS = "isConcurrentOrGreaterClock(_cclock,'@clock@')=TRUE " +
			"AND isStrictlyGreater(_dclock,'@clock@')=TRUE";
	public static final String DELETE_ROW_OP_SUFFIX_DELETE_WINS = "isConcurrentOrGreaterClock(_cclock,'@clock@')=TRUE"
			+ " AND isStrictlyGreater(_dclock,'@clock@')=TRUE";

	public static final String CLOCK_IS_GREATER_SUFIX = "clockIsGreater(_cclock,'@clock@')=TRUE";

}
