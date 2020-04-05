import java.io.*;
import java.util.Hashtable;
import java.util.regex.*;
import java.net.URL;
import java.net.MalformedURLException;
  

public class Configuration_Master_engine {

  private Hashtable<String, Integer> maturityLevel_aliases;

  private int get_maturityLevel_integer_from_alias(String alias_in) {
    return maturityLevel_aliases.get(alias_in.toLowerCase());
  }

  private class tuple_for_key_of_a_config {
    public maturityLevel_comparison_types the_MLC_kind;
    public int                            the_maturity_level_to_which_to_compare;
    public String                         the_namespace;
    public String                         the_key; // confusing, innit?  ;-)

    tuple_for_key_of_a_config(maturityLevel_comparison_types MLCt_in, int maturity_level_in, String namespace_in, String key_in) { // ctor
      the_MLC_kind                           = MLCt_in;
      the_maturity_level_to_which_to_compare = maturity_level_in;
      the_namespace                          = namespace_in;
      the_key                                = key_in;
    }

    @Override
    public int hashCode() {
      return (the_MLC_kind==null ? 0 : the_MLC_kind.hashCode()) ^ the_maturity_level_to_which_to_compare ^ (the_namespace==null ? 0 :  the_namespace.hashCode()) ^ (the_key==null ? 0 : the_key.hashCode());
    }

    @Override
    public boolean equals(Object o) {
      if (! tuple_for_key_of_a_config.class.isInstance(o))  return false;
      final tuple_for_key_of_a_config other = (tuple_for_key_of_a_config) o;
      return the_MLC_kind.equals(other.the_MLC_kind) && (the_maturity_level_to_which_to_compare == other.the_maturity_level_to_which_to_compare) && the_namespace.equals(other.the_namespace) && the_key.equals(other.the_key);
    }

    @Override
    public String toString() { // for debugging etc.
      return " tuple_for_key_of_a_config<the_MLC_kind=" + the_MLC_kind + ", the_maturity_level_to_which_to_compare=" + the_maturity_level_to_which_to_compare + ", the_namespace=" + stringize_safely(the_namespace) + ", the_key=" + stringize_safely(the_key) + "> ";
    }
  }


  private class tuple_for_key_of_a_schema {
    public String                         the_namespace;
    public String                         the_key; // confusing, innit?  ;-)

    tuple_for_key_of_a_schema(String namespace_in, String key_in) { // ctor
      the_namespace                          = namespace_in;
      the_key                                = key_in;
    }

    @Override
    public int hashCode() {
      return (the_namespace==null ? 0 :  the_namespace.hashCode()) ^ (the_key==null ? 0 : the_key.hashCode());
    }

    @Override
    public boolean equals(Object o) {
      if (! tuple_for_key_of_a_schema.class.isInstance(o))  return false;
      final tuple_for_key_of_a_schema other = (tuple_for_key_of_a_schema) o;
      return the_namespace.equals(other.the_namespace) && the_key.equals(other.the_key);
    }

    @Override
    public String toString() { // for debugging etc.
      return " tuple_for_key_of_a_schema<the_namespace=" + stringize_safely(the_namespace) + ", the_key=" + stringize_safely(the_key) + "> ";
    }
  }


  private class parsed_line_for_a_config {
    public tuple_for_key_of_a_config key;
    public config_algebraic_type     value;
    parsed_line_for_a_config(tuple_for_key_of_a_config key_in, config_algebraic_type value_in) {
      key   =   key_in;
      value = value_in;
    }
    public String toString() { // for debugging etc.
      return " parsed_line_for_a_config<key=" + key + ", value=" + value + "> ";
    }
  }


  private class parsed_line_for_a_schema {
    public tuple_for_key_of_a_schema key;
    public value_types               value;
    parsed_line_for_a_schema(tuple_for_key_of_a_schema key_in, value_types value_in) {
      key   =   key_in;
      value = value_in;
    }

