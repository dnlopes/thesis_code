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
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2015-10-21")
public class RequestValue implements org.apache.thrift.TBase<RequestValue, RequestValue._Fields>, java.io.Serializable, Cloneable, Comparable<RequestValue> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("RequestValue");

  private static final org.apache.thrift.protocol.TField CONSTRAINT_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("constraintId", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField OP_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("opId", org.apache.thrift.protocol.TType.I32, (short)2);
  private static final org.apache.thrift.protocol.TField FIELD_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("fieldName", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField REQUESTED_VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("requestedValue", org.apache.thrift.protocol.TType.STRING, (short)4);
  private static final org.apache.thrift.protocol.TField TEMP_SYMBOL_FIELD_DESC = new org.apache.thrift.protocol.TField("tempSymbol", org.apache.thrift.protocol.TType.STRING, (short)5);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new RequestValueStandardSchemeFactory());
    schemes.put(TupleScheme.class, new RequestValueTupleSchemeFactory());
  }

  public String constraintId; // required
  public int opId; // required
  public String fieldName; // required
  public String requestedValue; // required
  public String tempSymbol; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    CONSTRAINT_ID((short)1, "constraintId"),
    OP_ID((short)2, "opId"),
    FIELD_NAME((short)3, "fieldName"),
    REQUESTED_VALUE((short)4, "requestedValue"),
    TEMP_SYMBOL((short)5, "tempSymbol");

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
        case 1: // CONSTRAINT_ID
          return CONSTRAINT_ID;
        case 2: // OP_ID
          return OP_ID;
        case 3: // FIELD_NAME
          return FIELD_NAME;
        case 4: // REQUESTED_VALUE
          return REQUESTED_VALUE;
        case 5: // TEMP_SYMBOL
          return TEMP_SYMBOL;
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
  private static final int __OPID_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.CONSTRAINT_ID, new org.apache.thrift.meta_data.FieldMetaData("constraintId", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.OP_ID, new org.apache.thrift.meta_data.FieldMetaData("opId", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.FIELD_NAME, new org.apache.thrift.meta_data.FieldMetaData("fieldName", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.REQUESTED_VALUE, new org.apache.thrift.meta_data.FieldMetaData("requestedValue", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.TEMP_SYMBOL, new org.apache.thrift.meta_data.FieldMetaData("tempSymbol", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(RequestValue.class, metaDataMap);
  }

  public RequestValue() {
  }

  public RequestValue(
    String constraintId,
    int opId,
    String fieldName,
    String requestedValue,
    String tempSymbol)
  {
    this();
    this.constraintId = constraintId;
    this.opId = opId;
    setOpIdIsSet(true);
    this.fieldName = fieldName;
    this.requestedValue = requestedValue;
    this.tempSymbol = tempSymbol;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public RequestValue(RequestValue other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetConstraintId()) {
      this.constraintId = other.constraintId;
    }
    this.opId = other.opId;
    if (other.isSetFieldName()) {
      this.fieldName = other.fieldName;
    }
    if (other.isSetRequestedValue()) {
      this.requestedValue = other.requestedValue;
    }
    if (other.isSetTempSymbol()) {
      this.tempSymbol = other.tempSymbol;
    }
  }

  public RequestValue deepCopy() {
    return new RequestValue(this);
  }

  @Override
  public void clear() {
    this.constraintId = null;
    setOpIdIsSet(false);
    this.opId = 0;
    this.fieldName = null;
    this.requestedValue = null;
    this.tempSymbol = null;
  }

  public String getConstraintId() {
    return this.constraintId;
  }

  public RequestValue setConstraintId(String constraintId) {
    this.constraintId = constraintId;
    return this;
  }

  public void unsetConstraintId() {
    this.constraintId = null;
  }

  /** Returns true if field constraintId is set (has been assigned a value) and false otherwise */
  public boolean isSetConstraintId() {
    return this.constraintId != null;
  }

  public void setConstraintIdIsSet(boolean value) {
    if (!value) {
      this.constraintId = null;
    }
  }

  public int getOpId() {
    return this.opId;
  }

  public RequestValue setOpId(int opId) {
    this.opId = opId;
    setOpIdIsSet(true);
    return this;
  }

  public void unsetOpId() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __OPID_ISSET_ID);
  }

  /** Returns true if field opId is set (has been assigned a value) and false otherwise */
  public boolean isSetOpId() {
    return EncodingUtils.testBit(__isset_bitfield, __OPID_ISSET_ID);
  }

  public void setOpIdIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __OPID_ISSET_ID, value);
  }

  public String getFieldName() {
    return this.fieldName;
  }

  public RequestValue setFieldName(String fieldName) {
    this.fieldName = fieldName;
    return this;
  }

  public void unsetFieldName() {
    this.fieldName = null;
  }

  /** Returns true if field fieldName is set (has been assigned a value) and false otherwise */
  public boolean isSetFieldName() {
    return this.fieldName != null;
  }

  public void setFieldNameIsSet(boolean value) {
    if (!value) {
      this.fieldName = null;
    }
  }

  public String getRequestedValue() {
    return this.requestedValue;
  }

  public RequestValue setRequestedValue(String requestedValue) {
    this.requestedValue = requestedValue;
    return this;
  }

  public void unsetRequestedValue() {
    this.requestedValue = null;
  }

  /** Returns true if field requestedValue is set (has been assigned a value) and false otherwise */
  public boolean isSetRequestedValue() {
    return this.requestedValue != null;
  }

  public void setRequestedValueIsSet(boolean value) {
    if (!value) {
      this.requestedValue = null;
    }
  }

  public String getTempSymbol() {
    return this.tempSymbol;
  }

  public RequestValue setTempSymbol(String tempSymbol) {
    this.tempSymbol = tempSymbol;
    return this;
  }

  public void unsetTempSymbol() {
    this.tempSymbol = null;
  }

  /** Returns true if field tempSymbol is set (has been assigned a value) and false otherwise */
  public boolean isSetTempSymbol() {
    return this.tempSymbol != null;
  }

  public void setTempSymbolIsSet(boolean value) {
    if (!value) {
      this.tempSymbol = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case CONSTRAINT_ID:
      if (value == null) {
        unsetConstraintId();
      } else {
        setConstraintId((String)value);
      }
      break;

    case OP_ID:
      if (value == null) {
        unsetOpId();
      } else {
        setOpId((Integer)value);
      }
      break;

    case FIELD_NAME:
      if (value == null) {
        unsetFieldName();
      } else {
        setFieldName((String)value);
      }
      break;

    case REQUESTED_VALUE:
      if (value == null) {
        unsetRequestedValue();
      } else {
        setRequestedValue((String)value);
      }
      break;

    case TEMP_SYMBOL:
      if (value == null) {
        unsetTempSymbol();
      } else {
        setTempSymbol((String)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case CONSTRAINT_ID:
      return getConstraintId();

    case OP_ID:
      return Integer.valueOf(getOpId());

    case FIELD_NAME:
      return getFieldName();

    case REQUESTED_VALUE:
      return getRequestedValue();

    case TEMP_SYMBOL:
      return getTempSymbol();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case CONSTRAINT_ID:
      return isSetConstraintId();
    case OP_ID:
      return isSetOpId();
    case FIELD_NAME:
      return isSetFieldName();
    case REQUESTED_VALUE:
      return isSetRequestedValue();
    case TEMP_SYMBOL:
      return isSetTempSymbol();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof RequestValue)
      return this.equals((RequestValue)that);
    return false;
  }

  public boolean equals(RequestValue that) {
    if (that == null)
      return false;

    boolean this_present_constraintId = true && this.isSetConstraintId();
    boolean that_present_constraintId = true && that.isSetConstraintId();
    if (this_present_constraintId || that_present_constraintId) {
      if (!(this_present_constraintId && that_present_constraintId))
        return false;
      if (!this.constraintId.equals(that.constraintId))
        return false;
    }

    boolean this_present_opId = true;
    boolean that_present_opId = true;
    if (this_present_opId || that_present_opId) {
      if (!(this_present_opId && that_present_opId))
        return false;
      if (this.opId != that.opId)
        return false;
    }

    boolean this_present_fieldName = true && this.isSetFieldName();
    boolean that_present_fieldName = true && that.isSetFieldName();
    if (this_present_fieldName || that_present_fieldName) {
      if (!(this_present_fieldName && that_present_fieldName))
        return false;
      if (!this.fieldName.equals(that.fieldName))
        return false;
    }

    boolean this_present_requestedValue = true && this.isSetRequestedValue();
    boolean that_present_requestedValue = true && that.isSetRequestedValue();
    if (this_present_requestedValue || that_present_requestedValue) {
      if (!(this_present_requestedValue && that_present_requestedValue))
        return false;
      if (!this.requestedValue.equals(that.requestedValue))
        return false;
    }

    boolean this_present_tempSymbol = true && this.isSetTempSymbol();
    boolean that_present_tempSymbol = true && that.isSetTempSymbol();
    if (this_present_tempSymbol || that_present_tempSymbol) {
      if (!(this_present_tempSymbol && that_present_tempSymbol))
        return false;
      if (!this.tempSymbol.equals(that.tempSymbol))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_constraintId = true && (isSetConstraintId());
    list.add(present_constraintId);
    if (present_constraintId)
      list.add(constraintId);

    boolean present_opId = true;
    list.add(present_opId);
    if (present_opId)
      list.add(opId);

    boolean present_fieldName = true && (isSetFieldName());
    list.add(present_fieldName);
    if (present_fieldName)
      list.add(fieldName);

    boolean present_requestedValue = true && (isSetRequestedValue());
    list.add(present_requestedValue);
    if (present_requestedValue)
      list.add(requestedValue);

    boolean present_tempSymbol = true && (isSetTempSymbol());
    list.add(present_tempSymbol);
    if (present_tempSymbol)
      list.add(tempSymbol);

    return list.hashCode();
  }

  @Override
  public int compareTo(RequestValue other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetConstraintId()).compareTo(other.isSetConstraintId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetConstraintId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.constraintId, other.constraintId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetOpId()).compareTo(other.isSetOpId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetOpId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.opId, other.opId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetFieldName()).compareTo(other.isSetFieldName());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetFieldName()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.fieldName, other.fieldName);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetRequestedValue()).compareTo(other.isSetRequestedValue());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetRequestedValue()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.requestedValue, other.requestedValue);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetTempSymbol()).compareTo(other.isSetTempSymbol());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTempSymbol()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tempSymbol, other.tempSymbol);
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
    StringBuilder sb = new StringBuilder("RequestValue(");
    boolean first = true;

    sb.append("constraintId:");
    if (this.constraintId == null) {
      sb.append("null");
    } else {
      sb.append(this.constraintId);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("opId:");
    sb.append(this.opId);
    first = false;
    if (!first) sb.append(", ");
    sb.append("fieldName:");
    if (this.fieldName == null) {
      sb.append("null");
    } else {
      sb.append(this.fieldName);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("requestedValue:");
    if (this.requestedValue == null) {
      sb.append("null");
    } else {
      sb.append(this.requestedValue);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("tempSymbol:");
    if (this.tempSymbol == null) {
      sb.append("null");
    } else {
      sb.append(this.tempSymbol);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (constraintId == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'constraintId' was not present! Struct: " + toString());
    }
    // alas, we cannot check 'opId' because it's a primitive and you chose the non-beans generator.
    if (fieldName == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'fieldName' was not present! Struct: " + toString());
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

  private static class RequestValueStandardSchemeFactory implements SchemeFactory {
    public RequestValueStandardScheme getScheme() {
      return new RequestValueStandardScheme();
    }
  }

  private static class RequestValueStandardScheme extends StandardScheme<RequestValue> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, RequestValue struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // CONSTRAINT_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.constraintId = iprot.readString();
              struct.setConstraintIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // OP_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.opId = iprot.readI32();
              struct.setOpIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // FIELD_NAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.fieldName = iprot.readString();
              struct.setFieldNameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // REQUESTED_VALUE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.requestedValue = iprot.readString();
              struct.setRequestedValueIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // TEMP_SYMBOL
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.tempSymbol = iprot.readString();
              struct.setTempSymbolIsSet(true);
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
      if (!struct.isSetOpId()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'opId' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, RequestValue struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.constraintId != null) {
        oprot.writeFieldBegin(CONSTRAINT_ID_FIELD_DESC);
        oprot.writeString(struct.constraintId);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(OP_ID_FIELD_DESC);
      oprot.writeI32(struct.opId);
      oprot.writeFieldEnd();
      if (struct.fieldName != null) {
        oprot.writeFieldBegin(FIELD_NAME_FIELD_DESC);
        oprot.writeString(struct.fieldName);
        oprot.writeFieldEnd();
      }
      if (struct.requestedValue != null) {
        oprot.writeFieldBegin(REQUESTED_VALUE_FIELD_DESC);
        oprot.writeString(struct.requestedValue);
        oprot.writeFieldEnd();
      }
      if (struct.tempSymbol != null) {
        oprot.writeFieldBegin(TEMP_SYMBOL_FIELD_DESC);
        oprot.writeString(struct.tempSymbol);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class RequestValueTupleSchemeFactory implements SchemeFactory {
    public RequestValueTupleScheme getScheme() {
      return new RequestValueTupleScheme();
    }
  }

  private static class RequestValueTupleScheme extends TupleScheme<RequestValue> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, RequestValue struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeString(struct.constraintId);
      oprot.writeI32(struct.opId);
      oprot.writeString(struct.fieldName);
      BitSet optionals = new BitSet();
      if (struct.isSetRequestedValue()) {
        optionals.set(0);
      }
      if (struct.isSetTempSymbol()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetRequestedValue()) {
        oprot.writeString(struct.requestedValue);
      }
      if (struct.isSetTempSymbol()) {
        oprot.writeString(struct.tempSymbol);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, RequestValue struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.constraintId = iprot.readString();
      struct.setConstraintIdIsSet(true);
      struct.opId = iprot.readI32();
      struct.setOpIdIsSet(true);
      struct.fieldName = iprot.readString();
      struct.setFieldNameIsSet(true);
      BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        struct.requestedValue = iprot.readString();
        struct.setRequestedValueIsSet(true);
      }
      if (incoming.get(1)) {
        struct.tempSymbol = iprot.readString();
        struct.setTempSymbolIsSet(true);
      }
    }
  }

}

