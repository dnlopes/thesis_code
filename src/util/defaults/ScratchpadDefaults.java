package util.defaults;

/**
 * Created by dnlopes on 10/03/15.
 */
public class ScratchpadDefaults
{

	public static final int RDBMS_H2 = 1;
	public static final int RDBMS_MYSQL = 2;
	public static final int RDBMS_MIMER = 3;
	public static int SQL_ENGINE = RDBMS_MYSQL;

	public static final String SCRATCHPAD_NULL = "@$NULL";
	public static final String SCRATCHPAD_PREFIX = "SCRATCHPAD";

	public static final String SCRATCHPAD_TABLE_ALIAS_PREFIX = "_SPT_";
	public static final String SCRATCHPAD_TEMPTABLE_ALIAS_PREFIX = "_TSPT_";
	public static final String SCRATCHPAD_COL_PREFIX = "_SP_";
	public static final String SCRATCHPAD_COL_DELETED = "_SP_del";
	public static final String SCRATCHPAD_COL_TS = "_SP_ts";
	public static final String SCRATCHPAD_COL_VV = "_SP_clock";

}
