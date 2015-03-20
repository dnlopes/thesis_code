package database.occ;

import database.jdbc.Result;

import java.util.ArrayList;
import java.util.List;



public class PrimaryExecResults
	implements IPrimaryExec
{
	public List<Result> results;
	
	public PrimaryExecResults() {
		results = new ArrayList<>();
	}
	
	public void addResult( Result r) {
		results.add(r);
	}
	

	public Result getResult( int pos) {
		if( pos < 0 || pos >= results.size())
			return null;
		else
			return results.get(pos);
	}
	

}
