package database.invariants;

import database.util.DataField;

/**
 * Created by dnlopes on 06/03/15.
 */
public class LesserThanInvariant extends Invariant
{

	private String maxValue;

	public LesserThanInvariant(DataField field, String maxValue, String declaration)
	{
		super(field, declaration);
		this.maxValue = maxValue;
	}

	public String getMaxValue()
	{
		return this.maxValue;
	}
}
