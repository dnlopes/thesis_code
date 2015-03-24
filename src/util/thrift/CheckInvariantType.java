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

public enum CheckInvariantType implements org.apache.thrift.TEnum {
  UNIQUE(0),
  FOREIGN_KEY(1),
  GREATHER_THAN(2),
  LESSER_THAN(3),
  DELETE_VALUE(4),
  DELTA(5),
  REQUEST_VALUE(6);

  private final int value;

  private CheckInvariantType(int value) {
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
  public static CheckInvariantType findByValue(int value) { 
    switch (value) {
      case 0:
        return UNIQUE;
      case 1:
        return FOREIGN_KEY;
      case 2:
        return GREATHER_THAN;
      case 3:
        return LESSER_THAN;
      case 4:
        return DELETE_VALUE;
      case 5:
        return DELTA;
      case 6:
        return REQUEST_VALUE;
      default:
        return null;
    }
  }
}