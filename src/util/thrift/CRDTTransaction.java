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
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2015-10-20")
public class CRDTTransaction implements org.apache.thrift.TBase<CRDTTransaction, CRDTTransaction._Fields>, java.io.Serializable, Cloneable, Comparable<CRDTTransaction> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("CRDTTransaction");

  private static final org.apache.thrift.protocol.TField ID_FIELD_DESC = new org.apache.thrift.protocol.TField("id", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField REPLICATOR_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("replicatorId", org.apache.thrift.protocol.TType.I32, (short)2);
  private static final org.apache.thrift.protocol.TField TXN_CLOCK_FIELD_DESC = new org.apache.thrift.protocol.TField("txnClock", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField OPS_LIST_FIELD_DESC = new org.apache.thrift.protocol.TField("opsList", org.apache.thrift.protocol.TType.LIST, (short)4);
  private static final org.apache.thrift.protocol.TField REQUEST_TO_COORDINATOR_FIELD_DESC = new org.apache.thrift.protocol.TField("requestToCoordinator", org.apache.thrift.protocol.TType.STRUCT, (short)5);
  private static final org.apache.thrift.protocol.TField COMPILED_TXN_FIELD_DESC = new org.apache.thrift.protocol.TField("compiledTxn", org.apache.thrift.protocol.TType.STRUCT, (short)6);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new CRDTTransactionStandardSchemeFactory());
    schemes.put(TupleScheme.class, new CRDTTransactionTupleSchemeFactory());
  }

  public int id; // required
  public int replicatorId; // required
  public String txnClock; // required
  public List<CRDTOperation> opsList; // required
  public CoordinatorRequest requestToCoordinator; // optional
  public CRDTCompiledTransaction compiledTxn; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    ID((short)1, "id"),
    REPLICATOR_ID((short)2, "replicatorId"),
    TXN_CLOCK((short)3, "txnClock"),
    OPS_LIST((short)4, "opsList"),
    REQUEST_TO_COORDINATOR((short)5, "requestToCoordinator"),
    COMPILED_TXN((short)6, "compiledTxn");

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
        case 1: // ID
          return ID;
        case 2: // REPLICATOR_ID
          return REPLICATOR_ID;
        case 3: // TXN_CLOCK
          return TXN_CLOCK;
        case 4: // OPS_LIST
          return OPS_LIST;
        case 5: // REQUEST_TO_COORDINATOR
          return REQUEST_TO_COORDINATOR;
        case 6: // COMPILED_TXN
          return COMPILED_TXN;
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
  private static final int __ID_ISSET_ID = 0;
  private static final int __REPLICATORID_ISSET_ID = 1;
  private byte __isset_bitfield = 0;
  private static final _Fields optionals[] = {_Fields.REQUEST_TO_COORDINATOR,_Fields.COMPILED_TXN};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.ID, new org.apache.thrift.meta_data.FieldMetaData("id", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.REPLICATOR_ID, new org.apache.thrift.meta_data.FieldMetaData("replicatorId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.TXN_CLOCK, new org.apache.thrift.meta_data.FieldMetaData("txnClock", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.OPS_LIST, new org.apache.thrift.meta_data.FieldMetaData("opsList", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, CRDTOperation.class))));
    tmpMap.put(_Fields.REQUEST_TO_COORDINATOR, new org.apache.thrift.meta_data.FieldMetaData("requestToCoordinator", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, CoordinatorRequest.class)));
    tmpMap.put(_Fields.COMPILED_TXN, new org.apache.thrift.meta_data.FieldMetaData("compiledTxn", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT        , "CRDTCompiledTransaction")));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(CRDTTransaction.class, metaDataMap);
  }

  public CRDTTransaction() {
  }

  public CRDTTransaction(
    int id,
    int replicatorId,
    String txnClock,
    List<CRDTOperation> opsList)
  {
    this();
    this.id = id;
    setIdIsSet(true);
    this.replicatorId = replicatorId;
    setReplicatorIdIsSet(true);
    this.txnClock = txnClock;
    this.opsList = opsList;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public CRDTTransaction(CRDTTransaction other) {
    __isset_bitfield = other.__isset_bitfield;
    this.id = other.id;
    this.replicatorId = other.replicatorId;
    if (other.isSetTxnClock()) {
      this.txnClock = other.txnClock;
    }
    if (other.isSetOpsList()) {
      List<CRDTOperation> __this__opsList = new ArrayList<CRDTOperation>(other.opsList.size());
      for (CRDTOperation other_element : other.opsList) {
        __this__opsList.add(new CRDTOperation(other_element));
      }
      this.opsList = __this__opsList;
    }
    if (other.isSetRequestToCoordinator()) {
      this.requestToCoordinator = new CoordinatorRequest(other.requestToCoordinator);
    }
    if (other.isSetCompiledTxn()) {
      this.compiledTxn = other.compiledTxn;
    }
  }

  public CRDTTransaction deepCopy() {
    return new CRDTTransaction(this);
  }

  @Override
  public void clear() {
    setIdIsSet(false);
    this.id = 0;
    setReplicatorIdIsSet(false);
    this.replicatorId = 0;
    this.txnClock = null;
    this.opsList = null;
    this.requestToCoordinator = null;
    this.compiledTxn = null;
  }

  public int getId() {
    return this.id;
  }

  public CRDTTransaction setId(int id) {
    this.id = id;
    setIdIsSet(true);
    return this;
  }

  public void unsetId() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __ID_ISSET_ID);
  }

  /** Returns true if field id is set (has been assigned a value) and false otherwise */
  public boolean isSetId() {
    return EncodingUtils.testBit(__isset_bitfield, __ID_ISSET_ID);
  }

  public void setIdIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __ID_ISSET_ID, value);
  }

  public int getReplicatorId() {
    return this.replicatorId;
  }

  public CRDTTransaction setReplicatorId(int replicatorId) {
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

  public String getTxnClock() {
    return this.txnClock;
  }

  public CRDTTransaction setTxnClock(String txnClock) {
    this.txnClock = txnClock;
    return this;
  }

  public void unsetTxnClock() {
    this.txnClock = null;
  }

  /** Returns true if field txnClock is set (has been assigned a value) and false otherwise */
  public boolean isSetTxnClock() {
    return this.txnClock != null;
  }

  public void setTxnClockIsSet(boolean value) {
    if (!value) {
      this.txnClock = null;
    }
  }

  public int getOpsListSize() {
    return (this.opsList == null) ? 0 : this.opsList.size();
  }

  public java.util.Iterator<CRDTOperation> getOpsListIterator() {
    return (this.opsList == null) ? null : this.opsList.iterator();
  }

  public void addToOpsList(CRDTOperation elem) {
    if (this.opsList == null) {
      this.opsList = new ArrayList<CRDTOperation>();
    }
    this.opsList.add(elem);
  }

  public List<CRDTOperation> getOpsList() {
    return this.opsList;
  }

  public CRDTTransaction setOpsList(List<CRDTOperation> opsList) {
    this.opsList = opsList;
    return this;
  }

  public void unsetOpsList() {
    this.opsList = null;
  }

  /** Returns true if field opsList is set (has been assigned a value) and false otherwise */
  public boolean isSetOpsList() {
    return this.opsList != null;
  }

  public void setOpsListIsSet(boolean value) {
    if (!value) {
      this.opsList = null;
    }
  }

  public CoordinatorRequest getRequestToCoordinator() {
    return this.requestToCoordinator;
  }

  public CRDTTransaction setRequestToCoordinator(CoordinatorRequest requestToCoordinator) {
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

  public CRDTCompiledTransaction getCompiledTxn() {
    return this.compiledTxn;
  }

  public CRDTTransaction setCompiledTxn(CRDTCompiledTransaction compiledTxn) {
    this.compiledTxn = compiledTxn;
    return this;
  }

  public void unsetCompiledTxn() {
    this.compiledTxn = null;
  }

  /** Returns true if field compiledTxn is set (has been assigned a value) and false otherwise */
  public boolean isSetCompiledTxn() {
    return this.compiledTxn != null;
  }

  public void setCompiledTxnIsSet(boolean value) {
    if (!value) {
      this.compiledTxn = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case ID:
      if (value == null) {
        unsetId();
      } else {
        setId((Integer)value);
      }
      break;

    case REPLICATOR_ID:
      if (value == null) {
        unsetReplicatorId();
      } else {
        setReplicatorId((Integer)value);
      }
      break;

    case TXN_CLOCK:
      if (value == null) {
        unsetTxnClock();
      } else {
        setTxnClock((String)value);
      }
      break;

    case OPS_LIST:
      if (value == null) {
        unsetOpsList();
      } else {
        setOpsList((List<CRDTOperation>)value);
      }
      break;

    case REQUEST_TO_COORDINATOR:
      if (value == null) {
        unsetRequestToCoordinator();
      } else {
        setRequestToCoordinator((CoordinatorRequest)value);
      }
      break;

    case COMPILED_TXN:
      if (value == null) {
        unsetCompiledTxn();
      } else {
        setCompiledTxn((CRDTCompiledTransaction)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case ID:
      return Integer.valueOf(getId());

    case REPLICATOR_ID:
      return Integer.valueOf(getReplicatorId());

    case TXN_CLOCK:
      return getTxnClock();

    case OPS_LIST:
      return getOpsList();

    case REQUEST_TO_COORDINATOR:
      return getRequestToCoordinator();

    case COMPILED_TXN:
      return getCompiledTxn();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case ID:
      return isSetId();
    case REPLICATOR_ID:
      return isSetReplicatorId();
    case TXN_CLOCK:
      return isSetTxnClock();
    case OPS_LIST:
      return isSetOpsList();
    case REQUEST_TO_COORDINATOR:
      return isSetRequestToCoordinator();
    case COMPILED_TXN:
      return isSetCompiledTxn();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof CRDTTransaction)
      return this.equals((CRDTTransaction)that);
    return false;
  }

  public boolean equals(CRDTTransaction that) {
    if (that == null)
      return false;

    boolean this_present_id = true;
    boolean that_present_id = true;
    if (this_present_id || that_present_id) {
      if (!(this_present_id && that_present_id))
        return false;
      if (this.id != that.id)
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

    boolean this_present_txnClock = true && this.isSetTxnClock();
    boolean that_present_txnClock = true && that.isSetTxnClock();
    if (this_present_txnClock || that_present_txnClock) {
      if (!(this_present_txnClock && that_present_txnClock))
        return false;
      if (!this.txnClock.equals(that.txnClock))
        return false;
    }

    boolean this_present_opsList = true && this.isSetOpsList();
    boolean that_present_opsList = true && that.isSetOpsList();
    if (this_present_opsList || that_present_opsList) {
      if (!(this_present_opsList && that_present_opsList))
        return false;
      if (!this.opsList.equals(that.opsList))
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

    boolean this_present_compiledTxn = true && this.isSetCompiledTxn();
    boolean that_present_compiledTxn = true && that.isSetCompiledTxn();
    if (this_present_compiledTxn || that_present_compiledTxn) {
      if (!(this_present_compiledTxn && that_present_compiledTxn))
        return false;
      if (!this.compiledTxn.equals(that.compiledTxn))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_id = true;
    list.add(present_id);
    if (present_id)
      list.add(id);

    boolean present_replicatorId = true;
    list.add(present_replicatorId);
    if (present_replicatorId)
      list.add(replicatorId);

    boolean present_txnClock = true && (isSetTxnClock());
    list.add(present_txnClock);
    if (present_txnClock)
      list.add(txnClock);

    boolean present_opsList = true && (isSetOpsList());
    list.add(present_opsList);
    if (present_opsList)
      list.add(opsList);

    boolean present_requestToCoordinator = true && (isSetRequestToCoordinator());
    list.add(present_requestToCoordinator);
    if (present_requestToCoordinator)
      list.add(requestToCoordinator);

    boolean present_compiledTxn = true && (isSetCompiledTxn());
    list.add(present_compiledTxn);
    if (present_compiledTxn)
      list.add(compiledTxn);

    return list.hashCode();
  }

  @Override
  public int compareTo(CRDTTransaction other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetId()).compareTo(other.isSetId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.id, other.id);
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
    lastComparison = Boolean.valueOf(isSetTxnClock()).compareTo(other.isSetTxnClock());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTxnClock()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.txnClock, other.txnClock);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetOpsList()).compareTo(other.isSetOpsList());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetOpsList()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.opsList, other.opsList);
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
    lastComparison = Boolean.valueOf(isSetCompiledTxn()).compareTo(other.isSetCompiledTxn());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetCompiledTxn()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.compiledTxn, other.compiledTxn);
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
    StringBuilder sb = new StringBuilder("CRDTTransaction(");
    boolean first = true;

    sb.append("id:");
    sb.append(this.id);
    first = false;
    if (!first) sb.append(", ");
    sb.append("replicatorId:");
    sb.append(this.replicatorId);
    first = false;
    if (!first) sb.append(", ");
    sb.append("txnClock:");
    if (this.txnClock == null) {
      sb.append("null");
    } else {
      sb.append(this.txnClock);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("opsList:");
    if (this.opsList == null) {
      sb.append("null");
    } else {
      sb.append(this.opsList);
    }
    first = false;
    if (isSetRequestToCoordinator()) {
      if (!first) sb.append(", ");
      sb.append("requestToCoordinator:");
      if (this.requestToCoordinator == null) {
        sb.append("null");
      } else {
        sb.append(this.requestToCoordinator);
      }
      first = false;
    }
    if (isSetCompiledTxn()) {
      if (!first) sb.append(", ");
      sb.append("compiledTxn:");
      if (this.compiledTxn == null) {
        sb.append("null");
      } else {
        sb.append(this.compiledTxn);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // alas, we cannot check 'id' because it's a primitive and you chose the non-beans generator.
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

  private static class CRDTTransactionStandardSchemeFactory implements SchemeFactory {
    public CRDTTransactionStandardScheme getScheme() {
      return new CRDTTransactionStandardScheme();
    }
  }

  private static class CRDTTransactionStandardScheme extends StandardScheme<CRDTTransaction> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, CRDTTransaction struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.id = iprot.readI32();
              struct.setIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // REPLICATOR_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.replicatorId = iprot.readI32();
              struct.setReplicatorIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // TXN_CLOCK
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.txnClock = iprot.readString();
              struct.setTxnClockIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // OPS_LIST
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list62 = iprot.readListBegin();
                struct.opsList = new ArrayList<CRDTOperation>(_list62.size);
                CRDTOperation _elem63;
                for (int _i64 = 0; _i64 < _list62.size; ++_i64)
                {
                  _elem63 = new CRDTOperation();
                  _elem63.read(iprot);
                  struct.opsList.add(_elem63);
                }
                iprot.readListEnd();
              }
              struct.setOpsListIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // REQUEST_TO_COORDINATOR
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.requestToCoordinator = new CoordinatorRequest();
              struct.requestToCoordinator.read(iprot);
              struct.setRequestToCoordinatorIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // COMPILED_TXN
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.compiledTxn = new CRDTCompiledTransaction();
              struct.compiledTxn.read(iprot);
              struct.setCompiledTxnIsSet(true);
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
      if (!struct.isSetId()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'id' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, CRDTTransaction struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(ID_FIELD_DESC);
      oprot.writeI32(struct.id);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(REPLICATOR_ID_FIELD_DESC);
      oprot.writeI32(struct.replicatorId);
      oprot.writeFieldEnd();
      if (struct.txnClock != null) {
        oprot.writeFieldBegin(TXN_CLOCK_FIELD_DESC);
        oprot.writeString(struct.txnClock);
        oprot.writeFieldEnd();
      }
      if (struct.opsList != null) {
        oprot.writeFieldBegin(OPS_LIST_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.opsList.size()));
          for (CRDTOperation _iter65 : struct.opsList)
          {
            _iter65.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.requestToCoordinator != null) {
        if (struct.isSetRequestToCoordinator()) {
          oprot.writeFieldBegin(REQUEST_TO_COORDINATOR_FIELD_DESC);
          struct.requestToCoordinator.write(oprot);
          oprot.writeFieldEnd();
        }
      }
      if (struct.compiledTxn != null) {
        if (struct.isSetCompiledTxn()) {
          oprot.writeFieldBegin(COMPILED_TXN_FIELD_DESC);
          struct.compiledTxn.write(oprot);
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class CRDTTransactionTupleSchemeFactory implements SchemeFactory {
    public CRDTTransactionTupleScheme getScheme() {
      return new CRDTTransactionTupleScheme();
    }
  }

  private static class CRDTTransactionTupleScheme extends TupleScheme<CRDTTransaction> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, CRDTTransaction struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeI32(struct.id);
      BitSet optionals = new BitSet();
      if (struct.isSetReplicatorId()) {
        optionals.set(0);
      }
      if (struct.isSetTxnClock()) {
        optionals.set(1);
      }
      if (struct.isSetOpsList()) {
        optionals.set(2);
      }
      if (struct.isSetRequestToCoordinator()) {
        optionals.set(3);
      }
      if (struct.isSetCompiledTxn()) {
        optionals.set(4);
      }
      oprot.writeBitSet(optionals, 5);
      if (struct.isSetReplicatorId()) {
        oprot.writeI32(struct.replicatorId);
      }
      if (struct.isSetTxnClock()) {
        oprot.writeString(struct.txnClock);
      }
      if (struct.isSetOpsList()) {
        {
          oprot.writeI32(struct.opsList.size());
          for (CRDTOperation _iter66 : struct.opsList)
          {
            _iter66.write(oprot);
          }
        }
      }
      if (struct.isSetRequestToCoordinator()) {
        struct.requestToCoordinator.write(oprot);
      }
      if (struct.isSetCompiledTxn()) {
        struct.compiledTxn.write(oprot);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, CRDTTransaction struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.id = iprot.readI32();
      struct.setIdIsSet(true);
      BitSet incoming = iprot.readBitSet(5);
      if (incoming.get(0)) {
        struct.replicatorId = iprot.readI32();
        struct.setReplicatorIdIsSet(true);
      }
      if (incoming.get(1)) {
        struct.txnClock = iprot.readString();
        struct.setTxnClockIsSet(true);
      }
      if (incoming.get(2)) {
        {
          org.apache.thrift.protocol.TList _list67 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.opsList = new ArrayList<CRDTOperation>(_list67.size);
          CRDTOperation _elem68;
          for (int _i69 = 0; _i69 < _list67.size; ++_i69)
          {
            _elem68 = new CRDTOperation();
            _elem68.read(iprot);
            struct.opsList.add(_elem68);
          }
        }
        struct.setOpsListIsSet(true);
      }
      if (incoming.get(3)) {
        struct.requestToCoordinator = new CoordinatorRequest();
        struct.requestToCoordinator.read(iprot);
        struct.setRequestToCoordinatorIsSet(true);
      }
      if (incoming.get(4)) {
        struct.compiledTxn = new CRDTCompiledTransaction();
        struct.compiledTxn.read(iprot);
        struct.setCompiledTxnIsSet(true);
      }
    }
  }

}

