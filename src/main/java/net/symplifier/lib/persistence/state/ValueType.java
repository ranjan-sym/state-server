package net.symplifier.lib.persistence.state;

import java.util.Date;

/**
 * Created by ranjan on 11/17/15.
 */
public interface ValueType {

  default boolean isElement() { return false; }
  default boolean isList() { return false; }
  default boolean isGeneric() { return !isElement() && !isList(); }
  default String toString(Object value) { return value.toString(); }

  String getName();

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
