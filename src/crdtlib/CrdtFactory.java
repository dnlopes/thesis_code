/********************************************************************
 Copyright (c) 2013 chengli.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the GNU Public License v2.0
 which accompanies this distribution, and is available at
 http://www.gnu.org/licenses/old-licenses/gpl-2.0.html

 Contributors:
 chengli - initial API and implementation

 Contact:
 To distribute or use this code requires prior specific permission.
 In this case, please contact chengli@mpi-sws.org.
 ********************************************************************/
/**
 *
 */

package crdtlib;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;

import net.sf.jsqlparser.statement.Statement;
import database.invariants.InvariantChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.*;
import runtime.operation.ShadowOperation;
import util.ExitCode;
import util.commonfunc.StringOperations;
import crdtlib.datatypes.primitivetypes.LwwBoolean;
import crdtlib.datatypes.primitivetypes.LwwDateTime;
import crdtlib.datatypes.primitivetypes.LwwDouble;
import crdtlib.datatypes.primitivetypes.LwwFloat;
import crdtlib.datatypes.primitivetypes.LwwInteger;
import crdtlib.datatypes.primitivetypes.LwwString;
import crdtlib.datatypes.primitivetypes.NormalBoolean;
import crdtlib.datatypes.primitivetypes.NormalDateTime;
import crdtlib.datatypes.primitivetypes.NormalDouble;
import crdtlib.datatypes.primitivetypes.NormalFloat;
import crdtlib.datatypes.primitivetypes.NormalInteger;
import crdtlib.datatypes.primitivetypes.NormalString;
import crdtlib.datatypes.primitivetypes.NumberDeltaDateTime;
import crdtlib.datatypes.primitivetypes.NumberDeltaDouble;
import crdtlib.datatypes.primitivetypes.NumberDeltaFloat;
import crdtlib.datatypes.primitivetypes.NumberDeltaInteger;
import crdtlib.datatypes.primitivetypes.PrimitiveType;
import database.util.CrdtDataFieldType;
import database.util.DataField;
import database.util.DatabaseFunction;


/**
 * A factory for creating Crdt objects.
 */
public class CrdtFactory
{

	static final Logger LOG = LoggerFactory.getLogger(CrdtFactory.class);

	/**
	 * Gets the proper crdt object.
	 *
	 * @param crdtType
	 * 		the crdt type
	 * @param originalType
	 * 		the original type
	 *
	 * @return the proper crdt object
	 */
	public static String getProperCrdtObject(CrdtDataFieldType crdtType, String originalType)
	{
		switch(crdtType)
		{
		case LWWINTEGER:
			return "LwwInteger";
		case LWWFLOAT:
			return "LwwFloat";
		case LWWDOUBLE:
			return "LwwDouble";
		case LWWSTRING:
			return "LwwString";
		case LWWDATETIME:
			return "LwwDateTime";
		case LWWLOGICALTIMESTAMP:
			return "LwwLogicalTimestamp";
		case LWWBOOLEAN:
		case LWWDELETEDFLAG:
			return "LwwBoolean";
		case NUMDELTAINTEGER:
			return "NumberDeltaInteger";
		case NUMDELTAFLOAT:
			return "NumberDeltaFloat";
		case NUMDELTADOUBLE:
			return "NumberDeltaDouble";
		case NUMDELTADATETIME:
			return "NumberDeltaDateTime";
		case NONCRDTFIELD:
			return "Normal" + getNormalDataType(originalType);
		case NORMALINTEGER:
			return "NormalInteger";
		case NORMALBOOLEAN:
			return "NormalBoolean";
		case NORMALFLOAT:
			return "NormalFloat";
		case NORMALDOUBLE:
			return "NormalDouble";
		case NORMALSTRING:
			return "NormalString";
		case NORMALDATETIME:
			return "NormalDateTime";
		default:
			System.err.println("not reachable " + crdtType + " " + originalType);
			throw new RuntimeException("not such crdt type");
		}
	}

	/**
	 * Gets the lww logical timestamp crdt type string.
	 *
	 * @return the lww logical timestamp crdt type string
	 */
	public static String getLwwLogicalTimestampCrdtTypeString()
	{
		return "LwwLogicalTimestamp";
	}

	/**
	 * Gets the lww deleted flag.
	 *
	 * @return the lww deleted flag
	 */
	public static String getLwwDeletedFlag()
	{
		return "LwwBoolean";
	}

