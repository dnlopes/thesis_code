package database.constraints.check;


import database.constraints.AbstractConstraint;
import database.constraints.ConstraintType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeHelper;
import util.ExitCode;


/**
 * Created by dnlopes on 24/03/15.
 */
public class CheckConstraint extends AbstractConstraint
{

	protected static final Logger LOG = LoggerFactory.getLogger(CheckConstraint.class);

	private byte fieldType; // 1: string, 2: int, 3: float, 4: double
	protected CheckConstraintType conditionType;
	protected String thresholdValue;
	private boolean equalFlag;

	public CheckConstraint(CheckConstraintType type, String threshold, byte fieldType, boolean equalFlag)
	{
		super(ConstraintType.CHECK);

		this.fieldType = fieldType;
		this.conditionType = type;
		this.thresholdValue = threshold;
		this.equalFlag = equalFlag;
	}

	/**
	 * Check if the argument passed is a value that meets this check constraint
	 * @param value
	 * @return
	 */
	public boolean isValidValue(String value)
	{
		if(this.fieldType == 1)
			return this.isValidString(value);
		if(this.fieldType == 2)
			return this.isValidInt(Integer.parseInt(value));
		if(this.fieldType == 3)
			return this.isValidFloat(Float.parseFloat(value));
		if(this.fieldType == 4)
			return this.isValidDouble(Double.parseDouble(value));
		else
		{
			LOG.warn("unexpected field type");
			return false;
		}
	}

	/**
	 * For a given check constraint, this operation verifies
	 * if is it safe to update the field for newValue
	 * If it not the update must be coordinated
	 * @param newValue
	 * @param oldValue
	 * @return
	 */
	public boolean mustCoordinate(String newValue, String oldValue)
	{
		if(this.fieldType == 1)
			RuntimeHelper.throwRunTimeException("delta operation unexpected for a string field",
					ExitCode.UNEXPECTED_OP);
		if(this.fieldType == 2)
			return this.mustCoordinate(Integer.parseInt(newValue), Integer.parseInt(oldValue));
		if(this.fieldType == 3)
			return this.mustCoordinate(Float.parseFloat(newValue), Float.parseFloat(oldValue));
		if(this.fieldType == 4)
			return this.mustCoordinate(Double.parseDouble(newValue), Double.parseDouble(oldValue));
		else
		{
			LOG.warn("unexpected field type");
			return false;
		}
	}

	/**
	 * Calculates
	 * @param newValue
	 * @param oldValue
	 * @return
	 */
	public String calculateDelta(String newValue, String oldValue)
	{
		double newValueDouble = Double.parseDouble(newValue);
		double oldValueDouble = Double.parseDouble(oldValue);

		double delta = newValueDouble - oldValueDouble;
		return String.valueOf(delta);
	}

	private boolean mustCoordinate(int newValue, int oldValue)
	{
		int delta = newValue - oldValue;

		if(this.conditionType == CheckConstraintType.LESSER)
			return delta > 0;
		else if(this.conditionType == CheckConstraintType.GREATER)
			return delta < 0;
		else
		{
			LOG.error("unexpected condition type");
			RuntimeHelper.throwRunTimeException("tried to verify an unexpected check constraint",
					ExitCode.UNEXPECTED_OP);
			return false;
		}
	}

	private boolean mustCoordinate(float newValue, float oldValue)
	{
		float delta = newValue - oldValue;

		if(this.conditionType == CheckConstraintType.LESSER)
			return delta > 0;
		else if(this.conditionType == CheckConstraintType.GREATER)
			return delta < 0;
		else
		{
			LOG.error("unexpected condition type");
			RuntimeHelper.throwRunTimeException("tried to verify an unexpected check constraint",
					ExitCode.UNEXPECTED_OP);
			return false;
		}
	}

	private boolean mustCoordinate(double newValue, double oldValue)
	{
		double delta = newValue - oldValue;

		if(this.conditionType == CheckConstraintType.LESSER)
			return delta > 0;
		else if(this.conditionType == CheckConstraintType.GREATER)
			return delta < 0;
		else
		{
			LOG.error("unexpected condition type");
			RuntimeHelper.throwRunTimeException("tried to verify an unexpected check constraint",
					ExitCode.UNEXPECTED_OP);
			return false;
		}

	}

	private boolean isValidInt(int value)
	{
		switch(this.conditionType)
		{
		case LESSER:
			if(equalFlag)
				return value <= Integer.parseInt(this.thresholdValue);
			else
				return value < Integer.parseInt(this.thresholdValue);
		case GREATER:
			if(equalFlag)
				return value >= Integer.parseInt(this.thresholdValue);
			else
				return value > Integer.parseInt(this.thresholdValue);
		case EQUAL:
			return value == Integer.parseInt(this.thresholdValue);
		case NOT_EQUAL:
			return value != Integer.parseInt(this.thresholdValue);
		default:
			LOG.warn("unexpected condition type");
			return false;
		}

	}

	private boolean isValidFloat(float value)
	{
		switch(this.conditionType)
		{
		case LESSER:
			if(equalFlag)
				return value <= Float.parseFloat(this.thresholdValue);
			else
				return value < Float.parseFloat(this.thresholdValue);
		case GREATER:
			if(equalFlag)
				return value >= Float.parseFloat(this.thresholdValue);
			else
				return value > Float.parseFloat(this.thresholdValue);
		case EQUAL:
			return value == Float.parseFloat(this.thresholdValue);
		case NOT_EQUAL:
			return value != Float.parseFloat(this.thresholdValue);
		default:
			LOG.warn("unexpected condition type");
			return false;
		}
	}

	private boolean isValidDouble(double value)
	{
		switch(this.conditionType)
		{
		case LESSER:
			if(equalFlag)
				return value <= Double.parseDouble(this.thresholdValue);
			else
				return value < Double.parseDouble(this.thresholdValue);
		case GREATER:
			if(equalFlag)
				return value >= Double.parseDouble(this.thresholdValue);
			else
				return value > Double.parseDouble(this.thresholdValue);
		case EQUAL:
			return value == Double.parseDouble(this.thresholdValue);
		case NOT_EQUAL:
			return value != Double.parseDouble(this.thresholdValue);
		default:
			LOG.warn("unexpected condition type");
			return false;
		}
	}

	private boolean isValidString(String value)
	{
		switch(this.conditionType)
		{
		case LESSER:
			if(equalFlag)
				return value.compareTo(this.thresholdValue) <= 0;
			else
				return value.compareTo(this.thresholdValue) < 0;
		case GREATER:
			if(equalFlag)
				return value.compareTo(this.thresholdValue) >= 0;
			else
				return value.compareTo(this.thresholdValue) > 0;
		case EQUAL:
			return this.thresholdValue.compareTo(value) == 0;
		case NOT_EQUAL:
			return this.thresholdValue.compareTo(value) != 0;
		default:
			LOG.warn("unexpected condition type");
			return false;
		}

	}

}