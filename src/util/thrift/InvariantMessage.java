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
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2015-3-23")
public class InvariantMessage implements org.apache.thrift.TBase<InvariantMessage, InvariantMessage._Fields>, java.io.Serializable, Cloneable, Comparable<InvariantMessage> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("InvariantMessage");

  private static final org.apache.thrift.protocol.TField MESSAGE_TYPE_FIELD_DESC = new org.apache.thrift.protocol.TField("messageType", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField INVARIANTS_FIELD_DESC = new org.apache.thrift.protocol.TField("invariants", org.apache.thrift.protocol.TType.LIST, (short)2);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new InvariantMessageStandardSchemeFactory());
    schemes.put(TupleScheme.class, new InvariantMessageTupleSchemeFactory());
  }

  /**
   * 
   * @see InvariantCheckType
   */
  public InvariantCheckType messageType; // required
  public List<Invariant> invariants; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    /**
     * 
     * @see InvariantCheckType
     */
    MESSAGE_TYPE((short)1, "messageType"),
    INVARIANTS((short)2, "invariants");

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
        case 1: // MESSAGE_TYPE
          return MESSAGE_TYPE;
        case 2: // INVARIANTS
          return INVARIANTS;
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
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.MESSAGE_TYPE, new org.apache.thrift.meta_data.FieldMetaData("messageType", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, InvariantCheckType.class)));
    tmpMap.put(_Fields.INVARIANTS, new org.apache.thrift.meta_data.FieldMetaData("invariants", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Invariant.class))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(InvariantMessage.class, metaDataMap);
  }

  public InvariantMessage() {
  }

  public InvariantMessage(
    InvariantCheckType messageType,
    List<Invariant> invariants)
  {
    this();
    this.messageType = messageType;
    this.invariants = invariants;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public InvariantMessage(InvariantMessage other) {
    if (other.isSetMessageType()) {
      this.messageType = other.messageType;
    }
    if (other.isSetInvariants()) {
      List<Invariant> __this__invariants = new ArrayList<Invariant>(other.invariants.size());
      for (Invariant other_element : other.invariants) {
        __this__invariants.add(new Invariant(other_element));
      }
      this.invariants = __this__invariants;
    }
  }

  public InvariantMessage deepCopy() {
    return new InvariantMessage(this);
  }

  @Override
  public void clear() {
    this.messageType = null;
    this.invariants = null;
  }

  /**
   * 
   * @see InvariantCheckType
   */
  public InvariantCheckType getMessageType() {
    return this.messageType;
  }

  /**
   * 
   * @see InvariantCheckType
   */
  public InvariantMessage setMessageType(InvariantCheckType messageType) {
    this.messageType = messageType;
    return this;
  }

  public void unsetMessageType() {
    this.messageType = null;
  }

  /** Returns true if field messageType is set (has been assigned a value) and false otherwise */
  public boolean isSetMessageType() {
    return this.messageType != null;
  }

  public void setMessageTypeIsSet(boolean value) {
    if (!value) {
      this.messageType = null;
    }
  }

  public int getInvariantsSize() {
    return (this.invariants == null) ? 0 : this.invariants.size();
  }

  public java.util.Iterator<Invariant> getInvariantsIterator() {
    return (this.invariants == null) ? null : this.invariants.iterator();
  }

  public void addToInvariants(Invariant elem) {
    if (this.invariants == null) {
      this.invariants = new ArrayList<Invariant>();
    }
    this.invariants.add(elem);
  }

  public List<Invariant> getInvariants() {
    return this.invariants;
  }

  public InvariantMessage setInvariants(List<Invariant> invariants) {
    this.invariants = invariants;
    return this;
  }

  public void unsetInvariants() {
    this.invariants = null;
  }

  /** Returns true if field invariants is set (has been assigned a value) and false otherwise */
  public boolean isSetInvariants() {
    return this.invariants != null;
  }

  public void setInvariantsIsSet(boolean value) {
    if (!value) {
      this.invariants = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case MESSAGE_TYPE:
      if (value == null) {
        unsetMessageType();
      } else {
        setMessageType((InvariantCheckType)value);
      }
      break;

    case INVARIANTS:
      if (value == null) {
        unsetInvariants();
      } else {
        setInvariants((List<Invariant>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case MESSAGE_TYPE:
      return getMessageType();

    case INVARIANTS:
      return getInvariants();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case MESSAGE_TYPE:
      return isSetMessageType();
    case INVARIANTS:
      return isSetInvariants();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof InvariantMessage)
      return this.equals((InvariantMessage)that);
    return false;
  }

  public boolean equals(InvariantMessage that) {
    if (that == null)
      return false;

    boolean this_present_messageType = true && this.isSetMessageType();
    boolean that_present_messageType = true && that.isSetMessageType();
    if (this_present_messageType || that_present_messageType) {
      if (!(this_present_messageType && that_present_messageType))
        return false;
      if (!this.messageType.equals(that.messageType))
        return false;
    }

    boolean this_present_invariants = true && this.isSetInvariants();
    boolean that_present_invariants = true && that.isSetInvariants();
    if (this_present_invariants || that_present_invariants) {
      if (!(this_present_invariants && that_present_invariants))
        return false;
      if (!this.invariants.equals(that.invariants))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_messageType = true && (isSetMessageType());
    list.add(present_messageType);
    if (present_messageType)
      list.add(messageType.getValue());

    boolean present_invariants = true && (isSetInvariants());
    list.add(present_invariants);
    if (present_invariants)
      list.add(invariants);

    return list.hashCode();
  }

  @Override
  public int compareTo(InvariantMessage other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetMessageType()).compareTo(other.isSetMessageType());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMessageType()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.messageType, other.messageType);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetInvariants()).compareTo(other.isSetInvariants());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetInvariants()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.invariants, other.invariants);
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
    StringBuilder sb = new StringBuilder("InvariantMessage(");
    boolean first = true;

    sb.append("messageType:");
    if (this.messageType == null) {
      sb.append("null");
    } else {
      sb.append(this.messageType);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("invariants:");
    if (this.invariants == null) {
      sb.append("null");
    } else {
      sb.append(this.invariants);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (messageType == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'messageType' was not present! Struct: " + toString());
    }
    if (invariants == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'invariants' was not present! Struct: " + toString());
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
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class InvariantMessageStandardSchemeFactory implements SchemeFactory {
    public InvariantMessageStandardScheme getScheme() {
      return new InvariantMessageStandardScheme();
    }
  }

  private static class InvariantMessageStandardScheme extends StandardScheme<InvariantMessage> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, InvariantMessage struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // MESSAGE_TYPE
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.messageType = util.thrift.InvariantCheckType.findByValue(iprot.readI32());
              struct.setMessageTypeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // INVARIANTS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list0 = iprot.readListBegin();
                struct.invariants = new ArrayList<Invariant>(_list0.size);
                Invariant _elem1;
                for (int _i2 = 0; _i2 < _list0.size; ++_i2)
                {
                  _elem1 = new Invariant();
                  _elem1.read(iprot);
                  struct.invariants.add(_elem1);
                }
                iprot.readListEnd();
              }
              struct.setInvariantsIsSet(true);
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
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, InvariantMessage struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.messageType != null) {
        oprot.writeFieldBegin(MESSAGE_TYPE_FIELD_DESC);
        oprot.writeI32(struct.messageType.getValue());
        oprot.writeFieldEnd();
      }
      if (struct.invariants != null) {
        oprot.writeFieldBegin(INVARIANTS_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.invariants.size()));
          for (Invariant _iter3 : struct.invariants)
          {
            _iter3.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class InvariantMessageTupleSchemeFactory implements SchemeFactory {
    public InvariantMessageTupleScheme getScheme() {
      return new InvariantMessageTupleScheme();
    }
  }

  private static class InvariantMessageTupleScheme extends TupleScheme<InvariantMessage> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, InvariantMessage struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeI32(struct.messageType.getValue());
      {
        oprot.writeI32(struct.invariants.size());
        for (Invariant _iter4 : struct.invariants)
        {
          _iter4.write(oprot);
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, InvariantMessage struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.messageType = util.thrift.InvariantCheckType.findByValue(iprot.readI32());
      struct.setMessageTypeIsSet(true);
      {
        org.apache.thrift.protocol.TList _list5 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
        struct.invariants = new ArrayList<Invariant>(_list5.size);
        Invariant _elem6;
        for (int _i7 = 0; _i7 < _list5.size; ++_i7)
        {
          _elem6 = new Invariant();
          _elem6.read(iprot);
          struct.invariants.add(_elem6);
        }
      }
      struct.setInvariantsIsSet(true);
    }
  }

}