	/**
	 * Checks if is normal data type.
	 *
	 * @param crdtType
	 * 		the crdt type
	 *
	 * @return true, if is normal data type
	 */
	public static boolean isNormalDataType(CrdtDataFieldType crdtType)
	{
		switch(crdtType)
		{
		case NONCRDTFIELD:
		case NORMALINTEGER:
		case NORMALBOOLEAN:
		case NORMALFLOAT:
		case NORMALDOUBLE:
		case NORMALSTRING:
		case NORMALDATETIME:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Gets the normal data type.
	 *
	 * @param normalDBType
	 * 		the normal db type
	 *
	 * @return the normal data type
	 */
	public static String getNormalDataType(String normalDBType)
	{
		if(normalDBType.toLowerCase().equals("int"))
		{
			return "Integer";
		} else if(normalDBType.toLowerCase().equals("varchar"))
		{
			return "String";
		} else if(normalDBType.toLowerCase().equals("float"))
		{
			return "float";
		} else if(normalDBType.toLowerCase().equals("datetime"))
		{
			return "DateTime";
		} else
		{
			throw new RuntimeException("not implemented normal db type " + normalDBType);
		}
	}

	/**
	 * Checks if is lww type.
	 *
	 * @param crdtType
	 * 		the crdt type
	 *
	 * @return true, if is lww type
	 */
	public static boolean isLwwType(CrdtDataFieldType crdtType)
	{
		switch(crdtType)
		{
		case LWWINTEGER:
		case LWWFLOAT:
		case LWWDOUBLE:
		case LWWSTRING:
		case LWWDATETIME:
		case LWWBOOLEAN:
		case LWWDELETEDFLAG:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Checks if is number delta.
	 *
	 * @param crdtType
	 * 		the crdt type
	 *
	 * @return true, if is number delta
	 */
	public static boolean isNumberDelta(CrdtDataFieldType crdtType)
	{
		switch(crdtType)
		{
		case NUMDELTAINTEGER:
		case NUMDELTAFLOAT:
		case NUMDELTADOUBLE:
		case NUMDELTADATETIME:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Checks if is lww logical timestamp.
	 *
	 * @param crdtType
	 * 		the crdt type
	 *
	 * @return true, if is lww logical timestamp
	 */
	public static boolean isLwwLogicalTimestamp(CrdtDataFieldType crdtType)
	{
		switch(crdtType)
		{
		case LWWLOGICALTIMESTAMP:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Checks if is lww deleted flag.
	 *
	 * @param crdtType
	 * 		the crdt type
	 *
	 * @return true, if is lww deleted flag
	 */
	public static boolean isLwwDeletedFlag(CrdtDataFieldType crdtType)
	{
		switch(crdtType)
		{
		case LWWDELETEDFLAG:
			return true;
		default:
			return false;
		}
	}

	//for runtime shadow operation generation

	/**
	 * Generate crdt primitive type.
	 *
	 * @param df
	 * 		the df
	 * @param value
	 * 		the value
	 * @param rs
	 * 		the rs
	 *
	 * @return the primitive type
	 *
	 * @throws SQLException
	 * 		the sQL exception
	 */
	public static PrimitiveType generateCrdtPrimitiveType(ShadowOperation op, DateFormat dateFormat, DataField df,
														  String value, ResultSet rs, Statement statement)
			throws SQLException
	{
		// this looks like a good place to check invariants
		//if(df.hasInvariants())
		//	InvariantChecker.checkInvariants(op, statement, df, value);

		switch(df.getCrdtType())
		{
		case NONCRDTFIELD:
			throw new RuntimeException("NONCRDT is depreciated");
		case NORMALINTEGER:
			return new NormalInteger(df.getFieldName(), Integer.parseInt(value));
		case NORMALBOOLEAN:
			return new NormalBoolean(df.getFieldName(), Boolean.parseBoolean(value));
		case NORMALFLOAT:
			return new NormalFloat(df.getFieldName(), Float.parseFloat(value));
		case NORMALDOUBLE:
			return new NormalDouble(df.getFieldName(), Double.parseDouble(value));
		case NORMALSTRING:
			return new NormalString(df.getFieldName(), StringOperations.removeQuotesFromHeadTail(value));
		case NORMALDATETIME:
			return new NormalDateTime(df.getFieldName(), DatabaseFunction.convertDateStrToLong(dateFormat, value));
		case LWWINTEGER:
			return new LwwInteger(df.getFieldName(), Integer.parseInt(value));
		case LWWFLOAT:
			return new LwwFloat(df.getFieldName(), Float.parseFloat(value));
		case LWWDOUBLE:
			return new LwwDouble(df.getFieldName(), Double.parseDouble(value));
		case LWWSTRING:
			return new LwwString(df.getFieldName(), StringOperations.removeQuotesFromHeadTail(value));
		case LWWDATETIME:
			return new LwwDateTime(df.getFieldName(), DatabaseFunction.convertDateStrToLong(dateFormat, value));
		case LWWBOOLEAN:
			return new LwwBoolean(df.getFieldName(), Boolean.parseBoolean(value));
		case NUMDELTAINTEGER:
			if(rs != null)
			{
				rs.beforeFirst();
				rs.next();
				int finalIValue = Integer.parseInt(value);
				int oldIValue = rs.getInt(df.getFieldName());
				int iDelta = finalIValue - oldIValue;
				return new NumberDeltaInteger(df.getFieldName(), iDelta);
			} else
			{
				return new NumberDeltaInteger(df.getFieldName(), Integer.parseInt(value));
			}
		case NUMDELTAFLOAT:
			if(rs != null)
			{
				rs.beforeFirst();
				rs.next();
				float finalFValue = Float.parseFloat(value);
				float oldFValue = rs.getFloat(df.getFieldName());
				float fDelta = finalFValue - oldFValue;
				return new NumberDeltaFloat(df.getFieldName(), fDelta);
			} else
			{
				return new NumberDeltaFloat(df.getFieldName(), Float.parseFloat(value));
			}
		case NUMDELTADOUBLE:
			if(rs != null)
			{
				rs.beforeFirst();
				rs.next();
				double finalDValue = Double.parseDouble(value);
				double oldDValue = rs.getDouble(df.getFieldName());
				double dDelta = finalDValue - oldDValue;
				return new NumberDeltaDouble(df.getFieldName(), dDelta);
			} else
			{
				return new NumberDeltaDouble(df.getFieldName(), Double.parseDouble(value));
			}
		case NUMDELTADATETIME:
			if(rs != null)
			{
				rs.beforeFirst();
				rs.next();
				long finalTValue = DatabaseFunction.convertDateStrToLong(dateFormat, value);
				long oldTValue = DatabaseFunction.convertDateStrToLong(dateFormat, rs.getString(df.getFieldName()));
				long tDelta = finalTValue - oldTValue;
				return new NumberDeltaDateTime(df.getFieldName(), tDelta);
			} else
			{
				return new NumberDeltaDateTime(df.getFieldName(),
						DatabaseFunction.convertDateStrToLong(dateFormat, value));
			}
		default:
			LOG.error("cannot create primitive type for field {}", df.getFieldName());
			RuntimeHelper.throwRunTimeException("unkown primitive type", ExitCode.UNKNOWN_PRIMITIVE_TYPET);
			//dumb return, for compiler...
			return null;
		}
	}

	/**
	 * Gets the default value for data field.
	 *
	 * @param df
	 * 		the df
	 *
	 * @return the default value for data field
	 */
	public static String getDefaultValueForDataField(DateFormat dateFormat, DataField df)
	{
		switch(df.getCrdtType())
		{
		case NONCRDTFIELD:
			throw new RuntimeException("NONCRDT is depreciated");
		case NORMALINTEGER:
		case LWWINTEGER:
		case NUMDELTAINTEGER:
			return "0";
		case NORMALBOOLEAN:
		case LWWBOOLEAN:
			return "true";
		case NORMALFLOAT:
		case LWWFLOAT:
		case NUMDELTAFLOAT:
			return "0.0";
		case NORMALDOUBLE:
		case LWWDOUBLE:
		case NUMDELTADOUBLE:
			return "0.0";
		case NORMALSTRING:
		case LWWSTRING:
			return "'abc'";
		case NORMALDATETIME:
		case LWWDATETIME:
		case NUMDELTADATETIME:
			return "'" + DatabaseFunction.CURRENTTIMESTAMP(dateFormat) + "'";
		default:
			System.err.println("cannot get default value for primitive type" + df.toString());
			throw new RuntimeException("not such crdt type");
		}
	}
}
