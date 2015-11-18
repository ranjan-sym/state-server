package net.symplifier.lib.persistence.state;

import java.util.Date;

/**
 * The Type required for the values of an Element. There are 3 types of value
 * allowed to be set to the Element fields at the moment:
 * <ol>
 *   <li><b>Generic: </b>A Generic type represents all the standard data type
 *   - {@link Byte}, {@link Integer}, {@link Short}, {@link Long},
 *   {@link Float}, {@link Double}, {@link Boolean}, {@link String} and
 *   {@link Date}</li>
 *   <li><b>Element: </b>A Element type represents a reference to another
 *   {@link net.symplifier.lib.persistence.state.State.Element}</li>
 *   <li><b>List: </b>A List type represents a list of
 *   {@link net.symplifier.lib.persistence.state.State.Element} of a specific
 *   type</li>
 * </ol>
 * Created by ranjan on 11/17/15.
 */
public interface ValueType {

  /**
   * Determines if the type is an Element. This value is set by {@link ElementType}
   *
   * @return {@code true} if the value returned is going to be an Element
   */
  default boolean isElement() { return false; }

  /**
   * Determines if the type is a list of Element. This value is set by
   * {@link net.symplifier.lib.persistence.state.ElementType.ListType}
   *
   * @return {@code true} if the value returned is going to be a list of Elements
   */
  default boolean isList() { return false; }

  /**
   * Determines if the type is a generic data type.
   *
   * @return {@code true} if the value is going to be one of the generic data
   * type including Date
   */
  default boolean isGeneric() { return !isElement() && !isList(); }

  /**
   * Converts the given value to a String based on the value type.
   *
   * @param value The value to be converted
   * @return String representation of the given value as interpreted by the type
   */
  default String toString(Object value) { return value.toString(); }

  /**
   * Returns the name used to identify this type
   *
   * @return
   */
  String getName();

  /**
   * Checks if the given value object is valid for this Value Type. Used for
   * assertion while the values are being updated on the {@link ElementProxy}
   *
   * @param value The value to check
   * @return {@code true} if the value is a valid type
   */
  boolean isValid(Object value);

  ValueType BYTE = new ValueType() {
    public String getName() {
      return "Byte";
    }

    @Override
    public boolean isValid(Object value) {
      return value instanceof Byte;
    }
  };
  ValueType INTEGER = new ValueType() {
    @Override
    public String getName() {
      return "Integer";
    }

    @Override
    public boolean isValid(Object value) {
      return value instanceof Integer;
    }
  };
  ValueType SHORT = new ValueType() {

    @Override
    public String getName() {
      return "Short";
    }

    @Override
    public boolean isValid(Object value) {
      return value instanceof Short;
    }
  };
  ValueType LONG = new ValueType() {
    @Override
    public String getName() {
      return "Long";
    }

    @Override
    public boolean isValid(Object value) {
      return value instanceof Long;
    }
  };
  ValueType BOOLEAN = new ValueType() {
    @Override
    public String getName() {
      return "Boolean";
    }

    @Override
    public boolean isValid(Object value) {
      return value instanceof Boolean;
    }
  };
  ValueType CHARACTER = new ValueType() {
    @Override
    public String getName() {
      return "Character";
    }

    @Override
    public boolean isValid(Object value) {
      return value instanceof Character;
    }
  };
  ValueType STRING = new ValueType() {
    @Override
    public String getName() {
      return "String";
    }

    @Override
    public boolean isValid(Object value) {
      return value instanceof String;
    }
  };
  ValueType FLOAT = new ValueType() {
    @Override
    public String getName() {
      return "Float";
    }

    @Override
    public boolean isValid(Object value) {
      return value instanceof Float;
    }
  };
  ValueType DOUBLE = new ValueType() {
    @Override
    public String getName() {
      return "Double";
    }

    @Override
    public boolean isValid(Object value) {
      return value instanceof Double;
    }
  };
  ValueType DATE = new ValueType() {
    @Override
    public String getName() {
      return "Date";
    }

    @Override
    public boolean isValid(Object value) {
      return value instanceof Date;
    }
  };

}