    @Override
    public String toString() { // for debugging etc.
      return " parsed_line_for_a_schema<key=" + key + ", value=" + value + "> ";
    }
  }


  private Hashtable<String                   , value_types> typenames_to_types; // unfortunately, initializing a hashtable in Java is a {can of worms / Pandora`s box}, so we`ll do it the old-fashioned way

  private Hashtable<tuple_for_key_of_a_schema, value_types> the_schema;

  private static String stringize_safely(String input) {
    if (null == input)  return "«null»";
    return "“" + input + "”";
  }


  private parsed_line_for_a_schema parse_a_line_for_a_schema(String line) throws IOException {
    String                         the_namespace = null;
    String                         the_key       = null;
    String                         the_value_str = null;

    line = line.trim();
    if (line.length() > 0 && '#' != line.charAt(0)) { // ignore whole-line-possibly-modulo-leading-space comments
      line = line.replaceFirst("⍝.*", "").trim(); // HARD-CODED: the APL "lamp" symbol for an until-end-of-line comment, AKA "APL FUNCTIONAL SYMBOL UP SHOE JOT"
      final String[] the_split = line.split("␟"); // HARD-CODED: Unicode visible character for ASCII control "character" UNIT SEPARATOR

      // TO DO: make this fail more elegantly when the number of split results is not as expected

      the_namespace = the_split[0].trim();
      the_key       = the_split[1].trim();
      the_value_str = the_split[2].trim();
    }

    if ("*".equals(the_key)) {
      if (strict_checking_mode_enabled) {
        throw new IOException("Strict-checking mode violation: schema line parse indicates a line with an invalid key of ‘*’: “" + line + '”');
      }
      if (verbosity > 0) {
        System.err.println("\n\033[33mWARNING: schema line parse indicates a line with an invalid key of ‘*’: “" + line + "”; ignoring it.\033[0m\n");
      }
      return null;
    }

    if (null == the_namespace || the_namespace.length() < 1 || null == the_key || the_key.length() < 1 || null == the_value_str || the_value_str.length() < 1)  return null;

    return new parsed_line_for_a_schema(new tuple_for_key_of_a_schema(the_namespace, the_key), typenames_to_types.get(the_value_str)); // TO DO: make this fail gracefully when the typename "value" is unknown/unrecognized
  }


  private value_types get_type_from_schema(tuple_for_key_of_a_config the_key_of_the_config) {
    if (null == the_key_of_the_config.the_namespace || null == the_key_of_the_config.the_key)  return null;

    for (tuple_for_key_of_a_schema key_to_compare : the_schema.keySet()) {
      if (
             (the_key_of_the_config.the_namespace.equals(key_to_compare.the_namespace) || "*".equals(key_to_compare.the_namespace))
          &&
             (the_key_of_the_config.the_key.equals(key_to_compare.the_key) || "*".equals(key_to_compare.the_key))
         )
      {
        return the_schema.get(key_to_compare);
      }
    }
    return null; // nothing compatible found  :-(
  }


