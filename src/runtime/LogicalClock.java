package runtime;


import util.ExitCode;


public class LogicalClock implements java.io.Serializable
{

	private long generation;
	private long[] entries;

	public LogicalClock(int entriesNumber)
	{
		this.entries = new long[entriesNumber];
		this.generation = 1;
		for(int i = 0; i < entriesNumber; i++)
		{
			this.entries[i] = 0;
		}
	}

	public LogicalClock(long generation, long[] entries)
	{
		this.generation = generation;
		this.entries = entries;
	}

	public long getGeneration()
	{
		return this.generation;
	}

	public long[] getDcEntries()
	{
		return this.entries;
	}

	public long getEntry(int index)
	{
		return this.entries[index];
	}

	public boolean comparable(LogicalClock lc)
	{
		return lc.entries.length == this.entries.length;
	}

	/**
	 * @param lc
	 *
	 * @return a logical clock that is the pairwise maximum of these two clocks
	 */
	public LogicalClock maxClock(LogicalClock lc)
	{
		if(!comparable(lc))
			RuntimeHelper.throwRunTimeException(
					"incomparable logicalclocks: " + entries.length + " " + lc.entries.length, ExitCode.INVALIDUSAGE);

		long[] tmpEntries = new long[entries.length];
		long tmpGeneration;

		if(lc.getGeneration() > this.generation)
			tmpGeneration = lc.getGeneration();
		else
			tmpGeneration = this.generation;

		for(int i = 0; i < tmpEntries.length; i++)
		{
			if(entries[i] > lc.entries[i])
				tmpEntries[i] = entries[i];
			else
				tmpEntries[i] = lc.entries[i];
		}

		return new LogicalClock(tmpGeneration, tmpEntries);
	}

	public void increment(int index)
	{
		this.entries[index-1]++;
	}

	public boolean precedes(LogicalClock lc)
	{
		boolean res = false;

		for(int i = 0; res && i < entries.length; i++)
		{
			res = entries[i] <= lc.entries[i];
		}

		return res;
	}

	public boolean precededBy(LogicalClock lc)
	{
		return lc.precedes(this);
	}

	public boolean equals(LogicalClock lc)
	{
		boolean res = this.generation == lc.getGeneration();

		for(int i = 0; res && i < entries.length; i++)
		{
			res = res && entries[i] == lc.entries[i];
		}
		return res;
	}

	/**
	 * returns true iff this is less than lc in at most 1 position and by at most 1.
	 * returns false otherwise
	 */
	public boolean lessThanByAtMostOne(LogicalClock lc)
	{
		boolean res = comparable(lc);
		boolean one = false;

		for(int i = 0; res && i < entries.length; i++)
		{
			if(entries[i] < lc.entries[i])
			{
				if(entries[i] == lc.entries[i] - 1)
				{
					if(!one)
					{
						one = true;
					} else
					{
						res = false;
					}
				} else
				{
					res = false;
				}
			}
		}
		return res && one;
	}

	public long getClockValue()
	{
		long result = 0;
		long multiplier = 10 * this.entries.length;

		for(int i = 0; i < this.entries.length; i++)
		{
			multiplier = multiplier / 10;
			result += multiplier*this.entries[i];
		}

		return result;
	}

	@Override
	public String toString()
	{
		String tmp = "" + generation + ":";
		for(int i = 0; i < entries.length; i++)
		{

			tmp += "-" + entries[i];
		}

		return tmp;
	}

	public int hashcode()
	{
		long sum = 0;
		for(int i = 0; i < entries.length; i++)
		{
			sum += (int) entries[i];
		}
		return (int) (sum * 1000 + generation);

	}

}