/**
 * Autogenerated by Thrift Compiler (0.9.2)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package util.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2015-4-1")
public class ThriftOperation implements org.apache.thrift.TBase<ThriftOperation, ThriftOperation._Fields>, java.io.Serializable, Cloneable, Comparable<ThriftOperation> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ThriftOperation");

  private static final org.apache.thrift.protocol.TField TXN_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("txnId", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField OPERATIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("operations", org.apache.thrift.protocol.TType.LIST, (short)2);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new ThriftOperationStandardSchemeFactory());
    schemes.put(TupleScheme.class, new ThriftOperationTupleSchemeFactory());
  }

  public int txnId; // required
  public List<String> operations; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    TXN_ID((short)1, "txnId"),
    OPERATIONS((short)2, "operations");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // TXN_ID
          return TXN_ID;
        case 2: // OPERATIONS
          return OPERATIONS;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __TXNID_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.TXN_ID, new org.apache.thrift.meta_data.FieldMetaData("txnId", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.OPERATIONS, new org.apache.thrift.meta_data.FieldMetaData("operations", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ThriftOperation.class, metaDataMap);
  }

  public ThriftOperation() {
  }

  public ThriftOperation(
    int txnId,
    List<String> operations)
  {
    this();
    this.txnId = txnId;
    setTxnIdIsSet(true);
    this.operations = operations;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public ThriftOperation(ThriftOperation other) {
    __isset_bitfield = other.__isset_bitfield;
    this.txnId = other.txnId;
    if (other.isSetOperations()) {
      List<String> __this__operations = new ArrayList<String>(other.operations);
      this.operations = __this__operations;
    }
  }

  public ThriftOperation deepCopy() {
    return new ThriftOperation(this);
  }

  @Override
  public void clear() {
    setTxnIdIsSet(false);
    this.txnId = 0;
    this.operations = null;
  }

  public int getTxnId() {
    return this.txnId;
  }

  public ThriftOperation setTxnId(int txnId) {
    this.txnId = txnId;
    setTxnIdIsSet(true);
    return this;
  }

  public void unsetTxnId() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __TXNID_ISSET_ID);
  }

  /** Returns true if field txnId is set (has been assigned a value) and false otherwise */
  public boolean isSetTxnId() {
    return EncodingUtils.testBit(__isset_bitfield, __TXNID_ISSET_ID);
  }

  public void setTxnIdIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __TXNID_ISSET_ID, value);
  }

  public int getOperationsSize() {
    return (this.operations == null) ? 0 : this.operations.size();
  }

  public java.util.Iterator<String> getOperationsIterator() {
    return (this.operations == null) ? null : this.operations.iterator();
  }

  public void addToOperations(String elem) {
    if (this.operations == null) {
      this.operations = new ArrayList<String>();
    }
    this.operations.add(elem);
  }

  public List<String> getOperations() {
    return this.operations;
  }

  public ThriftOperation setOperations(List<String> operations) {
    this.operations = operations;
    return this;
  }

  public void unsetOperations() {
    this.operations = null;
  }

  /** Returns true if field operations is set (has been assigned a value) and false otherwise */
  public boolean isSetOperations() {
    return this.operations != null;
  }

  public void setOperationsIsSet(boolean value) {
    if (!value) {
      this.operations = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case TXN_ID:
      if (value == null) {
        unsetTxnId();
      } else {
        setTxnId((Integer)value);
      }
      break;

    case OPERATIONS:
      if (value == null) {
        unsetOperations();
      } else {
        setOperations((List<String>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case TXN_ID:
      return Integer.valueOf(getTxnId());

    case OPERATIONS:
      return getOperations();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case TXN_ID:
      return isSetTxnId();
    case OPERATIONS:
      return isSetOperations();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof ThriftOperation)
      return this.equals((ThriftOperation)that);
    return false;
  }

  public boolean equals(ThriftOperation that) {
    if (that == null)
      return false;

    boolean this_present_txnId = true;
    boolean that_present_txnId = true;
    if (this_present_txnId || that_present_txnId) {
      if (!(this_present_txnId && that_present_txnId))
        return false;
      if (this.txnId != that.txnId)
        return false;
    }

    boolean this_present_operations = true && this.isSetOperations();
    boolean that_present_operations = true && that.isSetOperations();
    if (this_present_operations || that_present_operations) {
      if (!(this_present_operations && that_present_operations))
        return false;
      if (!this.operations.equals(that.operations))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_txnId = true;
    list.add(present_txnId);
    if (present_txnId)
      list.add(txnId);

    boolean present_operations = true && (isSetOperations());
    list.add(present_operations);
    if (present_operations)
      list.add(operations);

    return list.hashCode();
  }

  @Override
  public int compareTo(ThriftOperation other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetTxnId()).compareTo(other.isSetTxnId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTxnId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.txnId, other.txnId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetOperations()).compareTo(other.isSetOperations());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetOperations()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.operations, other.operations);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ThriftOperation(");
    boolean first = true;

    sb.append("txnId:");
    sb.append(this.txnId);
    first = false;
    if (!first) sb.append(", ");
    sb.append("operations:");
    if (this.operations == null) {
      sb.append("null");
    } else {
      sb.append(this.operations);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // alas, we cannot check 'txnId' because it's a primitive and you chose the non-beans generator.
    if (operations == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'operations' was not present! Struct: " + toString());
    }
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class ThriftOperationStandardSchemeFactory implements SchemeFactory {
    public ThriftOperationStandardScheme getScheme() {
      return new ThriftOperationStandardScheme();
    }
  }

  private static class ThriftOperationStandardScheme extends StandardScheme<ThriftOperation> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, ThriftOperation struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // TXN_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.txnId = iprot.readI32();
              struct.setTxnIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // OPERATIONS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list16 = iprot.readListBegin();
                struct.operations = new ArrayList<String>(_list16.size);
                String _elem17;
                for (int _i18 = 0; _i18 < _list16.size; ++_i18)
                {
                  _elem17 = iprot.readString();
                  struct.operations.add(_elem17);
                }
                iprot.readListEnd();
              }
              struct.setOperationsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      if (!struct.isSetTxnId()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'txnId' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, ThriftOperation struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(TXN_ID_FIELD_DESC);
      oprot.writeI32(struct.txnId);
      oprot.writeFieldEnd();
      if (struct.operations != null) {
        oprot.writeFieldBegin(OPERATIONS_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.operations.size()));
          for (String _iter19 : struct.operations)
          {
            oprot.writeString(_iter19);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class ThriftOperationTupleSchemeFactory implements SchemeFactory {
    public ThriftOperationTupleScheme getScheme() {
      return new ThriftOperationTupleScheme();
    }
  }

  private static class ThriftOperationTupleScheme extends TupleScheme<ThriftOperation> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, ThriftOperation struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeI32(struct.txnId);
      {
        oprot.writeI32(struct.operations.size());
        for (String _iter20 : struct.operations)
        {
          oprot.writeString(_iter20);
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, ThriftOperation struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.txnId = iprot.readI32();
      struct.setTxnIdIsSet(true);
      {
        org.apache.thrift.protocol.TList _list21 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
        struct.operations = new ArrayList<String>(_list21.size);
        String _elem22;
        for (int _i23 = 0; _i23 < _list21.size; ++_i23)
        {
          _elem22 = iprot.readString();
          struct.operations.add(_elem22);
        }
      }
      struct.setOperationsIsSet(true);
    }
  }

}

