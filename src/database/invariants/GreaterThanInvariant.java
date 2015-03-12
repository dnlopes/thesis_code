package database.invariants;

import database.util.DataField;

/**
 * Created by dnlopes on 10/03/15.
 */
public class GreaterThanInvariant extends Invariant
{
	private String minValue;

	public GreaterThanInvariant(DataField field, String threshold, String declaration)
	{
		super(field, declaration);
		this.minValue = threshold;
	}

	public String getMinValue()
	{
		return this.minValue;
	}
}
