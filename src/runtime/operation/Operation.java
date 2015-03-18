package runtime.operation;

import util.UnsignedTypes;


public class Operation implements java.io.Serializable {

	public byte[] op;
	public String[] pk;

	public Operation(byte[] b) {
		op = b;
	}

	public Operation(byte b[], int offset) {

		long length = UnsignedTypes.bytesToLong(b, offset);
		offset += UnsignedTypes.uint32Size;
		op = new byte[(int) length];
		for (int i = 0; i < op.length; i++)
			op[i] = b[i + offset];
	}

	public void getBytes(byte[] b, int offset) {
		UnsignedTypes.longToBytes(op.length, b, offset);
		offset += UnsignedTypes.uint32Size;
		for (int i = 0; i < op.length; i++)
			b[i + offset] = op[i];
	}

	public final int getByteSize() {
		return op.length + UnsignedTypes.uint32Size;
	}

	public byte[] getOperation() {
		return op;
	}



	//public void clear();
	//public void addOperationEntry(/*OpEntry entry*/);
	//public boolean executeOperation();
	/*
	public Object getStatementObj()
	{
		RuntimeHelper.throwRunTimeException("unimplemented method", ExitCode.MISSING_IMPLEMENTATION);
		return null;
	}
	public Object getStatement()
	{
		RuntimeHelper.throwRunTimeException("unimplemented method", ExitCode.MISSING_IMPLEMENTATION);
		return null;
	}
	public Object executeDefOpUpdate()
	{
		RuntimeHelper.throwRunTimeException("unimplemented method", ExitCode.MISSING_IMPLEMENTATION);
		return null;
	}
	public Object executeDefOpDelete()
	{
		RuntimeHelper.throwRunTimeException("unimplemented method", ExitCode.MISSING_IMPLEMENTATION);
		return null;
	}
	public Object executeDefOpInsert()
	{
		RuntimeHelper.throwRunTimeException("unimplemented method", ExitCode.MISSING_IMPLEMENTATION);
		return null;
	}
                                 */

}
