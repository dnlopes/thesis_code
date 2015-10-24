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
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2015-10-23")
public class ThriftOperation implements org.apache.thrift.TBase<ThriftOperation, ThriftOperation._Fields>, java.io.Serializable, Cloneable, Comparable<ThriftOperation> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ThriftOperation");

  private static final org.apache.thrift.protocol.TField TXN_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("txnId", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField OPERATIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("operations", org.apache.thrift.protocol.TType.MAP, (short)2);
  private static final org.apache.thrift.protocol.TField CLOCK_FIELD_DESC = new org.apache.thrift.protocol.TField("clock", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField REPLICATOR_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("replicatorId", org.apache.thrift.protocol.TType.I32, (short)4);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new ThriftOperationStandardSchemeFactory());
    schemes.put(TupleScheme.class, new ThriftOperationTupleSchemeFactory());
  }

  public int txnId; // required
  public Map<Integer,String> operations; // required
  public String clock; // required
  public int replicatorId; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    TXN_ID((short)1, "txnId"),
    OPERATIONS((short)2, "operations"),
    CLOCK((short)3, "clock"),
    REPLICATOR_ID((short)4, "replicatorId");

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
        case 3: // CLOCK
          return CLOCK;
        case 4: // REPLICATOR_ID
          return REPLICATOR_ID;
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
  private static final int __REPLICATORID_ISSET_ID = 1;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.TXN_ID, new org.apache.thrift.meta_data.FieldMetaData("txnId", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.OPERATIONS, new org.apache.thrift.meta_data.FieldMetaData("operations", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32), 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    tmpMap.put(_Fields.CLOCK, new org.apache.thrift.meta_data.FieldMetaData("clock", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.REPLICATOR_ID, new org.apache.thrift.meta_data.FieldMetaData("replicatorId", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ThriftOperation.class, metaDataMap);
  }

  public ThriftOperation() {
  }

  public ThriftOperation(
    int txnId,
    Map<Integer,String> operations,
    String clock,
    int replicatorId)
  {
    this();
    this.txnId = txnId;
    setTxnIdIsSet(true);
    this.operations = operations;
    this.clock = clock;
    this.replicatorId = replicatorId;
    setReplicatorIdIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public ThriftOperation(ThriftOperation other) {
    __isset_bitfield = other.__isset_bitfield;
    this.txnId = other.txnId;
    if (other.isSetOperations()) {
      Map<Integer,String> __this__operations = new HashMap<Integer,String>(other.operations);
      this.operations = __this__operations;
    }
    if (other.isSetClock()) {
      this.clock = other.clock;
    }
    this.replicatorId = other.replicatorId;
  }

  public ThriftOperation deepCopy() {
    return new ThriftOperation(this);
  }

  @Override
  public void clear() {
    setTxnIdIsSet(false);
    this.txnId = 0;
    this.operations = null;
    this.clock = null;
    setReplicatorIdIsSet(false);
    this.replicatorId = 0;
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

  public void putToOperations(int key, String val) {
    if (this.operations == null) {
      this.operations = new HashMap<Integer,String>();
    }
    this.operations.put(key, val);
  }

  public Map<Integer,String> getOperations() {
    return this.operations;
  }

  public ThriftOperation setOperations(Map<Integer,String> operations) {
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

  public String getClock() {
    return this.clock;
  }

  public ThriftOperation setClock(String clock) {
    this.clock = clock;
    return this;
  }

  public void unsetClock() {
    this.clock = null;
  }

  /** Returns true if field clock is set (has been assigned a value) and false otherwise */
  public boolean isSetClock() {
    return this.clock != null;
  }

  public void setClockIsSet(boolean value) {
    if (!value) {
      this.clock = null;
    }
  }

  public int getReplicatorId() {
    return this.replicatorId;
  }

  public ThriftOperation setReplicatorId(int replicatorId) {
    this.replicatorId = replicatorId;
    setReplicatorIdIsSet(true);
    return this;
  }

  public void unsetReplicatorId() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __REPLICATORID_ISSET_ID);
  }

  /** Returns true if field replicatorId is set (has been assigned a value) and false otherwise */
  public boolean isSetReplicatorId() {
    return EncodingUtils.testBit(__isset_bitfield, __REPLICATORID_ISSET_ID);
  }

  public void setReplicatorIdIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __REPLICATORID_ISSET_ID, value);
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
        setOperations((Map<Integer,String>)value);
      }
      break;

    case CLOCK:
      if (value == null) {
        unsetClock();
      } else {
        setClock((String)value);
      }
      break;

    case REPLICATOR_ID:
      if (value == null) {
        unsetReplicatorId();
      } else {
        setReplicatorId((Integer)value);
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

    case CLOCK:
      return getClock();

    case REPLICATOR_ID:
      return Integer.valueOf(getReplicatorId());

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
    case CLOCK:
      return isSetClock();
    case REPLICATOR_ID:
      return isSetReplicatorId();
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

    boolean this_present_clock = true && this.isSetClock();
    boolean that_present_clock = true && that.isSetClock();
    if (this_present_clock || that_present_clock) {
      if (!(this_present_clock && that_present_clock))
        return false;
      if (!this.clock.equals(that.clock))
        return false;
    }

    boolean this_present_replicatorId = true;
    boolean that_present_replicatorId = true;
    if (this_present_replicatorId || that_present_replicatorId) {
      if (!(this_present_replicatorId && that_present_replicatorId))
        return false;
      if (this.replicatorId != that.replicatorId)
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

    boolean present_clock = true && (isSetClock());
    list.add(present_clock);
    if (present_clock)
      list.add(clock);

    boolean present_replicatorId = true;
    list.add(present_replicatorId);
    if (present_replicatorId)
      list.add(replicatorId);

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
    lastComparison = Boolean.valueOf(isSetClock()).compareTo(other.isSetClock());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetClock()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.clock, other.clock);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetReplicatorId()).compareTo(other.isSetReplicatorId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetReplicatorId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.replicatorId, other.replicatorId);
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
    if (!first) sb.append(", ");
    sb.append("clock:");
    if (this.clock == null) {
      sb.append("null");
    } else {
      sb.append(this.clock);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("replicatorId:");
    sb.append(this.replicatorId);
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
    // alas, we cannot check 'replicatorId' because it's a primitive and you chose the non-beans generator.
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
            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.thrift.protocol.TMap _map98 = iprot.readMapBegin();
                struct.operations = new HashMap<Integer,String>(2*_map98.size);
                int _key99;
                String _val100;
                for (int _i101 = 0; _i101 < _map98.size; ++_i101)
                {
                  _key99 = iprot.readI32();
                  _val100 = iprot.readString();
                  struct.operations.put(_key99, _val100);
                }
                iprot.readMapEnd();
              }
              struct.setOperationsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // CLOCK
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.clock = iprot.readString();
              struct.setClockIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // REPLICATOR_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.replicatorId = iprot.readI32();
              struct.setReplicatorIdIsSet(true);
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
      if (!struct.isSetReplicatorId()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'replicatorId' was not found in serialized data! Struct: " + toString());
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
          oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.I32, org.apache.thrift.protocol.TType.STRING, struct.operations.size()));
          for (Map.Entry<Integer, String> _iter102 : struct.operations.entrySet())
          {
            oprot.writeI32(_iter102.getKey());
            oprot.writeString(_iter102.getValue());
          }
          oprot.writeMapEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.clock != null) {
        oprot.writeFieldBegin(CLOCK_FIELD_DESC);
        oprot.writeString(struct.clock);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(REPLICATOR_ID_FIELD_DESC);
      oprot.writeI32(struct.replicatorId);
      oprot.writeFieldEnd();
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
        for (Map.Entry<Integer, String> _iter103 : struct.operations.entrySet())
        {
          oprot.writeI32(_iter103.getKey());
          oprot.writeString(_iter103.getValue());
        }
      }
      oprot.writeI32(struct.replicatorId);
      BitSet optionals = new BitSet();
      if (struct.isSetClock()) {
        optionals.set(0);
      }
      oprot.writeBitSet(optionals, 1);
      if (struct.isSetClock()) {
        oprot.writeString(struct.clock);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, ThriftOperation struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.txnId = iprot.readI32();
      struct.setTxnIdIsSet(true);
      {
        org.apache.thrift.protocol.TMap _map104 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.I32, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
        struct.operations = new HashMap<Integer,String>(2*_map104.size);
        int _key105;
        String _val106;
        for (int _i107 = 0; _i107 < _map104.size; ++_i107)
        {
          _key105 = iprot.readI32();
          _val106 = iprot.readString();
          struct.operations.put(_key105, _val106);
        }
      }
      struct.setOperationsIsSet(true);
      struct.replicatorId = iprot.readI32();
      struct.setReplicatorIdIsSet(true);
      BitSet incoming = iprot.readBitSet(1);
      if (incoming.get(0)) {
        struct.clock = iprot.readString();
        struct.setClockIsSet(true);
      }
    }
  }

}

