package Configuration_Master;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.*;
import java.net.URL;
import java.net.MalformedURLException;
  
import static Configuration_Master.utility_class.*;

public class Configuration_Master_engine {

  private Hashtable<String, Integer> maturityLevel_aliases;

  public int get_maturityLevel_integer_from_alias(String alias_in) {
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
      return the_MLC_kind.equals(other.the_MLC_kind) && (the_maturity_level_to_which_to_compare == other.the_maturity_level_to_which_to_compare) && the_namespace.equalsIgnoreCase(other.the_namespace) && the_key.equalsIgnoreCase(other.the_key);
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
      return the_namespace.equalsIgnoreCase(other.the_namespace) && the_key.equalsIgnoreCase(other.the_key);
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

  private value_types get_type_by_name_ignoring_case(String name_in) { // it would be nice if Java supported comparator objects/classes
    if (null == name_in)  return null;
    for (String name : typenames_to_types.keySet()) {
      if (name_in.equalsIgnoreCase(name))  return typenames_to_types.get(name);
    }
    return null;
  }

  private Hashtable<tuple_for_key_of_a_schema, value_types> the_schema;

  private parsed_line_for_a_schema parse_a_line_for_a_schema(String line, String source) throws IOException {
    String                         the_namespace = null;
    String                         the_key       = null;
    String                         the_value_str = null;

    line = line.trim();
    if (line.length() > 0 && '#' != line.charAt(0)) { // ignore whole-line-possibly-modulo-leading-space comments
      line = line.replaceFirst("⍝.*", "").trim(); // HARD-CODED: the APL "lamp" symbol for an until-end-of-line comment, AKA "APL FUNCTIONAL SYMBOL UP SHOE JOT"
      final String[] the_split = line.split("␟"); // HARD-CODED: Unicode visible character for ASCII control "character" UNIT SEPARATOR

      // TO DO: make this fail more elegantly when the number of split results is not as expected

      the_namespace = the_split[0].trim().toLowerCase();
      the_key       = the_split[1].trim().toLowerCase();
      the_value_str = the_split[2].trim();
    }

    if ("*".equals(the_key)) {
      if (strict_checking_mode_enabled) {
        throw new IOException("Strict-checking mode violation: schema line parse indicates a line with an invalid key of ‘*’: “" + line + "” at " + source);
      }
      if (verbosity > 0) {
        System.err.println("\n\033[33mWARNING: schema line parse indicates a line with an invalid key of ‘*’: “" + line + "” at " + source + "; ignoring it.\033[0m\n");
      }
      return null;
    }

    if (null == the_namespace || the_namespace.length() < 1 || null == the_key || the_key.length() < 1 || null == the_value_str || the_value_str.length() < 1)  return null;

    return new parsed_line_for_a_schema(new tuple_for_key_of_a_schema(the_namespace, the_key), get_type_by_name_ignoring_case(the_value_str)); // TO DO: make this fail gracefully when the typename "value" is unknown/unrecognized
  }


  private value_types get_type_from_schema(tuple_for_key_of_a_config the_key_of_the_config) {
    if (null == the_key_of_the_config.the_namespace || null == the_key_of_the_config.the_key)  return null;

    for (tuple_for_key_of_a_schema key_to_compare : the_schema.keySet()) {
      if (
             (the_key_of_the_config.the_namespace.equalsIgnoreCase(key_to_compare.the_namespace) || "*".equals(key_to_compare.the_namespace))
          &&
             (the_key_of_the_config.the_key.equalsIgnoreCase(key_to_compare.the_key) || "*".equals(key_to_compare.the_key))
         )
      {
        return the_schema.get(key_to_compare);
      }
    }
    return null; // nothing compatible found  :-(
  }


  private parsed_line_for_a_config parse_and_typecheck_a_line_for_a_config(String line, String source) throws IOException {
    maturityLevel_comparison_types the_MLC_kind                           = maturityLevel_comparison_types.equal_to;
    int                            the_maturity_level_to_which_to_compare = -2; // _do_ use a negative value as a sentinel, but do _not_ use -1, since that value is "reserved" for the desugared integer value of the nonsensical MLC "<0"
    String                         the_namespace                          = null;
    String                         the_key                                = null;
    String                         the_value_str                          = null;

    // line = line.trim(); // I`m pretty sure this line [pun intended ;-)] is redundant now that the line that handled '⍝'-style comments is immediately after this line of code, which originally it was _not_
    line = line.replaceFirst("⍝.*", "").trim(); // HARD-CODED: the APL "lamp" symbol for an until-end-of-line comment, AKA "APL FUNCTIONAL SYMBOL UP SHOE JOT"
    if (line.length() < 1 || '#' == line.charAt(0)) { // ignore whole-line-possibly-modulo-leading-space comments
      return null; // this makes the following code in this function _much_ simpler and more reliable when an empty line or effectively-all-comment line comes in
    }

    final String[] the_split = line.split("␟", -1); // HARD-CODED: Unicode visible character for ASCII control "character" UNIT SEPARATOR; the -1 prevents problems with the default "split" [i.e. the single-operand one] removing/{not including} the empty string at the end of the array when the last char. of the string being split is the delimiter, which screws up our ability to detect a missing value field [since the program just crashes when trying to access "the_split[3]"]

    // TO DO: make this fail more elegantly when the number of split results is not as expected

    String the_MLC_spec_as_a_string = the_split[0].trim();

    if ("*".equals(the_MLC_spec_as_a_string))  the_MLC_spec_as_a_string = "≥0"; // this was the easiest way to implement this functionality...  "so sue me" if it isn`t very elegant

    final String the_MLC_spec_after_the_initial_char = the_MLC_spec_as_a_string.substring(1).trim();
    // System.err.println("DEBUG: the_MLC_spec_after_the_initial_char=''" + the_MLC_spec_after_the_initial_char + "''");
    if (Pattern.matches("\\d+", the_MLC_spec_after_the_initial_char)) { // negative integers are _intentionally_ unsupported
      the_maturity_level_to_which_to_compare = Integer.parseInt(the_MLC_spec_after_the_initial_char);
      if (the_maturity_level_to_which_to_compare < 0)
        throw new IOException("SYNTAX ERROR: negative integer in a maturity-level specification at " + source);
    } else {
      the_maturity_level_to_which_to_compare = get_maturityLevel_integer_from_alias(the_MLC_spec_after_the_initial_char);
      if (the_maturity_level_to_which_to_compare < 0)
        throw new IOException("FLAGRANT SYSTEM ERROR: negative integer found in a maturity-level specification _after_ converting from an alias; not only is a negative mapping from an alias _bad_, but it should not even be _possible_ to have at this point in the program.  Source: " + source);
    }

    final char the_MLC_operator_as_a_char = the_MLC_spec_as_a_string.charAt(0);
    switch (the_MLC_operator_as_a_char) {
      case '<': // example: "<5" is really just syntactic sugar for "≤4"
                the_MLC_kind = maturityLevel_comparison_types.less_than_or_equal_to;
                --the_maturity_level_to_which_to_compare;
        break;
      case '≤': the_MLC_kind = maturityLevel_comparison_types.less_than_or_equal_to;
        break;
      case '=': the_MLC_kind = maturityLevel_comparison_types.equal_to;
        break;
      case '≥': the_MLC_kind = maturityLevel_comparison_types.greater_than_or_equal_to;
        break;
      case '>': // example: ">5" is really just syntactic sugar for "≥6"
                the_MLC_kind = maturityLevel_comparison_types.greater_than_or_equal_to;
                ++the_maturity_level_to_which_to_compare;
        break;
      default:
        throw new IOException("SYNTAX ERROR: unrecognized leading character in a maturity-level specification, at " + source);
      // no closing brace here due to my way of indenting inside switch blocks
    }

    the_namespace = the_split[1].trim().toLowerCase();
    the_key       = the_split[2].trim().toLowerCase();
    the_value_str = the_split[3].trim();

    if (the_maturity_level_to_which_to_compare == -1) { // this can [_only_, I hope] happen if/when the syntactic MLC is "<0", thus desugaring to "≤-1"
      if (strict_checking_mode_enabled)
        throw new IOException("Grammatical error: an MLC was effectively comparing to -1, which almost-certainly means that the literal input to the MLC engine was “<0” [module ASCII spaces], which is a nonsensical MLC that will _never_ match _any_ queries since negative numbers are explicitly disallowed in both literal integers in MLCs and in the values of maturity-level aliases; input line, after space trimming and comment removal: «" + line + "»; source: " + source);
      else {
        System.err.println("\n\033[33mWARNING: an MLC with a seemingly-nonsensical-in-this-context integer value [will _never_ match _any_ query] was found in the line «" + line + "» at " + source + "; ignoring it.\033[0m\n");
        return null; // ignore the invalid input in non-strict mode
      }
    }

    if (null == the_namespace || null == the_key || null == the_value_str)
      throw new IOException("Internal error: in the config. parser, at least one of the 3 String references was null in a place where this should be impossible.  Source: " + source);

    if (the_namespace.length() < 1 || the_key.length() < 1 || the_value_str.length() < 1) {
      if (strict_checking_mode_enabled)
        throw new IOException("Grammatical error: a configuration which seems to be missing at least one of {namespace, key, value} was found in the line «" + line + "» at " + source);
      else {
        System.err.println("\n\033[33mWARNING: a configuration which seems to be missing at least one of {namespace, key, value} was found in the line «" + line + "» at " + source + "; ignoring it.\033[0m\n");
        return null; // ignore the invalid input in non-strict mode
      }
    }

    if (the_maturity_level_to_which_to_compare < 0)  throw new IOException("Internal error: a parsed/“compiled” MLC`s integer value was negative despite all the efforts to prevent such a condition from reaching the point in the code where this exception was thrown.  Source: " + source); // this must come _after_ the line of code that returns null when the line of input was either empty or effectively all-comment

    config_algebraic_type the_value;

    final tuple_for_key_of_a_config the_key_of_the_config = // ...
      new tuple_for_key_of_a_config(the_MLC_kind, the_maturity_level_to_which_to_compare, the_namespace, the_key);

    // first, just parse; we will validate later
    value_types the_VT = get_type_from_schema(the_key_of_the_config);
    if (null == the_VT) {
      throw new IOException("Error while type checking; do you have a configuration that is not represented in the schema?  Key: " + the_key_of_the_config + "; source: " + source);
    }
    switch (the_VT) {
      case             integer:
      case nonnegative_integer:
      case    positive_integer:
        the_value = new config_algebraic_type(Long.parseLong(the_value_str));
        break;

      case          string:
      case nonempty_string:
        if ('“' != the_value_str.charAt(0) || '”' != the_value_str.charAt(the_value_str.length()-1)) {
          throw new IOException("Error while type checking; SYNTAX ERROR for a string.  Key: " + the_key_of_the_config + "; source: " + source);
        }
        the_value = new config_algebraic_type(the_value_str.replaceFirst("^“", "").replaceFirst("”$", ""));
        break;

      case URL:
        if ('<' != the_value_str.charAt(0) || '>' != the_value_str.charAt(the_value_str.length()-1)) {
          throw new IOException("Error while type checking; SYNTAX ERROR for a URL.  Key: " + the_key_of_the_config + "; source: " + source);
        }
        the_value = new config_algebraic_type(the_value_str.replaceFirst("^<", "").replaceFirst(">$", ""));
        break;

      default:
        throw new IOException("Internal implementation error: unrecognized value-type in configuration parser.  Source: " + source);
    }

    // now validate!
    switch (the_VT) {
      case nonnegative_integer:
        if (the_value.get_as_long() < 0) {
          throw new IOException("Error while type checking; for key: " + the_key_of_the_config + " the value was " + the_value + " but the schema said the type was “nonnegative_integer”.  Source: " + source);
        }
        break;

      case    positive_integer:
        if (the_value.get_as_long() < 1) {
          throw new IOException("Error while type checking; for key: " + the_key_of_the_config + " the value was " + the_value + " but the schema said the type was “positive_integer”.  Source: " + source);
        }
        break;

      case nonempty_string:
        if (the_value.get_as_String().length() < 1) {
          throw new IOException("Error while type checking; for key: " + the_key_of_the_config + " the value was " + the_value + " but the schema said the type was “nonempty_string”.  Source: " + source);
        }
        break;

      case URL:
        // -- the next line: hand-rolled URL validation via regex...  the Java library version is likely to be better in some way
        // if (the_value.get_as_String().length() < 1 || ! Pattern.matches("\\p{Alnum}+://[\\p{Alnum}-\\.]+(/\\p{Graph}*)?", the_value.get_as_String())) {
        if (the_value.get_as_String().length() < 1 || ! is_this_string_a_valid_URL(the_value.get_as_String())) {
          throw new IOException("Error while type checking; for key: " + the_key_of_the_config + " the value was " + the_value + " but the schema said the type was “URL”.  Source: " + source);
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

    String get_as_String_even_if_the_value_is_an_integer() { // for the main getter that provides this engine with its raison dêtre
      if (use_string)  return string_value;
      return String.valueOf(integer_value);
    }

    // this is needed for the correct "asterisk validation" of the configurations, if not also for other things
    public boolean equals(config_algebraic_type other) {
      if (null == other)                                       return false; // embedded assumption: "this" can never be null
      if (use_string != other.use_string)                      return false;
      if (! use_string)                                        return integer_value == other.integer_value;
      if (null == string_value || null == other.string_value)  return string_value == other.string_value; // _intentionally_ comparing pointers
      return string_value.equals(other.string_value);
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
  Configuration_Master_engine(
                               debugFriendly_buffered_input   maturityLevel_aliases_input,
                               debugFriendly_buffered_input[] schema_inputs,
                               debugFriendly_buffered_input[] config_inputs,
                               int                            verbosity_in,
                               boolean                        strict_checking_mode_enabled___in
                             )
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
          System.err.println();
          System.err.println("TESTING  1: maturity-level aliases input line: «" + line + "» read from " + maturityLevel_aliases_input.get_description_of_input_and_current_position());
        }
        line = line.replaceFirst("[#⍝].*", ""); // discard comments
        if (verbosity > 5) {
          System.err.println("TESTING  2: maturity level aliases input line after discarding comments: «" + line + '»');
        }
        line = line.replace(" ", "").toLowerCase(); // this algorithm will result in some "unexpected interpretations" for seemingly-invalid inputs, e.g. "d e v =" is equivalent to "dev=" and "1 2 3 4 5" is equivalent to "12345"
        if (verbosity > 5) {
          System.err.println("TESTING  3: maturity level aliases input line after removing all ASCII spaces and lower-casing: «" + line + '»');
        }
        if (line.length() > 0) {
          // identifier language: [a letter][letters, ASCII dashes, and ASCII underscores]*
          Matcher m1 = Pattern.compile("(\\p{IsL}[\\p{IsL}-_]*)((?:,\\p{IsL}[\\p{IsL}-_]*)*)=(\\d+).*").matcher(line); // allows trailing "garbage"; "{IsL}" is Java regex for "Is a Letter according to Unicode [includes ideographics and uncased alphabets/abugidas]
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

            if (mr1.groupCount() != 3) {
              throw new IOException("Wrong number of groups in results for maturity-level aliases input line micro-parser: expected 3, got " + String.valueOf(mr1.groupCount()) + " ... at " + maturityLevel_aliases_input.get_description_of_input_and_current_position()); // this one may never trigger, since the line is a syntax error [i.e. a failure to match the required regex]
            }

            final String         first_alias = m1.group(1);
            final String more_aliases_if_any = m1.group(2);
            if (verbosity > 5) {
              System.err.println("TESTING 11: first_alias=" + stringize_safely(first_alias) + ", more_aliases_if_any = " + stringize_safely(more_aliases_if_any));
            }
            final String number_as_string = m1.group(3);
            final int number = Integer.parseInt(number_as_string);
            if (verbosity > 5) {
              System.err.println("TESTING 12: number: " + number);
            }
            if (number < 0) {
              throw new IOException("Negative number in maturity-level aliases: for aliases ''" + first_alias + more_aliases_if_any + "'', got " + String.valueOf(number) + " ... at " + maturityLevel_aliases_input.get_description_of_input_and_current_position()); // this one may never trigger, since the '-' in e.g. "-1" is a syntax error [i.e. a failure to match the required regex]
            }

            ArrayList<String> aliases_for_this_number = new ArrayList<String>();
            aliases_for_this_number.add(first_alias);
            for (String alias : more_aliases_if_any.split(",")) {
              if (alias != null && alias.length() > 0) { // being careful, in case of wierdness in "split"
                aliases_for_this_number.add(alias);
              }
            }

            for (String alias : aliases_for_this_number) {

              if (maturityLevel_aliases.containsKey(alias)) {
                final int old_def = maturityLevel_aliases.get(alias);
                if (number != old_def || strict_checking_mode_enabled) {
                  throw new IOException("Error in maturity-level aliases: " + (number == old_def ? "redundant" : "conflicting") + " redefinition of the alias ''" + alias + "'' in the line ''" + line + "'' at " + maturityLevel_aliases_input.get_description_of_input_and_current_position());
                }
                System.err.println("\n\033[33mWARNING: redundant redefinition of the maturity level alias ''" + alias + "'' to the same value it had before: “" + line + "” at " + maturityLevel_aliases_input.get_description_of_input_and_current_position() + "; ignoring it.\033[0m\n");
              } else {
                if (verbosity > 5) {
                  System.err.println("TESTING 12.5: set mapping from alias ''" + alias + "'' to " + number);
                }
                maturityLevel_aliases.put(alias, number);
              }

            }

          } /* if line_matched_the_regex */ else {
            throw new IOException("Syntax error in maturity-level aliases: ''" + line + "'' at " + maturityLevel_aliases_input.get_description_of_input_and_current_position());
          }

        } // if line.length() > 0

      } // end while maturityLevel_aliases_input.ready()

      if (verbosity > 1) {
        System.err.println();
        System.err.println("INFO: maturityLevel_aliases: " + maturityLevel_aliases);
        System.err.println();
      }

      the_schema = new Hashtable<tuple_for_key_of_a_schema, value_types>();
      for (debugFriendly_buffered_input schema_input : schema_inputs) {
        while (schema_input.ready()) {
          String line = schema_input.readLine();
          if (verbosity > 5) {
            System.err.println();
            System.err.println("TESTING 13: schema input line: ''" + line + "''");
          }

          parsed_line_for_a_schema parse_result = parse_a_line_for_a_schema(line, schema_input.get_description_of_input_and_current_position());
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
      for (debugFriendly_buffered_input config_input : config_inputs) {
        while (config_input.ready()) {
          String line = config_input.readLine();
          if (verbosity > 5) {
            System.err.println();
            System.err.println("TESTING 18: config. input line: «" + line + '»');
          }

          parsed_line_for_a_config parse_result = parse_and_typecheck_a_line_for_a_config(line, config_input.get_description_of_input_and_current_position());
          if (verbosity > 5) {
            System.err.println("TESTING 19: config line parse: " + parse_result);
          }


          if (null != parse_result) {

            if (verbosity > 5) {
              System.err.println("TESTING 20: config. line parse indicates a line with valid data!  Hooray!!!");
            }
            if (the_configurations.containsKey(parse_result.key)) {
              final config_algebraic_type old_value = the_configurations.get(parse_result.key);
              if (old_value.equals(parse_result.value)) {
                if (verbosity > 5) {
                  System.err.println("TESTING 21: config. line seems to be valid, but redundant.  Ignoring.");
                }
              } else {
                throw new IOException("Data inconsistency: conflicting line for configuration: «" + line + "» conflicts with prior parse result value " + old_value);
              }
            } else { // if _not_ (the_schema.containsKey(parse_result.key))
              the_configurations.put(parse_result.key, parse_result.value);
            }

          } // end if null != parse_result

        } // end while
      } // end for BufferedReader config_input : config_inputs


      // INCOMPLETENESS WARNING: this implementation almost-certainly only finds conflicts that have the same maturity-level comparison specifier

      // --- "asterisk validation" for the configurations     --- //
      // --- performance warning: this is BRUTE FORCE for now --- //

      // this checker is conservative, but not complete; IOW, it cannot find all the relevant conflicts that could possibly exist

      for (tuple_for_key_of_a_config outer_key : the_configurations.keySet()) {
        if ("*".equals(outer_key.the_namespace)) {
          for (tuple_for_key_of_a_config inner_key : the_configurations.keySet()) {
            if (    outer_key.the_MLC_kind == inner_key.the_MLC_kind
                 && outer_key.the_maturity_level_to_which_to_compare == inner_key.the_maturity_level_to_which_to_compare
                 && outer_key.the_key.equalsIgnoreCase(inner_key.the_key)
                 && ! the_configurations.get(outer_key).equals( the_configurations.get(inner_key) )
               ) {
              throw new IOException("Data inconsistency: conflicting for-all-namespaces in configurations: " + outer_key + " mapping to " + the_configurations.get(outer_key) + " conflicts with " + inner_key + " mapping to " + the_configurations.get(inner_key));
            }
          }
        }
      }

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


  public String get_configuration(int maturity_level_of_query, String namespace_of_query, String key_of_query) throws IOException {
    if (null == namespace_of_query || null == key_of_query)
      throw new IOException("Internal program error in “get_configuration”: a param. was null that is not allowed to be null.");

    if (verbosity > 1) {
      System.err.println("\nINFO: maturity_level_of_query=" + maturity_level_of_query + ", namespace_of_query=" + stringize_safely(namespace_of_query) + ", key_of_query=" + stringize_safely(key_of_query) + '\n');
    }

    // for strict-checking mode: enable us to collect _all_ matches, and if there is a multiplicity, check whether or not it`s redundant [i.e. all have the same value] and therefor "stupid but OK"
    ArrayList<tuple_for_key_of_a_config> the_matching_KeyOfConfig_objects = new ArrayList<tuple_for_key_of_a_config>();
    ArrayList<String>                    the_matches                      = new ArrayList<String>();

    for (tuple_for_key_of_a_config the_key_of_the_config : the_configurations.keySet()) {
      switch (the_key_of_the_config.the_MLC_kind) {
        case    less_than_or_equal_to:
          if (! (maturity_level_of_query <= the_key_of_the_config.the_maturity_level_to_which_to_compare))  continue;
          break;
        case                 equal_to:
          if (! (maturity_level_of_query == the_key_of_the_config.the_maturity_level_to_which_to_compare))  continue;
          break;
        case greater_than_or_equal_to:
          if (! (maturity_level_of_query >= the_key_of_the_config.the_maturity_level_to_which_to_compare))  continue;
          break;
        default:
           throw new IOException("Internal program error while trying to compare a query to the maturity level of a config.");
      }

      // OK; at this point, we are supposed to be confident that the maturity level of the query is compatible with the MLC of the current config.

      if (   (namespace_of_query.equalsIgnoreCase(the_key_of_the_config.the_namespace) || "*".equals(the_key_of_the_config.the_namespace))
          && key_of_query.equalsIgnoreCase(the_key_of_the_config.the_key)
         )
      {
        final String the_match = the_configurations.get(the_key_of_the_config).get_as_String_even_if_the_value_is_an_integer();

        if (verbosity > 3) {
          System.err.println("TESTING 22: match if not null: " + stringize_safely(the_match));
        }

        the_matching_KeyOfConfig_objects.add(the_key_of_the_config);
        the_matches                     .add(the_match);
      } // end if
    } // end for

    if (verbosity > 3) {
      System.err.println("TESTING 23: the_matching_KeyOfConfig_objects.size() -> " + the_matching_KeyOfConfig_objects.size() + ", the_matches.size() -> " + the_matches.size());
    }
    // maybe TO DO, but low priority: I _could_ assert here [not using literally "assert", b/c that`s broken/useless in/on Java] that the_matches.size() == the_matching_KeyOfConfig_objects.size()

    // A POSSIBLE LACK OF STRICTNESS, EVEN IN STRICT-CHECKING MODE: since the code below checks for conflicts using the string representations after a _complete_ "unparse" of the relevant internal datum, not only can it not disambiguate between data with different types but the same values [e.g.: a URL with the value "http://example.com/" vs. a _string_ with the value "http://example.com/", a positive integer with the value 1 vs. a nonnegative integer with the value 1], but it _also_ cannot disambiguate between a positive integer with the value 1 and a string with the value "1" [w/o the quotes], and therefor will consider all those "could be viewed as conflicting" scenarios as "OK, just redundant"; perhaps TO DO about this: enable/implement multiple _levels_ of strictness, and if/when e.g. strictness>1 then check for these conflicts [i.e. type conflicts even when the values are either identical or "look the same" (i.e. 1 vs. "1")]

    // System.err.println("\n\033[31mWARNING: " + base_report + "\033[0m");

    // there is no need to enclose the following switch block in "if (strict_checking_mode_enabled)": if/when _not_, "the_matches" should be _empty_, therefor "case 0:  return null;" will be used, i.e. the same outcome as if the switch block _had_ been enclosed in "if (strict_checking_mode_enabled)"
    switch (the_matches.size()) {
      case 0:  return null; // nothing found, so cause the server to "return" a 404 by indicating that a match was not found
      case 1:  return the_matches.get(0); // only 1 match, so no chance that there is a conflict
      default: // the "fun" case
        if (verbosity > 3) {
          System.err.println("TESTING 24: _did_ get to the fun case [in the run-time (AKA query-processing-time)] conflict/redundancy checker/catcher.");
        }
        // search for conflicting _values_, i.e. different results for the same query when compared against different MLC spec.s 

        // AFAIK & IIRC there shouldn`t _be_ any nulls in this, but let`s play it safe all the same and handle the theoretical possibility; a policy decision I made: if _all_ of them are null, this is considered OK _here_, i.e. we are letting "somewhere else" deal with the problem
        final String first_match = the_matches.get(0);
        boolean bad = false; // so I can report a very verbose error dump, rather than just throwing as soon as a/the conflict is found
        if (null == first_match) {

          for (String the_match : the_matches) {
            if (null != the_match) {
              bad = true;
              break;
            }
          } // end for
          if (bad) {
            if (strict_checking_mode_enabled) {
              final String base_report = "Data conflict and/or internal program error: a collection of matches was found to contain at least one null, but not _all_ the matches were null.";
              report_conflicting_match_set_and_throw(base_report, the_matching_KeyOfConfig_objects, the_matches);
              // the preceding line should always throw, so no more execution here
            } else { // non-strict
              // being _super_-nonstrict here: actually going to search for the first non-null, return _that_
              dump_multiple_matches("WARNING: multiple matches, with the first being null and some being non-null; since engine is in non-strict mode, going to return the first non-null match...", the_matching_KeyOfConfig_objects, the_matches, "31");
              for (String the_match : the_matches)  if (null != the_match)  return the_match;
            }
          } // end if bad

        } else { // the first element of "the_matches" is not null, thank the FSM

          for (String the_match : the_matches) {
            if (! first_match.equals(the_match)) {
              bad = true;
              break;
            }
          } // end for
          if (bad) {
            if (strict_checking_mode_enabled) {
              final String base_report = "Data conflict: a collection of matches was found to contain different results, even when ignoring types and after converting integers to strings.";
              report_conflicting_match_set_and_throw(base_report, the_matching_KeyOfConfig_objects, the_matches);
              // the preceding line should always throw, so no more execution here
            } else { // non-strict
              dump_multiple_matches("WARNING: multiple matches, with the first being non-null; since engine is in non-strict mode, going to return the first match...", the_matching_KeyOfConfig_objects, the_matches, "31");
              return first_match;
            }
          } else { // not bad, therefor good  ;-)
            dump_multiple_matches("INFO: multiple matches, but apparently all with the same value [after type erasure and stringification of integers]", the_matching_KeyOfConfig_objects, the_matches, "93");
            return first_match;
          }

        } // end of else that connects to "if (null == first_match)"
      // end of "default:"...  "missing" '}' here is OK, b/c we are inside a switch
    } // end switch

    return null; // this is here just to shut up the stupid Java compiler about "missing return statement": this line should never be reached
  } // end of "get_configuration"


  private void report_conflicting_match_set_and_throw(String                               base_report,
                                                      ArrayList<tuple_for_key_of_a_config> the_matching_KeyOfConfig_objects,
                                                      ArrayList<String>                    the_matches
                                                     ) throws IOException {
    String string_for_exception = base_report + "  Matches:  ";

    System.err.println("\n\033[31mWARNING: " + base_report + "\033[0m");
    System.err.println();
    System.err.println("Matches found");
    System.err.println("-------------");
    for (int index = 0; index < the_matches.size(); ++index) { // intentionally not using a foreach loop, so I can get the matching elements from each ArrayList "in sync"; it would be nice if Java had something like C++`s "pair"
      final String mapping_string = the_matching_KeyOfConfig_objects.get(index).toString() + " ⇢ " + stringize_safely(the_matches.get(index));
      System.err.println("\033[31m" + mapping_string + "\033[0m");
      string_for_exception = string_for_exception + mapping_string;
      if (index < the_matches.size() - 1) { string_for_exception = string_for_exception + "; "; System.err.println(); }
      else                                  string_for_exception = string_for_exception + '.';
    }
    System.err.println("-------------\n");

    // if (strict_checking_mode_enabled)  // why did I originally make the next line conditional???
    throw new IOException(string_for_exception);
  }


  private void dump_multiple_matches(String                               report_title,
                                     ArrayList<tuple_for_key_of_a_config> the_matching_KeyOfConfig_objects,
                                     ArrayList<String>                    the_matches,
                                     String                               ANSI_color_string
                                    ) throws IOException {

    System.err.println("\n\033[" + ANSI_color_string + 'm' + report_title + "\033[0m");
    System.err.println();
    System.err.println("Matches found");
    System.err.println("-------------");
    for (int index = 0; index < the_matches.size(); ++index) { // intentionally not using a foreach loop, so I can get the matching elements from each ArrayList "in sync"; it would be nice if Java had something like C++`s "pair"
      final String mapping_string = the_matching_KeyOfConfig_objects.get(index).toString() + " ⇢ " + stringize_safely(the_matches.get(index));
      System.err.println("\033[" + ANSI_color_string + 'm' + mapping_string + "\033[0m");
      if (index < the_matches.size() - 1)  System.err.println();
    }
    System.err.println("-------------\n");
  }

} // end of class "Configuration_Master_engine"
