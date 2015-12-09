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
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2015-12-9")
public class CoordinatorRequest implements org.apache.thrift.TBase<CoordinatorRequest, CoordinatorRequest._Fields>, java.io.Serializable, Cloneable, Comparable<CoordinatorRequest> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("CoordinatorRequest");

  private static final org.apache.thrift.protocol.TField REQUESTS_FIELD_DESC = new org.apache.thrift.protocol.TField("requests", org.apache.thrift.protocol.TType.LIST, (short)1);
  private static final org.apache.thrift.protocol.TField UNIQUE_VALUES_FIELD_DESC = new org.apache.thrift.protocol.TField("uniqueValues", org.apache.thrift.protocol.TType.LIST, (short)2);
  private static final org.apache.thrift.protocol.TField DELTA_VALUES_FIELD_DESC = new org.apache.thrift.protocol.TField("deltaValues", org.apache.thrift.protocol.TType.LIST, (short)3);
  private static final org.apache.thrift.protocol.TField TEMP_NODE_PATH_FIELD_DESC = new org.apache.thrift.protocol.TField("tempNodePath", org.apache.thrift.protocol.TType.STRING, (short)4);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new CoordinatorRequestStandardSchemeFactory());
    schemes.put(TupleScheme.class, new CoordinatorRequestTupleSchemeFactory());
  }

  public List<RequestValue> requests; // required
  public List<UniqueValue> uniqueValues; // required
  public List<ApplyDelta> deltaValues; // required
  public String tempNodePath; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    REQUESTS((short)1, "requests"),
    UNIQUE_VALUES((short)2, "uniqueValues"),
    DELTA_VALUES((short)3, "deltaValues"),
    TEMP_NODE_PATH((short)4, "tempNodePath");

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
        case 1: // REQUESTS
          return REQUESTS;
        case 2: // UNIQUE_VALUES
          return UNIQUE_VALUES;
        case 3: // DELTA_VALUES
          return DELTA_VALUES;
        case 4: // TEMP_NODE_PATH
          return TEMP_NODE_PATH;
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
    tmpMap.put(_Fields.REQUESTS, new org.apache.thrift.meta_data.FieldMetaData("requests", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, RequestValue.class))));
    tmpMap.put(_Fields.UNIQUE_VALUES, new org.apache.thrift.meta_data.FieldMetaData("uniqueValues", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, UniqueValue.class))));
    tmpMap.put(_Fields.DELTA_VALUES, new org.apache.thrift.meta_data.FieldMetaData("deltaValues", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, ApplyDelta.class))));
    tmpMap.put(_Fields.TEMP_NODE_PATH, new org.apache.thrift.meta_data.FieldMetaData("tempNodePath", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(CoordinatorRequest.class, metaDataMap);
  }

  public CoordinatorRequest() {
  }

  public CoordinatorRequest(
    List<RequestValue> requests,
    List<UniqueValue> uniqueValues,
    List<ApplyDelta> deltaValues,
    String tempNodePath)
  {
    this();
    this.requests = requests;
    this.uniqueValues = uniqueValues;
    this.deltaValues = deltaValues;
    this.tempNodePath = tempNodePath;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public CoordinatorRequest(CoordinatorRequest other) {
    if (other.isSetRequests()) {
      List<RequestValue> __this__requests = new ArrayList<RequestValue>(other.requests.size());
      for (RequestValue other_element : other.requests) {
        __this__requests.add(new RequestValue(other_element));
      }
      this.requests = __this__requests;
    }
    if (other.isSetUniqueValues()) {
      List<UniqueValue> __this__uniqueValues = new ArrayList<UniqueValue>(other.uniqueValues.size());
      for (UniqueValue other_element : other.uniqueValues) {
        __this__uniqueValues.add(new UniqueValue(other_element));
      }
      this.uniqueValues = __this__uniqueValues;
    }
    if (other.isSetDeltaValues()) {
      List<ApplyDelta> __this__deltaValues = new ArrayList<ApplyDelta>(other.deltaValues.size());
      for (ApplyDelta other_element : other.deltaValues) {
        __this__deltaValues.add(new ApplyDelta(other_element));
      }
      this.deltaValues = __this__deltaValues;
    }
    if (other.isSetTempNodePath()) {
      this.tempNodePath = other.tempNodePath;
    }
  }

  public CoordinatorRequest deepCopy() {
    return new CoordinatorRequest(this);
  }

  @Override
  public void clear() {
    this.requests = null;
    this.uniqueValues = null;
    this.deltaValues = null;
    this.tempNodePath = null;
  }

  public int getRequestsSize() {
    return (this.requests == null) ? 0 : this.requests.size();
  }

  public java.util.Iterator<RequestValue> getRequestsIterator() {
    return (this.requests == null) ? null : this.requests.iterator();
  }

  public void addToRequests(RequestValue elem) {
    if (this.requests == null) {
      this.requests = new ArrayList<RequestValue>();
    }
    this.requests.add(elem);
  }

  public List<RequestValue> getRequests() {
    return this.requests;
  }

  public CoordinatorRequest setRequests(List<RequestValue> requests) {
    this.requests = requests;
    return this;
  }

  public void unsetRequests() {
    this.requests = null;
  }

  /** Returns true if field requests is set (has been assigned a value) and false otherwise */
  public boolean isSetRequests() {
    return this.requests != null;
  }

  public void setRequestsIsSet(boolean value) {
    if (!value) {
      this.requests = null;
    }
  }

  public int getUniqueValuesSize() {
    return (this.uniqueValues == null) ? 0 : this.uniqueValues.size();
  }

  public java.util.Iterator<UniqueValue> getUniqueValuesIterator() {
    return (this.uniqueValues == null) ? null : this.uniqueValues.iterator();
  }

  public void addToUniqueValues(UniqueValue elem) {
    if (this.uniqueValues == null) {
      this.uniqueValues = new ArrayList<UniqueValue>();
    }
    this.uniqueValues.add(elem);
  }

  public List<UniqueValue> getUniqueValues() {
    return this.uniqueValues;
  }

  public CoordinatorRequest setUniqueValues(List<UniqueValue> uniqueValues) {
    this.uniqueValues = uniqueValues;
    return this;
  }

  public void unsetUniqueValues() {
    this.uniqueValues = null;
  }

  /** Returns true if field uniqueValues is set (has been assigned a value) and false otherwise */
  public boolean isSetUniqueValues() {
    return this.uniqueValues != null;
  }

  public void setUniqueValuesIsSet(boolean value) {
    if (!value) {
      this.uniqueValues = null;
    }
  }

  public int getDeltaValuesSize() {
    return (this.deltaValues == null) ? 0 : this.deltaValues.size();
  }

  public java.util.Iterator<ApplyDelta> getDeltaValuesIterator() {
    return (this.deltaValues == null) ? null : this.deltaValues.iterator();
  }

  public void addToDeltaValues(ApplyDelta elem) {
    if (this.deltaValues == null) {
      this.deltaValues = new ArrayList<ApplyDelta>();
    }
    this.deltaValues.add(elem);
  }

  public List<ApplyDelta> getDeltaValues() {
    return this.deltaValues;
  }

  public CoordinatorRequest setDeltaValues(List<ApplyDelta> deltaValues) {
    this.deltaValues = deltaValues;
    return this;
  }

  public void unsetDeltaValues() {
    this.deltaValues = null;
  }

  /** Returns true if field deltaValues is set (has been assigned a value) and false otherwise */
  public boolean isSetDeltaValues() {
    return this.deltaValues != null;
  }

  public void setDeltaValuesIsSet(boolean value) {
    if (!value) {
      this.deltaValues = null;
    }
  }

  public String getTempNodePath() {
    return this.tempNodePath;
  }

  public CoordinatorRequest setTempNodePath(String tempNodePath) {
    this.tempNodePath = tempNodePath;
    return this;
  }

  public void unsetTempNodePath() {
    this.tempNodePath = null;
  }

  /** Returns true if field tempNodePath is set (has been assigned a value) and false otherwise */
  public boolean isSetTempNodePath() {
    return this.tempNodePath != null;
  }

  public void setTempNodePathIsSet(boolean value) {
    if (!value) {
      this.tempNodePath = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case REQUESTS:
      if (value == null) {
        unsetRequests();
      } else {
        setRequests((List<RequestValue>)value);
      }
      break;

    case UNIQUE_VALUES:
      if (value == null) {
        unsetUniqueValues();
      } else {
        setUniqueValues((List<UniqueValue>)value);
      }
      break;

    case DELTA_VALUES:
      if (value == null) {
        unsetDeltaValues();
      } else {
        setDeltaValues((List<ApplyDelta>)value);
      }
      break;

    case TEMP_NODE_PATH:
      if (value == null) {
        unsetTempNodePath();
      } else {
        setTempNodePath((String)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case REQUESTS:
      return getRequests();

    case UNIQUE_VALUES:
      return getUniqueValues();

    case DELTA_VALUES:
      return getDeltaValues();

    case TEMP_NODE_PATH:
      return getTempNodePath();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case REQUESTS:
      return isSetRequests();
    case UNIQUE_VALUES:
      return isSetUniqueValues();
    case DELTA_VALUES:
      return isSetDeltaValues();
    case TEMP_NODE_PATH:
      return isSetTempNodePath();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof CoordinatorRequest)
      return this.equals((CoordinatorRequest)that);
    return false;
  }

  public boolean equals(CoordinatorRequest that) {
    if (that == null)
      return false;

    boolean this_present_requests = true && this.isSetRequests();
    boolean that_present_requests = true && that.isSetRequests();
    if (this_present_requests || that_present_requests) {
      if (!(this_present_requests && that_present_requests))
        return false;
      if (!this.requests.equals(that.requests))
        return false;
    }

    boolean this_present_uniqueValues = true && this.isSetUniqueValues();
    boolean that_present_uniqueValues = true && that.isSetUniqueValues();
    if (this_present_uniqueValues || that_present_uniqueValues) {
      if (!(this_present_uniqueValues && that_present_uniqueValues))
        return false;
      if (!this.uniqueValues.equals(that.uniqueValues))
        return false;
    }

    boolean this_present_deltaValues = true && this.isSetDeltaValues();
    boolean that_present_deltaValues = true && that.isSetDeltaValues();
    if (this_present_deltaValues || that_present_deltaValues) {
      if (!(this_present_deltaValues && that_present_deltaValues))
        return false;
      if (!this.deltaValues.equals(that.deltaValues))
        return false;
    }

    boolean this_present_tempNodePath = true && this.isSetTempNodePath();
    boolean that_present_tempNodePath = true && that.isSetTempNodePath();
    if (this_present_tempNodePath || that_present_tempNodePath) {
      if (!(this_present_tempNodePath && that_present_tempNodePath))
        return false;
      if (!this.tempNodePath.equals(that.tempNodePath))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_requests = true && (isSetRequests());
    list.add(present_requests);
    if (present_requests)
      list.add(requests);

    boolean present_uniqueValues = true && (isSetUniqueValues());
    list.add(present_uniqueValues);
    if (present_uniqueValues)
      list.add(uniqueValues);

    boolean present_deltaValues = true && (isSetDeltaValues());
    list.add(present_deltaValues);
    if (present_deltaValues)
      list.add(deltaValues);

    boolean present_tempNodePath = true && (isSetTempNodePath());
    list.add(present_tempNodePath);
    if (present_tempNodePath)
      list.add(tempNodePath);

    return list.hashCode();
  }

  @Override
  public int compareTo(CoordinatorRequest other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetRequests()).compareTo(other.isSetRequests());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetRequests()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.requests, other.requests);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetUniqueValues()).compareTo(other.isSetUniqueValues());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetUniqueValues()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.uniqueValues, other.uniqueValues);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetDeltaValues()).compareTo(other.isSetDeltaValues());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDeltaValues()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.deltaValues, other.deltaValues);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetTempNodePath()).compareTo(other.isSetTempNodePath());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTempNodePath()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tempNodePath, other.tempNodePath);
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
    StringBuilder sb = new StringBuilder("CoordinatorRequest(");
    boolean first = true;

    sb.append("requests:");
    if (this.requests == null) {
      sb.append("null");
    } else {
      sb.append(this.requests);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("uniqueValues:");
    if (this.uniqueValues == null) {
      sb.append("null");
    } else {
      sb.append(this.uniqueValues);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("deltaValues:");
    if (this.deltaValues == null) {
      sb.append("null");
    } else {
      sb.append(this.deltaValues);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("tempNodePath:");
    if (this.tempNodePath == null) {
      sb.append("null");
    } else {
      sb.append(this.tempNodePath);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
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

  private static class CoordinatorRequestStandardSchemeFactory implements SchemeFactory {
    public CoordinatorRequestStandardScheme getScheme() {
      return new CoordinatorRequestStandardScheme();
    }
  }

  private static class CoordinatorRequestStandardScheme extends StandardScheme<CoordinatorRequest> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, CoordinatorRequest struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // REQUESTS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list8 = iprot.readListBegin();
                struct.requests = new ArrayList<RequestValue>(_list8.size);
                RequestValue _elem9;
                for (int _i10 = 0; _i10 < _list8.size; ++_i10)
                {
                  _elem9 = new RequestValue();
                  _elem9.read(iprot);
                  struct.requests.add(_elem9);
                }
                iprot.readListEnd();
              }
              struct.setRequestsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // UNIQUE_VALUES
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list11 = iprot.readListBegin();
                struct.uniqueValues = new ArrayList<UniqueValue>(_list11.size);
                UniqueValue _elem12;
                for (int _i13 = 0; _i13 < _list11.size; ++_i13)
                {
                  _elem12 = new UniqueValue();
                  _elem12.read(iprot);
                  struct.uniqueValues.add(_elem12);
                }
                iprot.readListEnd();
              }
              struct.setUniqueValuesIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // DELTA_VALUES
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list14 = iprot.readListBegin();
                struct.deltaValues = new ArrayList<ApplyDelta>(_list14.size);
                ApplyDelta _elem15;
                for (int _i16 = 0; _i16 < _list14.size; ++_i16)
                {
                  _elem15 = new ApplyDelta();
                  _elem15.read(iprot);
                  struct.deltaValues.add(_elem15);
                }
                iprot.readListEnd();
              }
              struct.setDeltaValuesIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // TEMP_NODE_PATH
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.tempNodePath = iprot.readString();
              struct.setTempNodePathIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, CoordinatorRequest struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.requests != null) {
        oprot.writeFieldBegin(REQUESTS_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.requests.size()));
          for (RequestValue _iter17 : struct.requests)
          {
            _iter17.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.uniqueValues != null) {
        oprot.writeFieldBegin(UNIQUE_VALUES_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.uniqueValues.size()));
          for (UniqueValue _iter18 : struct.uniqueValues)
          {
            _iter18.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.deltaValues != null) {
        oprot.writeFieldBegin(DELTA_VALUES_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.deltaValues.size()));
          for (ApplyDelta _iter19 : struct.deltaValues)
          {
            _iter19.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.tempNodePath != null) {
        oprot.writeFieldBegin(TEMP_NODE_PATH_FIELD_DESC);
        oprot.writeString(struct.tempNodePath);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class CoordinatorRequestTupleSchemeFactory implements SchemeFactory {
    public CoordinatorRequestTupleScheme getScheme() {
      return new CoordinatorRequestTupleScheme();
    }
  }

  private static class CoordinatorRequestTupleScheme extends TupleScheme<CoordinatorRequest> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, CoordinatorRequest struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetRequests()) {
        optionals.set(0);
      }
      if (struct.isSetUniqueValues()) {
        optionals.set(1);
      }
      if (struct.isSetDeltaValues()) {
        optionals.set(2);
      }
      if (struct.isSetTempNodePath()) {
        optionals.set(3);
      }
      oprot.writeBitSet(optionals, 4);
      if (struct.isSetRequests()) {
        {
          oprot.writeI32(struct.requests.size());
          for (RequestValue _iter20 : struct.requests)
          {
            _iter20.write(oprot);
          }
        }
      }
      if (struct.isSetUniqueValues()) {
        {
          oprot.writeI32(struct.uniqueValues.size());
          for (UniqueValue _iter21 : struct.uniqueValues)
          {
            _iter21.write(oprot);
          }
        }
      }
      if (struct.isSetDeltaValues()) {
        {
          oprot.writeI32(struct.deltaValues.size());
          for (ApplyDelta _iter22 : struct.deltaValues)
          {
            _iter22.write(oprot);
          }
        }
      }
      if (struct.isSetTempNodePath()) {
        oprot.writeString(struct.tempNodePath);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, CoordinatorRequest struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(4);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TList _list23 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.requests = new ArrayList<RequestValue>(_list23.size);
          RequestValue _elem24;
          for (int _i25 = 0; _i25 < _list23.size; ++_i25)
          {
            _elem24 = new RequestValue();
            _elem24.read(iprot);
            struct.requests.add(_elem24);
          }
        }
        struct.setRequestsIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.thrift.protocol.TList _list26 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.uniqueValues = new ArrayList<UniqueValue>(_list26.size);
          UniqueValue _elem27;
          for (int _i28 = 0; _i28 < _list26.size; ++_i28)
          {
            _elem27 = new UniqueValue();
            _elem27.read(iprot);
            struct.uniqueValues.add(_elem27);
          }
        }
        struct.setUniqueValuesIsSet(true);
      }
      if (incoming.get(2)) {
        {
          org.apache.thrift.protocol.TList _list29 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.deltaValues = new ArrayList<ApplyDelta>(_list29.size);
          ApplyDelta _elem30;
          for (int _i31 = 0; _i31 < _list29.size; ++_i31)
          {
            _elem30 = new ApplyDelta();
            _elem30.read(iprot);
            struct.deltaValues.add(_elem30);
          }
        }
        struct.setDeltaValuesIsSet(true);
      }
      if (incoming.get(3)) {
        struct.tempNodePath = iprot.readString();
        struct.setTempNodePathIsSet(true);
      }
    }
  }

}

