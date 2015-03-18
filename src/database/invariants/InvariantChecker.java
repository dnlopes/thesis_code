package database.invariants;


import database.util.DataField;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.operation.ShadowOperation;
import runtime.Runtime;
import util.ExitCode;


/**
 * Created by dnlopes on 17/03/15.
 */
public class InvariantChecker
{

	static final Logger LOG = LoggerFactory.getLogger(InvariantChecker.class);

	public static void checkInvariants(ShadowOperation shadowOp, Statement stat, DataField field, String value)
	{
		LOG.trace("checking for invariants for field {}", field.getFieldName());

		if(stat == null || !field.hasInvariants())
			return;

		if(stat instanceof Insert)
			checkInvariantsForInsert(shadowOp, field, value);
		if(stat instanceof Update)
			checkInvariantsForUpdate(shadowOp, (Update) stat, field, value);
		if(stat instanceof Delete)
			checkInvariantsForDelete(shadowOp, field, value);
	}

	public static void checkInvariantsForInsert(ShadowOperation shadowOp, DataField field, String value)
	{
		// unique (only if generated by user)
		// autoincrement is not included because it
		// check constraints (not implemented in MySQL)
		// no need to verify delta here because it is an insert op

		for(Invariant inv : field.getInvariants())
		{
			if(inv instanceof UniqueInvariant)
			{

				if(field.isAutoIncrement())
				{
					RequestValue requestValue = new RequestValue(field.getTableName(), field.getFieldName());
					shadowOp.addRequestValue(requestValue);
					LOG.trace("autoincrement constraint in field {}. Will request value to coordinator",
							field.getFieldName());

				} else
				{
					CheckValue checkValue = new CheckValue(field.getTableName(), field.getFieldName(), value);
					LOG.trace("unique constraint in field {} with desired value {}", field.getFieldName(), value);
					shadowOp.addCheckValue(checkValue);
				}

			} else if(inv instanceof GreaterThanInvariant)
			{
				if(((GreaterThanInvariant) inv).isViolated(value))
				{
					LOG.error("constraint violated: trying to insert {} in field {}", value, field.getFieldName());
					shadowOp.getTransaction().setInternalAborted("check constraint violated");
				}
			} else if(inv instanceof LesserThanInvariant)
			{

				if(((LesserThanInvariant) inv).isViolated(value))
				{
					LOG.error("constraint violated: trying to insert {} in field {}", value, field.getFieldName());
					shadowOp.getTransaction().setInternalAborted("check constraint violated");
				}
			} else
			{
				LOG.error("Unkown Invariant type. Exiting.");
				Runtime.throwRunTimeException("unknown invariant type", ExitCode.UNKNOWN_INVARIANT);
			}
		}

	}

	public static void checkInvariantsForUpdate(ShadowOperation shadowOp, Update stat, DataField field, String value)
	{
		//check uniques, and check
		//TODO
	}

	public static void checkInvariantsForDelete(ShadowOperation shadowOp, DataField field, String value)
	{

		// we should contact the coordinator to free "unique" names that will be free after delete
		// CHECK constraints are not relevant in this case
		// what if this check constraint involves other tuples?
		// here we only care about unique values that will be freed by the operation
		// we should not care about autoincrement as well

		for(Invariant inv : field.getInvariants())
		{
			if(inv instanceof UniqueInvariant)
			{
				DeleteValue deleteValue = new DeleteValue(field.getTableName(), field.getFieldName(), value);
				shadowOp.addDeleteValue(deleteValue);
			}
		}
	}
}
