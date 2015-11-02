/**
 * Autogenerated by Thrift Compiler (0.9.2)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package common.thrift;

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
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2015-10-27")
public class ThriftShadowTransaction implements org.apache.thrift.TBase<ThriftShadowTransaction, ThriftShadowTransaction._Fields>, java.io.Serializable, Cloneable, Comparable<ThriftShadowTransaction> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ThriftShadowTransaction");

  private static final org.apache.thrift.protocol.TField TXN_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("txnId", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField OPERATIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("operations", org.apache.thrift.protocol.TType.MAP, (short)2);
  private static final org.apache.thrift.protocol.TField REQUEST_TO_COORDINATOR_FIELD_DESC = new org.apache.thrift.protocol.TField("requestToCoordinator", org.apache.thrift.protocol.TType.STRUCT, (short)3);
  private static final org.apache.thrift.protocol.TField TEMP_OPERATIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("tempOperations", org.apache.thrift.protocol.TType.MAP, (short)4);
  private static final org.apache.thrift.protocol.TField CLOCK_FIELD_DESC = new org.apache.thrift.protocol.TField("clock", org.apache.thrift.protocol.TType.STRING, (short)5);
  private static final org.apache.thrift.protocol.TField REPLICATOR_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("replicatorId", org.apache.thrift.protocol.TType.I32, (short)6);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new ThriftShadowTransactionStandardSchemeFactory());
    schemes.put(TupleScheme.class, new ThriftShadowTransactionTupleSchemeFactory());
  }

  public int txnId; // required
  public Map<Integer,String> operations; // required
  public CoordinatorRequest requestToCoordinator; // required
  public Map<Integer,String> tempOperations; // required
  public String clock; // required
  public int replicatorId; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    TXN_ID((short)1, "txnId"),
    OPERATIONS((short)2, "operations"),
    REQUEST_TO_COORDINATOR((short)3, "requestToCoordinator"),
    TEMP_OPERATIONS((short)4, "tempOperations"),
    CLOCK((short)5, "clock"),
    REPLICATOR_ID((short)6, "replicatorId");

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
        case 3: // REQUEST_TO_COORDINATOR
          return REQUEST_TO_COORDINATOR;
        case 4: // TEMP_OPERATIONS
          return TEMP_OPERATIONS;
        case 5: // CLOCK
          return CLOCK;
        case 6: // REPLICATOR_ID
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
    tmpMap.put(_Fields.REQUEST_TO_COORDINATOR, new org.apache.thrift.meta_data.FieldMetaData("requestToCoordinator", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, CoordinatorRequest.class)));
    tmpMap.put(_Fields.TEMP_OPERATIONS, new org.apache.thrift.meta_data.FieldMetaData("tempOperations", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32), 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    tmpMap.put(_Fields.CLOCK, new org.apache.thrift.meta_data.FieldMetaData("clock", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.REPLICATOR_ID, new org.apache.thrift.meta_data.FieldMetaData("replicatorId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ThriftShadowTransaction.class, metaDataMap);
  }

  public ThriftShadowTransaction() {
  }

  public ThriftShadowTransaction(
    int txnId,
    Map<Integer,String> operations,
    CoordinatorRequest requestToCoordinator,
    Map<Integer,String> tempOperations,
    String clock,
    int replicatorId)
  {
    this();
    this.txnId = txnId;
    setTxnIdIsSet(true);
    this.operations = operations;
    this.requestToCoordinator = requestToCoordinator;
    this.tempOperations = tempOperations;
    this.clock = clock;
    this.replicatorId = replicatorId;
    setReplicatorIdIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public ThriftShadowTransaction(ThriftShadowTransaction other) {
    __isset_bitfield = other.__isset_bitfield;
    this.txnId = other.txnId;
    if (other.isSetOperations()) {
      Map<Integer,String> __this__operations = new HashMap<Integer,String>(other.operations);
      this.operations = __this__operations;
    }
    if (other.isSetRequestToCoordinator()) {
      this.requestToCoordinator = new CoordinatorRequest(other.requestToCoordinator);
    }
    if (other.isSetTempOperations()) {
      Map<Integer,String> __this__tempOperations = new HashMap<Integer,String>(other.tempOperations);
      this.tempOperations = __this__tempOperations;
    }
    if (other.isSetClock()) {
      this.clock = other.clock;
    }
    this.replicatorId = other.replicatorId;
  }

  public ThriftShadowTransaction deepCopy() {
    return new ThriftShadowTransaction(this);
  }

  @Override
  public void clear() {
    setTxnIdIsSet(false);
    this.txnId = 0;
    this.operations = null;
    this.requestToCoordinator = null;
    this.tempOperations = null;
    this.clock = null;
    setReplicatorIdIsSet(false);
    this.replicatorId = 0;
  }

  public int getTxnId() {
    return this.txnId;
  }

  public ThriftShadowTransaction setTxnId(int txnId) {
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

  public ThriftShadowTransaction setOperations(Map<Integer,String> operations) {
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

  public CoordinatorRequest getRequestToCoordinator() {
    return this.requestToCoordinator;
  }

  public ThriftShadowTransaction setRequestToCoordinator(CoordinatorRequest requestToCoordinator) {
    this.requestToCoordinator = requestToCoordinator;
    return this;
  }

  public void unsetRequestToCoordinator() {
    this.requestToCoordinator = null;
  }

  /** Returns true if field requestToCoordinator is set (has been assigned a value) and false otherwise */
  public boolean isSetRequestToCoordinator() {
    return this.requestToCoordinator != null;
  }

  public void setRequestToCoordinatorIsSet(boolean value) {
    if (!value) {
      this.requestToCoordinator = null;
    }
  }

  public int getTempOperationsSize() {
    return (this.tempOperations == null) ? 0 : this.tempOperations.size();
  }

  public void putToTempOperations(int key, String val) {
    if (this.tempOperations == null) {
      this.tempOperations = new HashMap<Integer,String>();
    }
    this.tempOperations.put(key, val);
  }

  public Map<Integer,String> getTempOperations() {
    return this.tempOperations;
  }

  public ThriftShadowTransaction setTempOperations(Map<Integer,String> tempOperations) {
    this.tempOperations = tempOperations;
    return this;
  }

  public void unsetTempOperations() {
    this.tempOperations = null;
  }

  /** Returns true if field tempOperations is set (has been assigned a value) and false otherwise */
  public boolean isSetTempOperations() {
    return this.tempOperations != null;
  }

  public void setTempOperationsIsSet(boolean value) {
    if (!value) {
      this.tempOperations = null;
    }
  }

  public String getClock() {
    return this.clock;
  }

  public ThriftShadowTransaction setClock(String clock) {
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

  public ThriftShadowTransaction setReplicatorId(int replicatorId) {
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

    case REQUEST_TO_COORDINATOR:
      if (value == null) {
        unsetRequestToCoordinator();
      } else {
        setRequestToCoordinator((CoordinatorRequest)value);
      }
      break;

    case TEMP_OPERATIONS:
      if (value == null) {
        unsetTempOperations();
      } else {
        setTempOperations((Map<Integer,String>)value);
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

    case REQUEST_TO_COORDINATOR:
      return getRequestToCoordinator();

    case TEMP_OPERATIONS:
      return getTempOperations();

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
    case REQUEST_TO_COORDINATOR:
      return isSetRequestToCoordinator();
    case TEMP_OPERATIONS:
      return isSetTempOperations();
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
    if (that instanceof ThriftShadowTransaction)
      return this.equals((ThriftShadowTransaction)that);
    return false;
  }

  public boolean equals(ThriftShadowTransaction that) {
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

    boolean this_present_requestToCoordinator = true && this.isSetRequestToCoordinator();
    boolean that_present_requestToCoordinator = true && that.isSetRequestToCoordinator();
    if (this_present_requestToCoordinator || that_present_requestToCoordinator) {
      if (!(this_present_requestToCoordinator && that_present_requestToCoordinator))
        return false;
      if (!this.requestToCoordinator.equals(that.requestToCoordinator))
        return false;
    }

    boolean this_present_tempOperations = true && this.isSetTempOperations();
    boolean that_present_tempOperations = true && that.isSetTempOperations();
    if (this_present_tempOperations || that_present_tempOperations) {
      if (!(this_present_tempOperations && that_present_tempOperations))
        return false;
      if (!this.tempOperations.equals(that.tempOperations))
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

    boolean present_requestToCoordinator = true && (isSetRequestToCoordinator());
    list.add(present_requestToCoordinator);
    if (present_requestToCoordinator)
      list.add(requestToCoordinator);

    boolean present_tempOperations = true && (isSetTempOperations());
    list.add(present_tempOperations);
    if (present_tempOperations)
      list.add(tempOperations);

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
  public int compareTo(ThriftShadowTransaction other) {
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
    lastComparison = Boolean.valueOf(isSetRequestToCoordinator()).compareTo(other.isSetRequestToCoordinator());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetRequestToCoordinator()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.requestToCoordinator, other.requestToCoordinator);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetTempOperations()).compareTo(other.isSetTempOperations());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTempOperations()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tempOperations, other.tempOperations);
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
    StringBuilder sb = new StringBuilder("ThriftShadowTransaction(");
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
    sb.append("requestToCoordinator:");
    if (this.requestToCoordinator == null) {
      sb.append("null");
    } else {
      sb.append(this.requestToCoordinator);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("tempOperations:");
    if (this.tempOperations == null) {
      sb.append("null");
    } else {
      sb.append(this.tempOperations);
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
    // check for sub-struct validity
    if (requestToCoordinator != null) {
      requestToCoordinator.validate();
    }
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

  private static class ThriftShadowTransactionStandardSchemeFactory implements SchemeFactory {
    public ThriftShadowTransactionStandardScheme getScheme() {
      return new ThriftShadowTransactionStandardScheme();
    }
  }

  private static class ThriftShadowTransactionStandardScheme extends StandardScheme<ThriftShadowTransaction> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, ThriftShadowTransaction struct) throws org.apache.thrift.TException {
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
                org.apache.thrift.protocol.TMap _map108 = iprot.readMapBegin();
                struct.operations = new HashMap<Integer,String>(2*_map108.size);
                int _key109;
                String _val110;
                for (int _i111 = 0; _i111 < _map108.size; ++_i111)
                {
                  _key109 = iprot.readI32();
                  _val110 = iprot.readString();
                  struct.operations.put(_key109, _val110);
                }
                iprot.readMapEnd();
              }
              struct.setOperationsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // REQUEST_TO_COORDINATOR
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.requestToCoordinator = new CoordinatorRequest();
              struct.requestToCoordinator.read(iprot);
              struct.setRequestToCoordinatorIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // TEMP_OPERATIONS
            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.thrift.protocol.TMap _map112 = iprot.readMapBegin();
                struct.tempOperations = new HashMap<Integer,String>(2*_map112.size);
                int _key113;
                String _val114;
                for (int _i115 = 0; _i115 < _map112.size; ++_i115)
                {
                  _key113 = iprot.readI32();
                  _val114 = iprot.readString();
                  struct.tempOperations.put(_key113, _val114);
                }
                iprot.readMapEnd();
              }
              struct.setTempOperationsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // CLOCK
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.clock = iprot.readString();
              struct.setClockIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // REPLICATOR_ID
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
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, ThriftShadowTransaction struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(TXN_ID_FIELD_DESC);
      oprot.writeI32(struct.txnId);
      oprot.writeFieldEnd();
      if (struct.operations != null) {
        oprot.writeFieldBegin(OPERATIONS_FIELD_DESC);
        {
          oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.I32, org.apache.thrift.protocol.TType.STRING, struct.operations.size()));
          for (Map.Entry<Integer, String> _iter116 : struct.operations.entrySet())
          {
            oprot.writeI32(_iter116.getKey());
            oprot.writeString(_iter116.getValue());
          }
          oprot.writeMapEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.requestToCoordinator != null) {
        oprot.writeFieldBegin(REQUEST_TO_COORDINATOR_FIELD_DESC);
        struct.requestToCoordinator.write(oprot);
        oprot.writeFieldEnd();
      }
      if (struct.tempOperations != null) {
        oprot.writeFieldBegin(TEMP_OPERATIONS_FIELD_DESC);
        {
          oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.I32, org.apache.thrift.protocol.TType.STRING, struct.tempOperations.size()));
          for (Map.Entry<Integer, String> _iter117 : struct.tempOperations.entrySet())
          {
            oprot.writeI32(_iter117.getKey());
            oprot.writeString(_iter117.getValue());
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

  private static class ThriftShadowTransactionTupleSchemeFactory implements SchemeFactory {
    public ThriftShadowTransactionTupleScheme getScheme() {
      return new ThriftShadowTransactionTupleScheme();
    }
  }

  private static class ThriftShadowTransactionTupleScheme extends TupleScheme<ThriftShadowTransaction> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, ThriftShadowTransaction struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeI32(struct.txnId);
      {
        oprot.writeI32(struct.operations.size());
        for (Map.Entry<Integer, String> _iter118 : struct.operations.entrySet())
        {
          oprot.writeI32(_iter118.getKey());
          oprot.writeString(_iter118.getValue());
        }
      }
      BitSet optionals = new BitSet();
      if (struct.isSetRequestToCoordinator()) {
        optionals.set(0);
      }
      if (struct.isSetTempOperations()) {
        optionals.set(1);
      }
      if (struct.isSetClock()) {
        optionals.set(2);
      }
      if (struct.isSetReplicatorId()) {
        optionals.set(3);
      }
      oprot.writeBitSet(optionals, 4);
      if (struct.isSetRequestToCoordinator()) {
        struct.requestToCoordinator.write(oprot);
      }
      if (struct.isSetTempOperations()) {
        {
          oprot.writeI32(struct.tempOperations.size());
          for (Map.Entry<Integer, String> _iter119 : struct.tempOperations.entrySet())
          {
            oprot.writeI32(_iter119.getKey());
            oprot.writeString(_iter119.getValue());
          }
        }
      }
      if (struct.isSetClock()) {
        oprot.writeString(struct.clock);
      }
      if (struct.isSetReplicatorId()) {
        oprot.writeI32(struct.replicatorId);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, ThriftShadowTransaction struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.txnId = iprot.readI32();
      struct.setTxnIdIsSet(true);
      {
        org.apache.thrift.protocol.TMap _map120 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.I32, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
        struct.operations = new HashMap<Integer,String>(2*_map120.size);
        int _key121;
        String _val122;
        for (int _i123 = 0; _i123 < _map120.size; ++_i123)
        {
          _key121 = iprot.readI32();
          _val122 = iprot.readString();
          struct.operations.put(_key121, _val122);
        }
      }
      struct.setOperationsIsSet(true);
      BitSet incoming = iprot.readBitSet(4);
      if (incoming.get(0)) {
        struct.requestToCoordinator = new CoordinatorRequest();
        struct.requestToCoordinator.read(iprot);
        struct.setRequestToCoordinatorIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.thrift.protocol.TMap _map124 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.I32, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.tempOperations = new HashMap<Integer,String>(2*_map124.size);
          int _key125;
          String _val126;
          for (int _i127 = 0; _i127 < _map124.size; ++_i127)
          {
            _key125 = iprot.readI32();
            _val126 = iprot.readString();
            struct.tempOperations.put(_key125, _val126);
          }
        }
        struct.setTempOperationsIsSet(true);
      }
      if (incoming.get(2)) {
        struct.clock = iprot.readString();
        struct.setClockIsSet(true);
      }
      if (incoming.get(3)) {
        struct.replicatorId = iprot.readI32();
        struct.setReplicatorIdIsSet(true);
      }
    }
  }

}
