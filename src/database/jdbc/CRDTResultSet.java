package database.jdbc;

import database.scratchpad.ScratchpadException;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;


/**
 * Created by dnlopes on 04/03/15.
 */

public class CRDTResultSet implements ResultSet
{

	DBSelectResult res;

	public CRDTResultSet(DBSelectResult res)
	{
		this.res = res;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public boolean absolute(int arg0) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public void afterLast() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public void beforeFirst() throws SQLException
	{
		Debug.println("Move the cursor of the resultset to the one before first");
		this.res.reset();
	}

	@Override
	public void cancelRowUpdates() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public void clearWarnings() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public void close() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public void deleteRow() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public int findColumn(String arg0) throws SQLException
	{
		if(res.getColumnAliasToNumbersMap() == null)
			throw new SQLException("order of the attributes in sql query where not defined");
		return res.getColumnAliasToNumbersMap().get(arg0);

	}

	@Override
	public boolean first() throws SQLException
	{
		try
		{
			return res.first();
		} catch(ScratchpadException e)
		{
			throw new SQLException(e);
		}
	}

	@Override
	public Array getArray(int arg0) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public Array getArray(String arg0) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public InputStream getAsciiStream(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 610");
		return null;
	}

	@Override
	public InputStream getAsciiStream(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 611");
		return null;
	}

	@Override
	public BigDecimal getBigDecimal(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 612");
		return null;
	}

	@Override
	public BigDecimal getBigDecimal(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 613");
		return null;
	}

	@Override
	public BigDecimal getBigDecimal(int arg0, int arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 614");
		return null;
	}

	@Override
	public BigDecimal getBigDecimal(String arg0, int arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 615");
		return null;
	}

	@Override
	public InputStream getBinaryStream(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 616");
		return null;
	}

	@Override
	public InputStream getBinaryStream(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 617");
		return null;
	}

	@Override
	public Blob getBlob(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 618");
		return null;
	}

	@Override
	public Blob getBlob(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 619");
		return null;
	}

	@Override
	public boolean getBoolean(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 620");
		return false;
	}

	@Override
	public boolean getBoolean(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 621");
		return false;
	}

	@Override
	public byte getByte(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 622");
		return 0;
	}

	@Override
	public byte getByte(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 623");
		return 0;
	}

	@Override
	public byte[] getBytes(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 624");
		return null;
	}

	@Override
	public byte[] getBytes(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 625");
		return null;
	}

	@Override
	public Reader getCharacterStream(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 626");
		return null;
	}

	@Override
	public Reader getCharacterStream(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 627");
		return null;
	}

	@Override
	public Clob getClob(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 628");
		return null;
	}

	@Override
	public Clob getClob(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 630");
		return null;
	}

	@Override
	public int getConcurrency() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 631");
		return 0;
	}

