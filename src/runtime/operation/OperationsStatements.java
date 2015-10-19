package runtime.operation;


/**
 * Created by dnlopes on 20/07/15.
 */
public interface OperationsStatements
{

	String UPDATE = "UPDATE ";
	String PARENT_VALUES_PARENT = ") VALUES ( ";
	String INSERT_INTO = "INSERT INTO ";
	String INSERT = "INSERT ";
	String WHERE = " WHERE ";
	String AND = " AND ";
	String SET = " SET ";

	String VISIBLE_PARENT_OP_SUFFIX = "isConcurrentOrGreaterClock(_dclock,'@clock@')=TRUE";
	String IS_CONCURRENT_OR_GREATER_DCLOCK = "isConcurrentOrGreaterClock(_dclock,'@clock@')=TRUE";

	String SET_DELETED = " SET _del=1 ";
	String SET_NOT_DELETED = " SET _del=0 ";
	String MERGE_DCLOCK_OP = " SET _dclock=maxClock(_dclock,'@clock@')";
	String MERGE_CCLOCK_OP = " SET _cclock=maxClock(_cclock,'@clock@')";

	String DELETE_ROW_OP_SUFFIX_UPDATE_WINS = "isStrictlyGreater(_cclock,'@clock@')=TRUE";
	String DELETE_ROW_OP_SUFFIX_DELETE_WINS = "isConcurrentOrGreaterClock(_cclock,'@clock@')=TRUE";

	String CLOCK_IS_GREATER_SUFIX = "clockIsGreater(_cclock,'@clock@')=TRUE";

}
