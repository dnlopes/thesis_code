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
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2015-12-7")
public class UniqueValue implements org.apache.thrift.TBase<UniqueValue, UniqueValue._Fields>, java.io.Serializable, Cloneable, Comparable<UniqueValue> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("UniqueValue");

  private static final org.apache.thrift.protocol.TField CONSTRAINT_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("constraintId", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("value", org.apache.thrift.protocol.TType.STRING, (short)2);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new UniqueValueStandardSchemeFactory());
    schemes.put(TupleScheme.class, new UniqueValueTupleSchemeFactory());
  }

  public String constraintId; // required
  public String value; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    CONSTRAINT_ID((short)1, "constraintId"),
    VALUE((short)2, "value");

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
        case 2: // VALUE
          return VALUE;
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
    tmpMap.put(_Fields.CONSTRAINT_ID, new org.apache.thrift.meta_data.FieldMetaData("constraintId", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.VALUE, new org.apache.thrift.meta_data.FieldMetaData("value", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(UniqueValue.class, metaDataMap);
  }

  public UniqueValue() {
  }

  public UniqueValue(
    String constraintId,
    String value)
  {
    this();
    this.constraintId = constraintId;
    this.value = value;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public UniqueValue(UniqueValue other) {
    if (other.isSetConstraintId()) {
      this.constraintId = other.constraintId;
    }
    if (other.isSetValue()) {
      this.value = other.value;
    }
  }

  public UniqueValue deepCopy() {
    return new UniqueValue(this);
  }

  @Override
  public void clear() {
    this.constraintId = null;
    this.value = null;
  }

  public String getConstraintId() {
    return this.constraintId;
  }

  public UniqueValue setConstraintId(String constraintId) {
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

  public String getValue() {
    return this.value;
  }

  public UniqueValue setValue(String value) {
    this.value = value;
    return this;
  }

  public void unsetValue() {
    this.value = null;
  }

  /** Returns true if field value is set (has been assigned a value) and false otherwise */
  public boolean isSetValue() {
    return this.value != null;
  }

  public void setValueIsSet(boolean value) {
    if (!value) {
      this.value = null;
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

    case VALUE:
      if (value == null) {
        unsetValue();
      } else {
        setValue((String)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case CONSTRAINT_ID:
      return getConstraintId();

    case VALUE:
      return getValue();

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
    case VALUE:
      return isSetValue();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof UniqueValue)
      return this.equals((UniqueValue)that);
    return false;
  }

  public boolean equals(UniqueValue that) {
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

    boolean this_present_value = true && this.isSetValue();
    boolean that_present_value = true && that.isSetValue();
    if (this_present_value || that_present_value) {
      if (!(this_present_value && that_present_value))
        return false;
      if (!this.value.equals(that.value))
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

    boolean present_value = true && (isSetValue());
    list.add(present_value);
    if (present_value)
      list.add(value);

    return list.hashCode();
  }

  @Override
  public int compareTo(UniqueValue other) {
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
    lastComparison = Boolean.valueOf(isSetValue()).compareTo(other.isSetValue());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetValue()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.value, other.value);
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
    StringBuilder sb = new StringBuilder("UniqueValue(");
    boolean first = true;

    sb.append("constraintId:");
    if (this.constraintId == null) {
      sb.append("null");
    } else {
      sb.append(this.constraintId);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("value:");
    if (this.value == null) {
      sb.append("null");
    } else {
      sb.append(this.value);
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
    if (value == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'value' was not present! Struct: " + toString());
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

  private static class UniqueValueStandardSchemeFactory implements SchemeFactory {
    public UniqueValueStandardScheme getScheme() {
      return new UniqueValueStandardScheme();
    }
  }

  private static class UniqueValueStandardScheme extends StandardScheme<UniqueValue> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, UniqueValue struct) throws org.apache.thrift.TException {
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
          case 2: // VALUE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.value = iprot.readString();
              struct.setValueIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, UniqueValue struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.constraintId != null) {
        oprot.writeFieldBegin(CONSTRAINT_ID_FIELD_DESC);
        oprot.writeString(struct.constraintId);
        oprot.writeFieldEnd();
      }
      if (struct.value != null) {
        oprot.writeFieldBegin(VALUE_FIELD_DESC);
        oprot.writeString(struct.value);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class UniqueValueTupleSchemeFactory implements SchemeFactory {
    public UniqueValueTupleScheme getScheme() {
      return new UniqueValueTupleScheme();
    }
  }

  private static class UniqueValueTupleScheme extends TupleScheme<UniqueValue> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, UniqueValue struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeString(struct.constraintId);
      oprot.writeString(struct.value);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, UniqueValue struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.constraintId = iprot.readString();
      struct.setConstraintIdIsSet(true);
      struct.value = iprot.readString();
      struct.setValueIsSet(true);
    }
  }

}

