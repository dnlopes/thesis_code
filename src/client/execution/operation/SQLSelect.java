package client.execution.operation;


import common.util.ExitCode;
import common.util.RuntimeUtils;
import common.util.defaults.DatabaseDefaults;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;


/**
 * Created by dnlopes on 06/12/15.
 */
public class SQLSelect extends SQLOperation
{

	private static final String NOT_DELETED_EXPRESSION = DatabaseDefaults.DELETED_COLUMN + "=" + DatabaseDefaults
			.NOT_DELETED_VALUE;
	protected final Select sqlStat;

	public SQLSelect(Select sqlStat)
	{
		super(SQLOperationType.SELECT);
		this.sqlStat = sqlStat;
	}

	public Select getSelect()
	{
		return sqlStat;
	}

	@Override
	public void prepareOperation(boolean useWhere, String tempTableName)
	{
		RuntimeUtils.throwRunTimeException("should not be calling this method", ExitCode.INVALIDUSAGE);
	}

	public void prepareOperation(String tempTableName)
	{
		RuntimeUtils.throwRunTimeException("should not be calling this method", ExitCode.INVALIDUSAGE);
	}

	public void prepareOperation()
	{
		appendNotDeletedFilter();
	}

	private void appendNotDeletedFilter()
	{
		StringBuilder buffer = new StringBuilder();

		ExpressionDeParser expressionDeParser = new ExpressionDeParser()
		{
			boolean done = false;

			@Override
			public void visit(AndExpression andExpression)
			{
				if(andExpression.isNot())
					getBuffer().append(" NOT ");

				andExpression.getLeftExpression().accept(this);
				getBuffer().append(" AND ");
				andExpression.getRightExpression().accept(this);

				if(!done)
				{
					getBuffer().append(" ").append(NOT_DELETED_EXPRESSION);
					done = true;
				}
			}

			@Override
			public void visit(EqualsTo equalsTo)
			{
				visitOldOracleJoinBinaryExpression(equalsTo, " = ");

				if(!done)
				{
					getBuffer().append(" AND ").append(NOT_DELETED_EXPRESSION);
					done = true;
				}
			}
		};

		SelectDeParser deparser = new SelectDeParser(expressionDeParser, buffer);
		expressionDeParser.setSelectVisitor(deparser);
		expressionDeParser.setBuffer(buffer);
		sqlStat.getSelectBody().accept(deparser);

		sqlString = buffer.toString();
	}

	@Override
	public SQLUpdate duplicate() throws JSQLParserException
	{
		RuntimeUtils.throwRunTimeException("should not be calling this method", ExitCode.INVALIDUSAGE);
		return null;
	}

}
