package client.log;


/**
 * Created by dnlopes on 09/12/15.
 */
public class TransactionLogEntry
{

	private double selectsTime;
	private double updatesTime;
	private double insertsTime;
	private double deletesTime;
	private double parsingTime;
	private double execTime;
	private double commitTime;
	private double prepareOpTime;
	private double loadFromMainTime;
	private int proxyId;

	public TransactionLogEntry(int proxyId, double selectsTime, double updatesTime, double insertsTime, double
			deletesTime,
							   double parsingTime, double execTime, double commitTime, double prepareOpTime,
							   double loadFromMainTime)
	{
		this.proxyId = proxyId;
		this.selectsTime = selectsTime;
		this.updatesTime = updatesTime;
		this.insertsTime = insertsTime;
		this.deletesTime = deletesTime;
		this.parsingTime = parsingTime;
		this.execTime = execTime;
		this.commitTime = commitTime;
		this.prepareOpTime = prepareOpTime;
		this.loadFromMainTime = loadFromMainTime;

	}

	public String toString()
	{
		StringBuilder buffer = new StringBuilder();

		buffer.append("(").append(proxyId).append(") ");
		buffer.append("exec ").append(execTime).append("ms | ");
		buffer.append("commit ").append(commitTime).append("ms | ");
		buffer.append("inserts ").append(insertsTime).append("ms | ");
		buffer.append("updates ").append(updatesTime).append("ms | ");
		buffer.append("deletes ").append(deletesTime).append("ms | ");
		buffer.append("selects ").append(selectsTime).append("ms | ");
		buffer.append("parsing ").append(parsingTime).append("ms | ");
		buffer.append("generate crdt ").append(prepareOpTime).append("ms | ");
		buffer.append("load from main ").append(loadFromMainTime).append("ms");

		return buffer.toString();
	}
}
