package database.constraints.check;


import database.constraints.AbstractConstraint;
import database.constraints.ConstraintType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
