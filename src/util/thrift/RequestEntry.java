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
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2015-3-29")
public class RequestEntry implements org.apache.thrift.TBase<RequestEntry, RequestEntry._Fields>, java.io.Serializable, Cloneable, Comparable<RequestEntry> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("RequestEntry");

  private static final org.apache.thrift.protocol.TField TYPE_FIELD_DESC = new org.apache.thrift.protocol.TField("type", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField ID_FIELD_DESC = new org.apache.thrift.protocol.TField("id", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField FIELD_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("fieldName", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField TABLE_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("tableName", org.apache.thrift.protocol.TType.STRING, (short)4);
  private static final org.apache.thrift.protocol.TField VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("value", org.apache.thrift.protocol.TType.STRING, (short)5);
  private static final org.apache.thrift.protocol.TField CONSTRAINT_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("constraintId", org.apache.thrift.protocol.TType.STRING, (short)6);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new RequestEntryStandardSchemeFactory());
    schemes.put(TupleScheme.class, new RequestEntryTupleSchemeFactory());
  }

  /**
   * 
   * @see RequestType
   */
  public RequestType type; // required
  public String id; // required
  public String fieldName; // required
  public String tableName; // required
  public String value; // required
  public String constraintId; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    /**
     * 
     * @see RequestType
     */
    TYPE((short)1, "type"),
    ID((short)2, "id"),
    FIELD_NAME((short)3, "fieldName"),
    TABLE_NAME((short)4, "tableName"),
    VALUE((short)5, "value"),
    CONSTRAINT_ID((short)6, "constraintId");

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
        case 1: // TYPE
          return TYPE;
        case 2: // ID
          return ID;
        case 3: // FIELD_NAME
          return FIELD_NAME;
        case 4: // TABLE_NAME
          return TABLE_NAME;
        case 5: // VALUE
          return VALUE;
        case 6: // CONSTRAINT_ID
          return CONSTRAINT_ID;
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
    tmpMap.put(_Fields.TYPE, new org.apache.thrift.meta_data.FieldMetaData("type", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, RequestType.class)));
    tmpMap.put(_Fields.ID, new org.apache.thrift.meta_data.FieldMetaData("id", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.FIELD_NAME, new org.apache.thrift.meta_data.FieldMetaData("fieldName", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.TABLE_NAME, new org.apache.thrift.meta_data.FieldMetaData("tableName", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.VALUE, new org.apache.thrift.meta_data.FieldMetaData("value", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.CONSTRAINT_ID, new org.apache.thrift.meta_data.FieldMetaData("constraintId", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(RequestEntry.class, metaDataMap);
  }

  public RequestEntry() {
  }

  public RequestEntry(
    RequestType type,
    String id,
    String fieldName,
    String tableName,
    String value,
    String constraintId)
  {
    this();
    this.type = type;
    this.id = id;
    this.fieldName = fieldName;
    this.tableName = tableName;
    this.value = value;
    this.constraintId = constraintId;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public RequestEntry(RequestEntry other) {
    if (other.isSetType()) {
      this.type = other.type;
    }
    if (other.isSetId()) {
      this.id = other.id;
    }
    if (other.isSetFieldName()) {
      this.fieldName = other.fieldName;
    }
    if (other.isSetTableName()) {
      this.tableName = other.tableName;
    }
    if (other.isSetValue()) {
      this.value = other.value;
    }
    if (other.isSetConstraintId()) {
      this.constraintId = other.constraintId;
    }
  }

  public RequestEntry deepCopy() {
    return new RequestEntry(this);
  }

  @Override
  public void clear() {
    this.type = null;
    this.id = null;
    this.fieldName = null;
    this.tableName = null;
    this.value = null;
    this.constraintId = null;
  }

  /**
   * 
   * @see RequestType
   */
  public RequestType getType() {
    return this.type;
  }

  /**
   * 
   * @see RequestType
   */
  public RequestEntry setType(RequestType type) {
    this.type = type;
    return this;
  }

  public void unsetType() {
    this.type = null;
  }

  /** Returns true if field type is set (has been assigned a value) and false otherwise */
  public boolean isSetType() {
    return this.type != null;
  }

  public void setTypeIsSet(boolean value) {
    if (!value) {
      this.type = null;
    }
  }

  public String getId() {
    return this.id;
  }

  public RequestEntry setId(String id) {
    this.id = id;
    return this;
  }

  public void unsetId() {
    this.id = null;
  }

  /** Returns true if field id is set (has been assigned a value) and false otherwise */
  public boolean isSetId() {
    return this.id != null;
  }

  public void setIdIsSet(boolean value) {
    if (!value) {
      this.id = null;
    }
  }

  public String getFieldName() {
    return this.fieldName;
  }

  public RequestEntry setFieldName(String fieldName) {
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

  public String getTableName() {
    return this.tableName;
  }

  public RequestEntry setTableName(String tableName) {
    this.tableName = tableName;
    return this;
  }

  public void unsetTableName() {
    this.tableName = null;
  }

  /** Returns true if field tableName is set (has been assigned a value) and false otherwise */
  public boolean isSetTableName() {
    return this.tableName != null;
  }

  public void setTableNameIsSet(boolean value) {
    if (!value) {
      this.tableName = null;
    }
  }

  public String getValue() {
    return this.value;
  }

  public RequestEntry setValue(String value) {
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

  public String getConstraintId() {
    return this.constraintId;
  }

  public RequestEntry setConstraintId(String constraintId) {
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

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case TYPE:
      if (value == null) {
        unsetType();
      } else {
        setType((RequestType)value);
      }
      break;

    case ID:
      if (value == null) {
        unsetId();
      } else {
        setId((String)value);
      }
      break;

    case FIELD_NAME:
      if (value == null) {
        unsetFieldName();
      } else {
        setFieldName((String)value);
      }
      break;

    case TABLE_NAME:
      if (value == null) {
        unsetTableName();
      } else {
        setTableName((String)value);
      }
      break;

    case VALUE:
      if (value == null) {
        unsetValue();
      } else {
        setValue((String)value);
      }
      break;

    case CONSTRAINT_ID:
      if (value == null) {
        unsetConstraintId();
      } else {
        setConstraintId((String)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case TYPE:
      return getType();

    case ID:
      return getId();

    case FIELD_NAME:
      return getFieldName();

    case TABLE_NAME:
      return getTableName();

    case VALUE:
      return getValue();

    case CONSTRAINT_ID:
      return getConstraintId();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case TYPE:
      return isSetType();
    case ID:
      return isSetId();
    case FIELD_NAME:
      return isSetFieldName();
    case TABLE_NAME:
      return isSetTableName();
    case VALUE:
      return isSetValue();
    case CONSTRAINT_ID:
      return isSetConstraintId();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof RequestEntry)
      return this.equals((RequestEntry)that);
    return false;
  }

  public boolean equals(RequestEntry that) {
    if (that == null)
      return false;

    boolean this_present_type = true && this.isSetType();
    boolean that_present_type = true && that.isSetType();
    if (this_present_type || that_present_type) {
      if (!(this_present_type && that_present_type))
        return false;
      if (!this.type.equals(that.type))
        return false;
    }

    boolean this_present_id = true && this.isSetId();
    boolean that_present_id = true && that.isSetId();
    if (this_present_id || that_present_id) {
      if (!(this_present_id && that_present_id))
        return false;
      if (!this.id.equals(that.id))
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

    boolean this_present_tableName = true && this.isSetTableName();
    boolean that_present_tableName = true && that.isSetTableName();
    if (this_present_tableName || that_present_tableName) {
      if (!(this_present_tableName && that_present_tableName))
        return false;
      if (!this.tableName.equals(that.tableName))
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

    boolean this_present_constraintId = true && this.isSetConstraintId();
    boolean that_present_constraintId = true && that.isSetConstraintId();
    if (this_present_constraintId || that_present_constraintId) {
      if (!(this_present_constraintId && that_present_constraintId))
        return false;
      if (!this.constraintId.equals(that.constraintId))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_type = true && (isSetType());
    list.add(present_type);
    if (present_type)
      list.add(type.getValue());

    boolean present_id = true && (isSetId());
    list.add(present_id);
    if (present_id)
      list.add(id);

    boolean present_fieldName = true && (isSetFieldName());
    list.add(present_fieldName);
    if (present_fieldName)
      list.add(fieldName);

    boolean present_tableName = true && (isSetTableName());
    list.add(present_tableName);
    if (present_tableName)
      list.add(tableName);

    boolean present_value = true && (isSetValue());
    list.add(present_value);
    if (present_value)
      list.add(value);

    boolean present_constraintId = true && (isSetConstraintId());
    list.add(present_constraintId);
    if (present_constraintId)
      list.add(constraintId);

    return list.hashCode();
  }

  @Override
  public int compareTo(RequestEntry other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetType()).compareTo(other.isSetType());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetType()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.type, other.type);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
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
    lastComparison = Boolean.valueOf(isSetTableName()).compareTo(other.isSetTableName());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTableName()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tableName, other.tableName);
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
    StringBuilder sb = new StringBuilder("RequestEntry(");
    boolean first = true;

    sb.append("type:");
    if (this.type == null) {
      sb.append("null");
    } else {
      sb.append(this.type);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("id:");
    if (this.id == null) {
      sb.append("null");
    } else {
      sb.append(this.id);
    }
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
    sb.append("tableName:");
    if (this.tableName == null) {
      sb.append("null");
    } else {
      sb.append(this.tableName);
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
    if (!first) sb.append(", ");
    sb.append("constraintId:");
    if (this.constraintId == null) {
      sb.append("null");
    } else {
      sb.append(this.constraintId);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (type == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'type' was not present! Struct: " + toString());
    }
    if (id == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'id' was not present! Struct: " + toString());
    }
    if (fieldName == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'fieldName' was not present! Struct: " + toString());
    }
    if (tableName == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'tableName' was not present! Struct: " + toString());
    }
    if (constraintId == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'constraintId' was not present! Struct: " + toString());
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

  private static class RequestEntryStandardSchemeFactory implements SchemeFactory {
    public RequestEntryStandardScheme getScheme() {
      return new RequestEntryStandardScheme();
    }
  }

  private static class RequestEntryStandardScheme extends StandardScheme<RequestEntry> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, RequestEntry struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // TYPE
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.type = util.thrift.RequestType.findByValue(iprot.readI32());
              struct.setTypeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.id = iprot.readString();
              struct.setIdIsSet(true);
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
          case 4: // TABLE_NAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.tableName = iprot.readString();
              struct.setTableNameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // VALUE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.value = iprot.readString();
              struct.setValueIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // CONSTRAINT_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.constraintId = iprot.readString();
              struct.setConstraintIdIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, RequestEntry struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.type != null) {
        oprot.writeFieldBegin(TYPE_FIELD_DESC);
        oprot.writeI32(struct.type.getValue());
        oprot.writeFieldEnd();
      }
      if (struct.id != null) {
        oprot.writeFieldBegin(ID_FIELD_DESC);
        oprot.writeString(struct.id);
        oprot.writeFieldEnd();
      }
      if (struct.fieldName != null) {
        oprot.writeFieldBegin(FIELD_NAME_FIELD_DESC);
        oprot.writeString(struct.fieldName);
        oprot.writeFieldEnd();
      }
      if (struct.tableName != null) {
        oprot.writeFieldBegin(TABLE_NAME_FIELD_DESC);
        oprot.writeString(struct.tableName);
        oprot.writeFieldEnd();
      }
      if (struct.value != null) {
        oprot.writeFieldBegin(VALUE_FIELD_DESC);
        oprot.writeString(struct.value);
        oprot.writeFieldEnd();
      }
      if (struct.constraintId != null) {
        oprot.writeFieldBegin(CONSTRAINT_ID_FIELD_DESC);
        oprot.writeString(struct.constraintId);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class RequestEntryTupleSchemeFactory implements SchemeFactory {
    public RequestEntryTupleScheme getScheme() {
      return new RequestEntryTupleScheme();
    }
  }

  private static class RequestEntryTupleScheme extends TupleScheme<RequestEntry> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, RequestEntry struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeI32(struct.type.getValue());
      oprot.writeString(struct.id);
      oprot.writeString(struct.fieldName);
      oprot.writeString(struct.tableName);
      oprot.writeString(struct.constraintId);
      BitSet optionals = new BitSet();
      if (struct.isSetValue()) {
        optionals.set(0);
      }
      oprot.writeBitSet(optionals, 1);
      if (struct.isSetValue()) {
        oprot.writeString(struct.value);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, RequestEntry struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.type = util.thrift.RequestType.findByValue(iprot.readI32());
      struct.setTypeIsSet(true);
      struct.id = iprot.readString();
      struct.setIdIsSet(true);
      struct.fieldName = iprot.readString();
      struct.setFieldNameIsSet(true);
      struct.tableName = iprot.readString();
      struct.setTableNameIsSet(true);
      struct.constraintId = iprot.readString();
      struct.setConstraintIdIsSet(true);
      BitSet incoming = iprot.readBitSet(1);
      if (incoming.get(0)) {
        struct.value = iprot.readString();
        struct.setValueIsSet(true);
      }
    }
  }

}