  private parsed_line_for_a_config parse_and_typecheck_a_line_for_a_config(String line) throws IOException {
    maturityLevel_comparison_types the_MLC_kind                           = maturityLevel_comparison_types.equal_to;
    int                            the_maturity_level_to_which_to_compare = -1;
    String                         the_namespace                          = null;
    String                         the_key                                = null;
    String                         the_value_str                          = null;

    line = line.trim();
    if (line.length() > 0 && '#' != line.charAt(0)) { // ignore whole-line-possibly-modulo-leading-space comments
      line = line.replaceFirst("⍝.*", "").trim(); // HARD-CODED: the APL "lamp" symbol for an until-end-of-line comment, AKA "APL FUNCTIONAL SYMBOL UP SHOE JOT"
      final String[] the_split = line.split("␟"); // HARD-CODED: Unicode visible character for ASCII control "character" UNIT SEPARATOR

      // TO DO: make this fail more elegantly when the number of split results is not as expected

      String the_MLC_spec_as_a_string = the_split[0].trim();

      if ("*".equals(the_MLC_spec_as_a_string))  the_MLC_spec_as_a_string = "≥0"; // this was the easiest way to implement this functionality...  "so sue me" if it isn`t very elegant

      final char the_MLC_operator_as_a_char = the_MLC_spec_as_a_string.charAt(0);
      switch (the_MLC_operator_as_a_char) {
        case '<': the_MLC_kind = maturityLevel_comparison_types.less_than;
          break;
        case '≤': the_MLC_kind = maturityLevel_comparison_types.less_than_or_equal_to;
          break;
        case '=': the_MLC_kind = maturityLevel_comparison_types.equal_to;
          break;
        case '≥': the_MLC_kind = maturityLevel_comparison_types.greater_than_or_equal_to;
          break;
        case '>': the_MLC_kind = maturityLevel_comparison_types.greater_than;
          break;
        default:
          throw new IOException("Syntax error: unrecognized leading character in a maturity-level specification.");
        }
      final String the_MLC_spec_after_the_initial_char = the_MLC_spec_as_a_string.substring(1).trim();
      if (Pattern.matches("\\d+", the_MLC_spec_after_the_initial_char)) {
        the_maturity_level_to_which_to_compare = Integer.parseInt(the_MLC_spec_after_the_initial_char);
      } else {
        the_maturity_level_to_which_to_compare = get_maturityLevel_integer_from_alias(the_MLC_spec_after_the_initial_char);
      }

      the_namespace = the_split[1].trim();
      the_key       = the_split[2].trim();
      the_value_str = the_split[3].trim();
    }

// saved for later: if (parse_result.key.the_maturity_level_to_which_to_compare < 0 || null == parse_result.key.the_namespace || null == parse_result.key.the_key || null == parse_result.value) {
    if (the_maturity_level_to_which_to_compare < 0 || null == the_namespace || the_namespace.length() < 1 || null == the_key || the_key.length() < 1 || null == the_value_str || the_value_str.length() < 1)  return null;

    config_algebraic_type the_value;

    final tuple_for_key_of_a_config the_key_of_the_config = new tuple_for_key_of_a_config(the_MLC_kind, the_maturity_level_to_which_to_compare, the_namespace, the_key);

    // first, just parse; we will validate later
    value_types the_VT = get_type_from_schema(the_key_of_the_config);
    if (null == the_VT) {
      throw new IOException("Error while type checking; do you have a configuration that is not represented in the schema?  Key: " + the_key_of_the_config);
    }
    switch (the_VT) {
      case             integer:
      case nonnegative_integer:
      case    positive_integer:
        the_value = new config_algebraic_type(Long.parseLong(the_value_str));
        break;

      case          string:
      case nonempty_string:
        the_value = new config_algebraic_type(the_value_str.replaceFirst("^“", "").replaceFirst("”$", ""));
        break;

      case URL:
        the_value = new config_algebraic_type(the_value_str.replaceFirst("^<", "").replaceFirst(">$", ""));
        break;

      default:
        throw new IOException("Internal implementation error: unrecognized value-type in configuration parser.");
    }

    // now validate!
    switch (the_VT) {
      case nonnegative_integer:
        if (the_value.get_as_long() < 0) {
          throw new IOException("Error while type checking; for key: " + the_key_of_the_config + " the value was " + the_value + " but the schema said the type was “nonnegative_integer”.");
        }
        break;

      case    positive_integer:
        if (the_value.get_as_long() < 1) {
          throw new IOException("Error while type checking; for key: " + the_key_of_the_config + " the value was " + the_value + " but the schema said the type was “positive_integer”.");
        }
        break;

      case nonempty_string:
        if (the_value.get_as_String().length() < 1) {
          throw new IOException("Error while type checking; for key: " + the_key_of_the_config + " the value was " + the_value + " but the schema said the type was “nonempty_string”.");
        }
        break;

      case URL:
        // -- the next line: hand-rolled URL validation via regex...  the Java library version is likely to be better in some way
        // if (the_value.get_as_String().length() < 1 || ! Pattern.matches("\\p{Alnum}+://[\\p{Alnum}-\\.]+(/\\p{Graph}*)?", the_value.get_as_String())) { // TO DO / WIP: add better URL checking... <https://docs.oracle.com/javase/6/docs/api/java/net/URL.html#URL(java.lang.String)>
        if (the_value.get_as_String().length() < 1 || ! is_this_string_a_valid_URL(the_value.get_as_String())) {
          throw new IOException("Error while type checking; for key: " + the_key_of_the_config + " the value was " + the_value + " but the schema said the type was “URL”.");
        }
        break;

      // _intentionally_ no "default:"
    }

    return new parsed_line_for_a_config(the_key_of_the_config, the_value);
  }


