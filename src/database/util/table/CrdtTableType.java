package database.util.table;

/**
 * The Enum CrdtTableType.
 */
public enum CRDTTableType
{
	
	/** The noncrdttable. */
	NONCRDTTABLE, 
 
	/** The aosettable. */
	// append only set
	AOSETTABLE,
	
	/** The ausettable. append unique item and plus update*/
	AUSETTABLE,
 
	/** The arsettable. */
	ARSETTABLE,
	
	/** The uosettable. update only*/
	UOSETTABLE
}
