package database.execution.temporary;


import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by dnlopes on 16/09/15.
 */
public interface ReadOnlyScratchpad
{

	public ResultSet executeQuery(String query) throws SQLException;
	
}
