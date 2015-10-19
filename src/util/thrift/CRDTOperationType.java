/**
 * Autogenerated by Thrift Compiler (0.9.2)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package util.thrift;


import java.util.Map;
import java.util.HashMap;
import org.apache.thrift.TEnum;

public enum CRDTOperationType implements org.apache.thrift.TEnum {
  INSERT(0),
  INSERT_CHILD(1),
  UPDATE(2),
  UPDATE_CHILD(3),
  DELETE(4),
  DELETE_PARENT(5);

  private final int value;

  private CRDTOperationType(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  public static CRDTOperationType findByValue(int value) { 
    switch (value) {
      case 0:
        return INSERT;
      case 1:
        return INSERT_CHILD;
      case 2:
        return UPDATE;
      case 3:
        return UPDATE_CHILD;
      case 4:
        return DELETE;
      case 5:
        return DELETE_PARENT;
      default:
        return null;
    }
  }
}