	@Override
	public String getCursorName() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 632");
		return null;
	}

	@Override
	public Date getDate(int arg0) throws SQLException
	{
		try
		{
			return new Date(res.getDate(arg0).getTime());
		} catch(ScratchpadException e)
		{
			throw new SQLException(e);
		}
	}

	@Override
	public Date getDate(String arg0) throws SQLException
	{
		return getDate(findColumn(arg0));
	}

	@Override
	public Date getDate(int arg0, Calendar arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 633");
		return null;
	}

	@Override
	public Date getDate(String arg0, Calendar arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 634");
		return null;
	}

	@Override
	public double getDouble(int col) throws SQLException
	{
		try
		{
			return res.getDouble(col);
		} catch(ScratchpadException e)
		{
			throw new SQLException(e);
		}
	}

	@Override
	public double getDouble(String arg0) throws SQLException
	{
		return getDouble(findColumn(arg0));
	}

	@Override
	public int getFetchDirection() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 635");
		return 0;
	}

	@Override
	public int getFetchSize() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 636");
		return 0;
	}

	@Override
	public float getFloat(int arg0) throws SQLException
	{
		try
		{
			return res.getFloat(arg0);
		} catch(ScratchpadException e)
		{
			throw new SQLException(e);
		}
	}

	@Override
	public float getFloat(String arg0) throws SQLException
	{
		return getFloat(findColumn(arg0));
	}

	@Override
	public int getHoldability() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 637");
		return 0;
	}

	@Override
	public int getInt(int col) throws SQLException
	{
		try
		{
			return res.getInt(col);
		} catch(ScratchpadException e)
		{
			throw new SQLException(e);
		}
	}

	@Override
	public int getInt(String arg0) throws SQLException
	{
		return getInt(findColumn(arg0));
	}

	@Override
	public long getLong(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 638");
		return 0;
	}

	@Override
	public long getLong(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 639");
		return 0;
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 640");
		return null;
	}

	@Override
	public Reader getNCharacterStream(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 641");
		return null;
	}

	@Override
	public Reader getNCharacterStream(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 642");
		return null;
	}

	@Override
	public NClob getNClob(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 643");
		return null;
	}

	@Override
	public NClob getNClob(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 644");
		return null;
	}

	@Override
	public String getNString(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 645");
		return null;
	}

	@Override
	public String getNString(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 646");
		return null;
	}

	@Override
	public Object getObject(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 647");
		return null;
	}

	@Override
	public Object getObject(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 648");
		return null;
	}

	@Override
	public Object getObject(int arg0, Map<String, Class<?>> arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 649");
		return null;
	}

	@Override
	public Object getObject(String arg0, Map<String, Class<?>> arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 650");
		return null;
	}

	@Override
	public Ref getRef(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 651");
		return null;
	}

	@Override
	public Ref getRef(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 652");
		return null;
	}

	@Override
	public int getRow() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 653");
		return 0;
	}

	@Override
	public RowId getRowId(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 654");
		return null;
	}

	@Override
	public RowId getRowId(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 655");
		return null;
	}

	@Override
	public SQLXML getSQLXML(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 656");
		return null;
	}

	@Override
	public SQLXML getSQLXML(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 657");
		return null;
	}

	@Override
	public short getShort(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 658");
		return 0;
	}

	@Override
	public short getShort(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 659");
		return 0;
	}

	@Override
	public Statement getStatement() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 660");
		return null;
	}

	@Override
	public String getString(int col) throws SQLException
	{
		try
		{
			return res.getString(col);
		} catch(ScratchpadException e)
		{
			throw new SQLException(e);
		}
	}

	@Override
	public String getString(String arg0) throws SQLException
	{
		return getString(findColumn(arg0));
	}

	@Override
	public Time getTime(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 661");
		return null;
	}

	@Override
	public Time getTime(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 662");
		return null;
	}

	@Override
	public Time getTime(int arg0, Calendar arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 663");
		return null;
	}

	@Override
	public Time getTime(String arg0, Calendar arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 664");
		return null;
	}

	@Override
	public Timestamp getTimestamp(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 665");
		return null;
	}

	@Override
	public Timestamp getTimestamp(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 666");
		return null;
	}

	@Override
	public Timestamp getTimestamp(int arg0, Calendar arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 667");
		return null;
	}

	@Override
	public Timestamp getTimestamp(String arg0, Calendar arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 668");
		return null;
	}

	@Override
	public int getType() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 669");
		return 0;
	}

	@Override
	public URL getURL(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 670");
		return null;
	}

	@Override
	public URL getURL(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 671");
		return null;
	}

	@Override
	public InputStream getUnicodeStream(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 672");
		return null;
	}

	@Override
	public InputStream getUnicodeStream(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 673");
		return null;
	}

	@Override
	public SQLWarning getWarnings() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 674");
		return null;
	}

	@Override
	public void insertRow() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 675");

	}

	@Override
	public boolean isAfterLast() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 676");
		return false;
	}

	@Override
	public boolean isBeforeFirst() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 677");
		return false;
	}

	@Override
	public boolean isClosed() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 678");
		return false;
	}

	@Override
	public boolean isFirst() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 679");
		return false;
	}

	@Override
	public boolean isLast() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 680");
		return false;
	}

	@Override
	public boolean last() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 681");
		return false;
	}

	@Override
	public void moveToCurrentRow() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 681");

	}

	@Override
	public void moveToInsertRow() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 681");

	}

	@Override
	public boolean next() throws SQLException
	{
		try
		{
			return res.next();
		} catch(ScratchpadException e)
		{
			throw new SQLException(e);
		}
	}

	@Override
	public boolean previous() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 681");
		return false;
	}

	@Override
	public void refreshRow() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 681");

	}

	@Override
	public boolean relative(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 681");
		return false;
	}

	@Override
	public boolean rowDeleted() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 681");
		return false;
	}

	@Override
	public boolean rowInserted() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 681");
		return false;
	}

	@Override
	public boolean rowUpdated() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 681");
		return false;
	}

	@Override
	public void setFetchDirection(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 681");

	}

	@Override
	public void setFetchSize(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 681");

	}

	@Override
	public void updateArray(int arg0, Array arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 691");

	}

	@Override
	public void updateArray(String arg0, Array arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 691");

	}

	@Override
	public void updateAsciiStream(int arg0, InputStream arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 691");

	}

	@Override
	public void updateAsciiStream(String arg0, InputStream arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 691");

	}

	@Override
	public void updateAsciiStream(int arg0, InputStream arg1, int arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 691");

	}

	@Override
	public void updateAsciiStream(String arg0, InputStream arg1, int arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 691");

	}

	@Override
	public void updateAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 691");

	}

	@Override
	public void updateAsciiStream(String arg0, InputStream arg1, long arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 691");

	}

	@Override
	public void updateBigDecimal(int arg0, BigDecimal arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 691");

	}

	@Override
	public void updateBigDecimal(String arg0, BigDecimal arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 691");

	}

	@Override
	public void updateBinaryStream(int arg0, InputStream arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 701");

	}

	@Override
	public void updateBinaryStream(String arg0, InputStream arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 701");

	}

	@Override
	public void updateBinaryStream(int arg0, InputStream arg1, int arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 701");

	}

	@Override
	public void updateBinaryStream(String arg0, InputStream arg1, int arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 701");

	}

	@Override
	public void updateBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 701");

	}

	@Override
	public void updateBinaryStream(String arg0, InputStream arg1, long arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 701");

	}

	@Override
	public void updateBlob(int arg0, Blob arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 701");

	}

	@Override
	public void updateBlob(String arg0, Blob arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 701");

	}

	@Override
	public void updateBlob(int arg0, InputStream arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 701");

	}

	@Override
	public void updateBlob(String arg0, InputStream arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 701");

	}

	@Override
	public void updateBlob(int arg0, InputStream arg1, long arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 711");

	}

	@Override
	public void updateBlob(String arg0, InputStream arg1, long arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 711");

	}

	@Override
	public void updateBoolean(int arg0, boolean arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 711");

	}

	@Override
	public void updateBoolean(String arg0, boolean arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 711");

	}

	@Override
	public void updateByte(int arg0, byte arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 711");

	}

	@Override
	public void updateByte(String arg0, byte arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 711");

	}

	@Override
	public void updateBytes(int arg0, byte[] arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 711");

	}

	@Override
	public void updateBytes(String arg0, byte[] arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 711");

	}

	@Override
	public void updateCharacterStream(int arg0, Reader arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 711");

	}

	@Override
	public void updateCharacterStream(String arg0, Reader arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 711");

	}

	@Override
	public void updateCharacterStream(int arg0, Reader arg1, int arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 711");

	}

	@Override
	public void updateCharacterStream(String arg0, Reader arg1, int arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 711");

	}

	@Override
	public void updateCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 711");

	}

	@Override
	public void updateCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 711");

	}

	@Override
	public void updateClob(int arg0, Clob arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 711");

	}

	@Override
	public void updateClob(String arg0, Clob arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 711");

	}

	@Override
	public void updateClob(int arg0, Reader arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 711");

	}

	@Override
	public void updateClob(String arg0, Reader arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 711");

	}

	@Override
	public void updateClob(int arg0, Reader arg1, long arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 711");

	}

	@Override
	public void updateClob(String arg0, Reader arg1, long arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 711");

	}

	@Override
	public void updateDate(int arg0, Date arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 731");

	}

	@Override
	public void updateDate(String arg0, Date arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 731");

	}

	@Override
	public void updateDouble(int arg0, double arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 731");

	}

	@Override
	public void updateDouble(String arg0, double arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 731");

	}

	@Override
	public void updateFloat(int arg0, float arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 731");

	}

	@Override
	public void updateFloat(String arg0, float arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 731");

	}

	@Override
	public void updateInt(int arg0, int arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 731");

	}

	@Override
	public void updateInt(String arg0, int arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 731");

	}

	@Override
	public void updateLong(int arg0, long arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 731");

	}

	@Override
	public void updateLong(String arg0, long arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 731");

	}

	@Override
	public void updateNCharacterStream(int arg0, Reader arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 731");

	}

	@Override
	public void updateNCharacterStream(String arg0, Reader arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 731");

	}

	@Override
	public void updateNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 731");

	}

	@Override
	public void updateNCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 731");

	}

	@Override
	public void updateNClob(int arg0, NClob arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 731");

	}

	@Override
	public void updateNClob(String arg0, NClob arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 731");

	}

	@Override
	public void updateNClob(int arg0, Reader arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 731");

	}

	@Override
	public void updateNClob(String arg0, Reader arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 731");

	}

	@Override
	public void updateNClob(int arg0, Reader arg1, long arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 731");

	}

	@Override
	public void updateNClob(String arg0, Reader arg1, long arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 731");

	}

	@Override
	public void updateNString(int arg0, String arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 741");

	}

	@Override
	public void updateNString(String arg0, String arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 741");

	}

	@Override
	public void updateNull(int arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 741");

	}

	@Override
	public void updateNull(String arg0) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 741");

	}

	@Override
	public void updateObject(int arg0, Object arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 741");

	}

	@Override
	public void updateObject(String arg0, Object arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 741");

	}

	@Override
	public void updateObject(int arg0, Object arg1, int arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 741");

	}

	@Override
	public void updateObject(String arg0, Object arg1, int arg2) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 741");

	}

	@Override
	public void updateRef(int arg0, Ref arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 741");

	}

	@Override
	public void updateRef(String arg0, Ref arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 741");

	}

	@Override
	public void updateRow() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 741");

	}

	@Override
	public void updateRowId(int arg0, RowId arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 741");

	}

	@Override
	public void updateRowId(String arg0, RowId arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 741");

	}

	@Override
	public void updateSQLXML(int arg0, SQLXML arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 741");

	}

	@Override
	public void updateSQLXML(String arg0, SQLXML arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 741");

	}

	@Override
	public void updateShort(int arg0, short arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 741");

	}

	@Override
	public void updateShort(String arg0, short arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 741");

	}

	@Override
	public void updateString(int arg0, String arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 741");

	}

	@Override
	public void updateString(String arg0, String arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 741");

	}

	@Override
	public void updateTime(int arg0, Time arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 741");

	}

	@Override
	public void updateTime(String arg0, Time arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 751");

	}

	@Override
	public void updateTimestamp(int arg0, Timestamp arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 751");

	}

	@Override
	public void updateTimestamp(String arg0, Timestamp arg1) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 751");

	}

	@Override
	public boolean wasNull() throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 751");
		return false;
	}

	public String toString()
	{
		return res.toString();
	}

	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 752");
		return null;
	}

	public <T> T getObject(String columnLabel, Class<T> type) throws SQLException
	{
		System.out.println(" // TODO Auto-generated method stub 753");
		return null;
	}

}