package org.example.Configuration_Master;
 
public enum maturityLevel_comparison_types {
     less_than_or_equal_to,
                  equal_to,
  greater_than_or_equal_to;

  public char get_as_char() throws Exception { // throwing when something goes wrong here, b/c we can`t return null
    switch (this) {
      case    less_than_or_equal_to:  return '≤';
      case                 equal_to:  return '=';
      case greater_than_or_equal_to:  return '≥';
      default:  throw new Exception("Program logic error in ''maturityLevel_comparison_types.java''");
    }
  }

  public String get_as_a_twoChar_ASCII_string() {
    switch (this) {
      case    less_than_or_equal_to:  return "<=";
      case                 equal_to:  return "==";
      case greater_than_or_equal_to:  return ">=";
      default:                        return null;
    }
  }
}
