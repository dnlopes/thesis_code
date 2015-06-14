package applications.micro.workload;


import applications.micro.GeneratorUtils;

import java.util.Random;


/**
 * Created by dnlopes on 05/06/15.
 */
public class MicroWorkload implements Workload,
									  MicroConstants
{

	private int writeRate;
	private int coordinatedRate;
	private Random random;
	private int counter;

	public MicroWorkload(int writeRate, int coordinatedRate)
	{
		this.writeRate = writeRate;
		this.coordinatedRate = coordinatedRate;
		this.random = new Random(System.nanoTime());
		this.counter = 10000;
	}

	@Override
	public String getNextOperation()
	{
		if(random.nextInt(100) < this.writeRate)
			return this.generateWriteOperation();
		else
			return this.generateReadOperation();
	}

	private int selectRandomRecord()
	{
		return random.nextInt(RECORDS_PER_TABLE - 1);
	}

	private String generateWriteOperation()
	{
		if(random.nextInt(100) < this.coordinatedRate)
			return this.generateCoordinatedOperation();
		else
			return this.generateNonCoordinatedOperation();
	}

	private String generateCoordinatedOperation()
	{
		//int newValue = GeneratorUtils.randomNumber(5000, 100000);
		int newValue = counter++;
		int pkValue = this.selectRandomRecord();
		return "UPDATE t1 set b = " + newValue + " WHERE a = " + pkValue;
	}

	private String generateNonCoordinatedOperation()
	{
		int newValue = GeneratorUtils.randomNumber(10000, 100000);
		int pkValue = this.selectRandomRecord();
		return "UPDATE t1 set c = " + newValue + " WHERE a = " + pkValue;

	}

	private String generateReadOperation()
	{
		int randomPk = this.selectRandomRecord();
		return "SELECT a,b,c,d,e FROM t2 WHERE a = " + randomPk;
	}

	public int getWriteRate()
	{
		return this.writeRate;
	}

	public int getCoordinatedRate()
	{
		return this.coordinatedRate;
	}

}
