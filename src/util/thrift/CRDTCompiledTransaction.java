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
public class CRDTCompiledTransaction implements org.apache.thrift.TBase<CRDTCompiledTransaction, CRDTCompiledTransaction._Fields>, java.io.Serializable, Cloneable, Comparable<CRDTCompiledTransaction> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("CRDTCompiledTransaction");

  private static final org.apache.thrift.protocol.TField ID_FIELD_DESC = new org.apache.thrift.protocol.TField("id", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField REPLICATOR_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("replicatorId", org.apache.thrift.protocol.TType.I32, (short)2);
  private static final org.apache.thrift.protocol.TField TXN_CLOCK_FIELD_DESC = new org.apache.thrift.protocol.TField("txnClock", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField OPS_LIST_FIELD_DESC = new org.apache.thrift.protocol.TField("opsList", org.apache.thrift.protocol.TType.LIST, (short)4);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new CRDTCompiledTransactionStandardSchemeFactory());
    schemes.put(TupleScheme.class, new CRDTCompiledTransactionTupleSchemeFactory());
  }

  public int id; // required
  public int replicatorId; // required
  public String txnClock; // required
  public List<String> opsList; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    ID((short)1, "id"),
    REPLICATOR_ID((short)2, "replicatorId"),
    TXN_CLOCK((short)3, "txnClock"),
    OPS_LIST((short)4, "opsList");

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
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.ID, new org.apache.thrift.meta_data.FieldMetaData("id", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.REPLICATOR_ID, new org.apache.thrift.meta_data.FieldMetaData("replicatorId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.TXN_CLOCK, new org.apache.thrift.meta_data.FieldMetaData("txnClock", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.OPS_LIST, new org.apache.thrift.meta_data.FieldMetaData("opsList", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(CRDTCompiledTransaction.class, metaDataMap);
  }

  public CRDTCompiledTransaction() {
  }

  public CRDTCompiledTransaction(
    int id,
    int replicatorId,
    String txnClock,
    List<String> opsList)
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
  public CRDTCompiledTransaction(CRDTCompiledTransaction other) {
    __isset_bitfield = other.__isset_bitfield;
    this.id = other.id;
    this.replicatorId = other.replicatorId;
    if (other.isSetTxnClock()) {
      this.txnClock = other.txnClock;
    }
    if (other.isSetOpsList()) {
      List<String> __this__opsList = new ArrayList<String>(other.opsList);
      this.opsList = __this__opsList;
    }
  }

  public CRDTCompiledTransaction deepCopy() {
    return new CRDTCompiledTransaction(this);
  }

  @Override
  public void clear() {
    setIdIsSet(false);
    this.id = 0;
    setReplicatorIdIsSet(false);
    this.replicatorId = 0;
    this.txnClock = null;
    this.opsList = null;
  }

  public int getId() {
    return this.id;
  }

  public CRDTCompiledTransaction setId(int id) {
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

  public CRDTCompiledTransaction setReplicatorId(int replicatorId) {
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

  public CRDTCompiledTransaction setTxnClock(String txnClock) {
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

  public java.util.Iterator<String> getOpsListIterator() {
    return (this.opsList == null) ? null : this.opsList.iterator();
  }

  public void addToOpsList(String elem) {
    if (this.opsList == null) {
      this.opsList = new ArrayList<String>();
    }
    this.opsList.add(elem);
  }

  public List<String> getOpsList() {
    return this.opsList;
  }

  public CRDTCompiledTransaction setOpsList(List<String> opsList) {
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
        setOpsList((List<String>)value);
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
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof CRDTCompiledTransaction)
      return this.equals((CRDTCompiledTransaction)that);
    return false;
  }

  public boolean equals(CRDTCompiledTransaction that) {
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

    return list.hashCode();
  }

  @Override
  public int compareTo(CRDTCompiledTransaction other) {
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
    StringBuilder sb = new StringBuilder("CRDTCompiledTransaction(");
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
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // alas, we cannot check 'id' because it's a primitive and you chose the non-beans generator.
    if (txnClock == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'txnClock' was not present! Struct: " + toString());
    }
    if (opsList == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'opsList' was not present! Struct: " + toString());
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

  private static class CRDTCompiledTransactionStandardSchemeFactory implements SchemeFactory {
    public CRDTCompiledTransactionStandardScheme getScheme() {
      return new CRDTCompiledTransactionStandardScheme();
    }
  }

  private static class CRDTCompiledTransactionStandardScheme extends StandardScheme<CRDTCompiledTransaction> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, CRDTCompiledTransaction struct) throws org.apache.thrift.TException {
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
                org.apache.thrift.protocol.TList _list90 = iprot.readListBegin();
                struct.opsList = new ArrayList<String>(_list90.size);
                String _elem91;
                for (int _i92 = 0; _i92 < _list90.size; ++_i92)
                {
                  _elem91 = iprot.readString();
                  struct.opsList.add(_elem91);
                }
                iprot.readListEnd();
              }
              struct.setOpsListIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, CRDTCompiledTransaction struct) throws org.apache.thrift.TException {
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
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.opsList.size()));
          for (String _iter93 : struct.opsList)
          {
            oprot.writeString(_iter93);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class CRDTCompiledTransactionTupleSchemeFactory implements SchemeFactory {
    public CRDTCompiledTransactionTupleScheme getScheme() {
      return new CRDTCompiledTransactionTupleScheme();
    }
  }

  private static class CRDTCompiledTransactionTupleScheme extends TupleScheme<CRDTCompiledTransaction> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, CRDTCompiledTransaction struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeI32(struct.id);
      oprot.writeString(struct.txnClock);
      {
        oprot.writeI32(struct.opsList.size());
        for (String _iter94 : struct.opsList)
        {
          oprot.writeString(_iter94);
        }
      }
      BitSet optionals = new BitSet();
      if (struct.isSetReplicatorId()) {
        optionals.set(0);
      }
      oprot.writeBitSet(optionals, 1);
      if (struct.isSetReplicatorId()) {
        oprot.writeI32(struct.replicatorId);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, CRDTCompiledTransaction struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.id = iprot.readI32();
      struct.setIdIsSet(true);
      struct.txnClock = iprot.readString();
      struct.setTxnClockIsSet(true);
      {
        org.apache.thrift.protocol.TList _list95 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
        struct.opsList = new ArrayList<String>(_list95.size);
        String _elem96;
        for (int _i97 = 0; _i97 < _list95.size; ++_i97)
        {
          _elem96 = iprot.readString();
          struct.opsList.add(_elem96);
        }
      }
      struct.setOpsListIsSet(true);
      BitSet incoming = iprot.readBitSet(1);
      if (incoming.get(0)) {
        struct.replicatorId = iprot.readI32();
        struct.setReplicatorIdIsSet(true);
      }
    }
  }

}

