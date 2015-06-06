package applications.micro.workload;


import java.util.Random;


/**
 * Created by dnlopes on 05/06/15.
 */
public class MicroWorkload implements Workload, MicroConstants
{

	private int coordinationRate;
	private Random random;

	public MicroWorkload(int coordinationRate)
	{
		this.coordinationRate = coordinationRate;
		this.random = new Random(System.nanoTime());
	}

	@Override
	public String getNextOperation()
	{
		if(random.nextInt(100) < coordinationRate)
		{

		} else
		{

		}

	}



	private int selectRandomRecord()
	{
		return random.nextInt(RECORDS_PER_TABLE);
	}

	private String generateCoordinatedOperation()
	{

	}

	private String generateNonCoordinatedOperation()
	{

	}

}