  private boolean is_this_string_a_valid_URL(String in) {
    try {
      URL to_throw_away = new URL(in);
      return true;
    } catch (MalformedURLException mue) { // expected, so not doing anything "special" with it
      return false;
    }
  }

  // every configuration value is currently assumed to be either {an integer compatible with a "long"} or {"string-like"}
  private class config_algebraic_type {
    private boolean use_string;
    private long  integer_value;
    private String string_value;

    public config_algebraic_type(long in) {
      integer_value = in;
      use_string = false;
    }

    public config_algebraic_type(String in) {
      string_value = in;
      use_string = true;
    }

    boolean should_get_as_String() {
      return use_string;
    }

    long get_as_long() {
      return integer_value;
    }

    String get_as_String() {
      return string_value;
    }

    public String toString() { // for debugging etc.
      return " config_algebraic_type<use_string=" + use_string + ", integer_value=" + integer_value + ", string_value=" + stringize_safely(string_value) + "> ";
    }
  }


  private Hashtable<tuple_for_key_of_a_config, config_algebraic_type> the_configurations;

  // the next 2 lines: so non-ctor methods will be able to read these values without me needing to pass it around
  private int verbosity;
  private boolean strict_checking_mode_enabled;


  // start of ctor
  Configuration_Master_engine(BufferedReader   maturityLevel_aliases_input,
                              BufferedReader[] schema_inputs,
                              BufferedReader[] config_inputs,
                              int              verbosity_in,
                              boolean          strict_checking_mode_enabled___in)
                                                throws IOException {

    verbosity = verbosity_in;
    strict_checking_mode_enabled = strict_checking_mode_enabled___in;

    typenames_to_types = new Hashtable<String, value_types>();
    for (value_types VT : value_types.values()) {
      typenames_to_types.put(VT.name(), VT);
    }
    if (verbosity > 1) {
      System.err.println();
      System.err.println("INFO: registered value types: " + typenames_to_types);
      System.err.println();
    }

    maturityLevel_aliases = new Hashtable<String, Integer>();

    try {

      while (maturityLevel_aliases_input.ready()) {
        String line = maturityLevel_aliases_input.readLine();
        if (verbosity > 5) {
          System.err.println("TESTING  1: maturity-level aliases input line: ''" + line + "''");
        }
        line = line.split("#")[0]; // discard comments
        if (verbosity > 5) {
          System.err.println("TESTING  2: maturity level aliases input line after discarding comments: ''" + line + "''");
        }
        line = line.replace(" ", "").toLowerCase(); // this algorithm will result in some "unexpected interpretations" for seemingly-invalid inputs, e.g. "d e v =" is equivalent to "dev=" and "1 2 3 4 5" is equivalent to "12345"
        if (verbosity > 5) {
          System.err.println("TESTING  3: maturity level aliases input line after removing all ASCII spaces and lower-casing: ''" + line + "''");
        }
        if (line.length() > 0) {
          Matcher m1 = Pattern.compile("(\\p{javaLowerCase}+)=(\\d+).*").matcher(line); // allows trailing "garbage"
          if (verbosity > 5) {
            System.err.println("TESTING  4: m1: " + m1);
            System.err.println("TESTING  5: m1.groupCount() -> " + m1.groupCount());
          }

          if (verbosity > 5) {
            System.err.println("TESTING  6: m1: " + m1);
          }
          final boolean line_matched_the_regex = m1.find(); // CRUCIAL
          if (verbosity > 5) {
            System.err.println("TESTING  7: line_matched_the_regex: " + line_matched_the_regex);
          }

          if (line_matched_the_regex) {

            if (verbosity > 5) {
              System.err.println("TESTING  8: m1: " + m1);
            }

            MatchResult mr1 = m1.toMatchResult();
            if (verbosity > 5) {
              System.err.println("TESTING  9: mr1: " + mr1);
              System.err.println("TESTING 10: mr1.groupCount() -> " + mr1.groupCount());
            }

            if (mr1.groupCount() != 2) {
              throw new IOException("Wrong number of groups in results for maturity-level aliases input line micro-parser: expected 2, got " + String.valueOf(mr1.groupCount()));
            }

            final String            alias = m1.group(1);
            if (verbosity > 4) {
              System.err.println("TESTING 11: alias=''" + alias + "''");
            }
            final String number_as_string = m1.group(2);
            final int number = Integer.parseInt(number_as_string);
            if (verbosity > 4) {
              System.err.println("TESTING 12: number: " + number);
              System.err.println();
            }
            if (number < 0) {
              throw new IOException("Negative number in maturity-level aliases: for alias ''" + alias + "'', got " + String.valueOf(number));
            }

            maturityLevel_aliases.put(alias, number);

          } /* if line_matched_the_regex */ else {
            throw new IOException("Syntax error in maturity-level aliases: ''" + line + "''");
          }

        } // if line.length() > 0

      } // end while maturityLevel_aliases_input.ready()

      if (verbosity > 1) {
        System.err.println();
        System.err.println("INFO: maturityLevel_aliases: " + maturityLevel_aliases);
        System.err.println();
      }

      the_schema = new Hashtable<tuple_for_key_of_a_schema, value_types>();
      for (BufferedReader schema_input : schema_inputs) {
        while (schema_input.ready()) {
          String line = schema_input.readLine();
          if (verbosity > 5) {
            System.err.println();
            System.err.println("TESTING 13: schema input line: ''" + line + "''");
          }

          parsed_line_for_a_schema parse_result = parse_a_line_for_a_schema(line);
          if (verbosity > 5) {
            System.err.println("TESTING 14: schema line parse: " + parse_result);
          }

          if (null == parse_result || null == parse_result.key || null == parse_result.key.the_namespace || null == parse_result.key.the_key || null == parse_result.value) {
            if (verbosity > 5) {
              System.err.println("TESTING 15: schema line parse indicates not a line with valid data, e.g. an effectively-blank or all-comment line");
            }
            if (null != parse_result && null != parse_result.key && null != parse_result.key.the_namespace && null != parse_result.key.the_key && null == parse_result.value) {
              if (strict_checking_mode_enabled) {
                throw new IOException("Strict-checking mode violation: schema line parse seems to indicate a line with valid key and namespace, but an _invalid_ type value: “" + line + '”');
              }
              if (verbosity > 0) {
                System.err.println("\n\033[33mWARNING: schema line parse seems to indicate a line with valid key and namespace, but an _invalid_ type value: “" + line + "”; ignoring it.\033[0m\n");
              }
            }
          } else { // looks like a valid line
            if (verbosity > 5) {
              System.err.println("TESTING 16: schema line parse indicates a line with valid data!  Hooray!!!");
            }
            if (the_schema.containsKey(parse_result.key)) {
              final value_types old_VT = the_schema.get(parse_result.key);
              if (old_VT.equals(parse_result.value)) {
                if (verbosity > 5) {
                  System.err.println("TESTING 17: schema line seems to be valid, but redundant.  Ignoring.");
                }
              } else {
                throw new IOException("Data inconsistency: conflicting line for schema: ''" + line + "'' conflicts with prior parse result value-type «" + old_VT + '»');
              }
            } else { // if _not_ (the_schema.containsKey(parse_result.key))
              the_schema.put(parse_result.key, parse_result.value);
            }
          }

        } // end while
      } // end for BufferedReader schema_input : schema_inputs

      if (verbosity > 0) {
        System.err.println();
        System.err.println("INFO: the_schema: " + the_schema);
        System.err.println();
      }

      // --- "asterisk validation" for the schema --- //

      // this algorithm is slow and probably also stupid
      for (tuple_for_key_of_a_schema outer_key : the_schema.keySet()) {
        if ("*".equals(outer_key.the_namespace)) {
          for (tuple_for_key_of_a_schema inner_key : the_schema.keySet()) {
            if (outer_key.the_key.equals(inner_key.the_key) && the_schema.get(outer_key) != the_schema.get(inner_key)) {
              throw new IOException("Data inconsistency: conflicting for-all-namespaces in schema: " + outer_key + " mapping to «" + the_schema.get(outer_key) + "» conflicts with " + inner_key + " mapping to «" + the_schema.get(inner_key) + '»');
            }
          }
        }
      }

      // --- done parsing and validating the schema --- //

      the_configurations = new Hashtable<tuple_for_key_of_a_config, config_algebraic_type>();
      for (BufferedReader config_input : config_inputs) {
        while (config_input.ready()) {
          String line = config_input.readLine();
          if (verbosity > 1) {
            System.err.println();
            System.err.println("TESTING 18: config. input line: ''" + line + "''");
          }

          parsed_line_for_a_config parse_result = parse_and_typecheck_a_line_for_a_config(line);
          if (verbosity > 1) {
            System.err.println("TESTING 19: config line parse: " + parse_result);
          }

          if (null != parse_result)  the_configurations.put(parse_result.key, parse_result.value); // save it if it`s good

        } // end while
      } // end for BufferedReader config_input : config_inputs


      // --- "asterisk validation" for the configurations     --- //
      // --- performance warning: this is BRUTE FORCE for now --- //

      /*
      for (tuple_for_key_of_a_config outer_key : the_schema.keySet()) {
        if ("*".equals(outer_key.the_namespace)) {
          for (tuple_for_key_of_a_schema inner_key : the_schema.keySet()) {
            if (outer_key.the_key.equals(inner_key.the_key) && the_schema.get(outer_key) != the_schema.get(inner_key)) {
              throw new IOException("Data inconsistency: conflicting for-all-namespaces in schema: " + outer_key + " mapping to «" + the_schema.get(outer_key) + "» conflicts with " + inner_key + " mapping to «" + the_schema.get(inner_key) + '»');
            }
          }
        }
      }
      */



      if (verbosity > 0) {
        System.err.println();
        System.err.println("INFO: the_configurations: " + the_configurations);
        System.err.println();
      }

    } catch (IOException ioe) {

       final String response = "An I/O exception occurred while trying to initialize the Configuration Master engine: " + ioe;
       System.err.println("\033[31m" + response + "\033[0m");
       throw new IOException(response); // enabling the following snippet didn`t seem to add anything perceptible: , ioe.getCause());
    } // end of try-catch

  } // end of ctor




// get_configuration(maturity_level, namespace, key) // WIP

  public static String get_configuration(int maturity_level_of_query, String namespace_of_query, String key_of_query) {

    // WIP WIP WIP //
    return "<place-holder response>"; // place-holder
    // WIP WIP WIP //

  }


/* for later, for the configuration getter:

        switch (the_key_of_the_config.the_MLC_kind) {
          case :
            break;
          case :
            break;
          case :
            break;
          case :
            break;
          case :
            break;
        }

*/

} // end of class "Configuration_Master_engine"
