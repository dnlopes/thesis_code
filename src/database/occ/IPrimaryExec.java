package database.occ;

import database.jdbc.Result;


public interface IPrimaryExec
{
	public void addResult( Result r);
	public Result getResult( int pos);
}
