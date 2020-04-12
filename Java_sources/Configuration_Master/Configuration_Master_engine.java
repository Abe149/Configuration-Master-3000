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

    public String toString_with_inverted_MLC_kind_and_integer() { // for the benefit of "simple_overlappingML_config_finder"
      return " tuple_for_key_of_a_config<the_MLC_kind=\033[7m" + the_MLC_kind + "\033[27m, the_maturity_level_to_which_to_compare=\033[7m" + the_maturity_level_to_which_to_compare + "\033[27m, the_namespace=" + stringize_safely(the_namespace) + ", the_key=" + stringize_safely(the_key) + "> ";
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
    if (line.length() < 1)  return null; // ignore empty-modulo-leading-and/or-trailing ASCII spaces

    line = line.replaceFirst("[#⍝].*", "").trim(); // ‘⍝’: the APL "lamp" symbol for an until-end-of-line comment, AKA "APL FUNCTIONAL SYMBOL UP SHOE JOT"
    // ignore whole-line-possibly-modulo-leading-ASCII-spaces comments
    if (line.length() < 1)  return null;

    final String[] the_split = line.split("␟"); // HARD-CODED: Unicode visible character for ASCII control "character" UNIT SEPARATOR

    // TO DO: make this fail more elegantly when the number of split results is not as expected

    the_namespace = the_split[0].trim().toLowerCase();
    the_key       = the_split[1].trim().toLowerCase();
    the_value_str = the_split[2].trim();

    if ("*".equals(the_key)) {
      throw new IOException("Schema line parse indicates a line with an invalid key of ‘*’: “" + line + "” at " + source);
    }

    if (null == the_namespace || the_namespace.length() < 1 || null == the_key || the_key.length() < 1 || null == the_value_str || the_value_str.length() < 1)  return null;

    return new parsed_line_for_a_schema(new tuple_for_key_of_a_schema(the_namespace, the_key), get_type_by_name_ignoring_case(the_value_str)); // TO DO: make this fail gracefully when the typename "value" is unknown/unrecognized
  } // end of "parse_a_line_for_a_schema"


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
      throw new IOException("Grammatical error: an MLC was effectively comparing to -1, which almost-certainly means that the literal input to the MLC engine was “<0” [module ASCII spaces], which is a nonsensical MLC that will _never_ match _any_ queries since negative numbers are explicitly disallowed in both literal integers in MLCs and in the values of maturity-level aliases; input line, after space trimming and comment removal: «" + line + "»; source: " + source);
    }

    if (null == the_namespace || null == the_key || null == the_value_str)
      throw new IOException("Internal error: in the config. parser, at least one of the 3 String references was null in a place where this should be impossible.  Source: " + source);

    if (the_namespace.length() < 1 || the_key.length() < 1 || the_value_str.length() < 1) {
      throw new IOException("Grammatical error: a configuration which seems to be missing at least one of {namespace, key, value} was found in the line «" + line + "» at " + source);
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
      case      IP_port_number:
        the_value_str = the_value_str.replaceFirst("[^-\\d ].*", "").trim(); // allow trailing "garbage", e.g. a '#'-started comment
        // the preceding line`s regex: r"\D" [_non_-digit] is _not_  usable here, as I allow negative integers in the case of plain "integer"; including ASCII space in the regex [i.e. _not_ stripping the field at its first remaining ASCII space after trimming] in case "parseLong" allows e.g. "- 1" as input that parses to -1: that`s why I need the ".trim()" at the end
        if (the_value_str.length() < 1)
          throw new IOException("Error while type checking; SYNTAX ERROR for an integer or IP port number.  Suspected leading garbage.  Key: " + the_key_of_the_config + "; source: " + source);
        the_value = new config_algebraic_type(Long.parseLong(the_value_str));
        break;

      case          string:
      case nonempty_string:
        the_value_str = the_value_str.replaceFirst("”.*", "”"); // allow trailing "garbage", e.g. a '#'-started comment
        if ('“' != the_value_str.charAt(0) || '”' != the_value_str.charAt(the_value_str.length()-1)) {
          throw new IOException("Error while type checking; SYNTAX ERROR for a string.  Key: " + the_key_of_the_config + "; source: " + source);
        }
        the_value = new config_algebraic_type(the_value_str.replaceFirst("^“", "").replaceFirst("”$", ""));
        break;

      case URL:
        the_value_str = the_value_str.replaceFirst(">.*", ">"); // allow trailing "garbage", e.g. a '#'-started comment
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

      case IP_port_number:
        if (the_value.get_as_long() < 0 || the_value.get_as_long() > 65535) {
          throw new IOException("Error while type checking; for key: " + the_key_of_the_config + " the value was " + the_value + " but the schema said the type was “IP_port_number”.  Source: " + source);
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

    boolean is_internally_a_String() {
      return use_string;
    }

  /* nah, changed my mind: exposes internal details of this class too much
    boolean is_internally_a_long() {
      return ! use_string;
    }
  */
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

  // the next 3 lines: so non-ctor methods will be able to read these values without me needing to pass them around
  private boolean strict_checking_mode_enabled;
  private short   strictness_level;
  private short   verbosity;


  // start of ctor
  Configuration_Master_engine(
                               debugFriendly_buffered_input   maturityLevel_aliases_input,
                               debugFriendly_buffered_input[] schema_inputs,
                               debugFriendly_buffered_input[] config_inputs,
                               short                          verbosity_in,
                               short                          strictness_level___in,
                               boolean                        allow_empty_schema,
                               boolean                        allow_no_configurations
                             )
                             throws IOException {

    verbosity = verbosity_in;
    strictness_level = strictness_level___in;
    strict_checking_mode_enabled = strictness_level > 0; // for "backwards compatibility" with CM3000`s "old" code, i.e. so I don`t need to replace e.g. "if (strict_checking_mode_enabled)" with "if (strictness_level > 0)" all over the place, and similarly in at least one spot where I used "strict_checking_mode_enabled" as the predicate in a ternary-operator expression

    typenames_to_types = new Hashtable<String, value_types>();
    for (value_types VT : value_types.values()) {
      typenames_to_types.put(VT.name(), VT);
    }
    if (verbosity > 0) {
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

      if (verbosity > 0) {
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

          final String source = schema_input.get_description_of_input_and_current_position();
          parsed_line_for_a_schema parse_result = parse_a_line_for_a_schema(line, source);
          if (verbosity > 5) {
            System.err.println("TESTING 14: schema line parse: " + parse_result);
          }

          if (null == parse_result || null == parse_result.key || null == parse_result.key.the_namespace || null == parse_result.key.the_key || null == parse_result.value) {
            if (verbosity > 5) {
              System.err.println("TESTING 15: schema line parse indicates not a line with valid data, e.g. an effectively-blank or all-comment line");
            }
            if (null != parse_result && null != parse_result.key && null != parse_result.key.the_namespace && null != parse_result.key.the_key && null == parse_result.value) {
              throw new IOException("Schema line parse seems to indicate a line with valid key and namespace, but an _invalid_ type value: “" + line + "” at " + source);
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
                throw new IOException("Data inconsistency: conflicting line for schema: ''" + line + "'' at " + source + " conflicts with prior parse result value-type «" + old_VT + '»');
              }
            } else { // if _not_ (the_schema.containsKey(parse_result.key))
              the_schema.put(parse_result.key, parse_result.value);
            }
          }

        } // end while
      } // end for schema_input : schema_inputs

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
          final String source = config_input.get_description_of_input_and_current_position();

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
                  System.err.println("TESTING 21: config. line seems to be valid, but redundant.  " + (strict_checking_mode_enabled ? "The engine should ''crash'' immediately, since it is in strict mode." : "Ignoring, since the engine is in non-strict mode."));
                }
                if (strict_checking_mode_enabled)
                  throw new IOException("Redundant definition found in strict mode in the configuration line «" + line + "»; source: " + source);
              } else {
                throw new IOException("Data inconsistency: conflicting line for configuration: «" + line + "» at " + source + " conflicts with prior parse result value " + old_value);
              }
            } else { // if _not_ (the_schema.containsKey(parse_result.key))
              the_configurations.put(parse_result.key, parse_result.value);
            }

          } // end if null != parse_result

        } // end while
      } // end for config_input : config_inputs


      // --- "asterisk validation" for the configurations     --- //
      // --- performance warning: this is BRUTE FORCE for now --- //

      // this checker is conservative, but not complete; IOW, it cannot find all the relevant conflicts that could possibly exist

      for (tuple_for_key_of_a_config outer_key : the_configurations.keySet()) {
        if ("*".equals(outer_key.the_namespace)) {
          for (tuple_for_key_of_a_config inner_key : the_configurations.keySet()) {
            if (
                    outer_key.the_MLC_kind == inner_key.the_MLC_kind
                 && outer_key.the_maturity_level_to_which_to_compare == inner_key.the_maturity_level_to_which_to_compare
                 && outer_key.the_key.equalsIgnoreCase(inner_key.the_key)
                 && ! the_configurations.get(outer_key).equals( the_configurations.get(inner_key) )
               ) {
              throw new IOException("Data inconsistency: conflicting for-all-namespaces in configurations: " + outer_key + " mapping to " + the_configurations.get(outer_key) + " conflicts with " + inner_key + " mapping to " + the_configurations.get(inner_key));
            }
          }
        }
      }

      if (verbosity > 1) {
        System.err.println();
        System.err.println("INFO: the_configurations: " + the_configurations); // dump the configurations
        System.err.println();
      }

      if (the_schema.size() < 1 && ! allow_empty_schema) {
        System.err.println("\033[31mCowardly refusing to run with an empty schema: without _any_ schema entries, no configurations could possibly pass type checking, so what would be the point?\033[0m");
        System.exit(-1);
      }

      if (the_configurations.size() < 1 && ! allow_no_configurations) {
        System.err.println("\033[31mCowardly refusing to run without any configurations: what would be the point?\033[0m");
        System.exit(-2);
      }

      simple_overlappingML_config_finder(); // simple simulation of queries, to try to find overlaps and [depending on "downstream code", i.e. callees] {{give INFO / throw} for redundant entries [INFO when low strictness level, throw when "high enough"] and {warn/throw for conflicting entries [warn when low strictness level, throw when "high enough"]}

    } catch (IOException ioe) {

       final String response = "An I/O exception occurred while trying to initialize the Configuration Master engine: " + ioe;
       System.err.println("\033[31m" + response + "\033[0m");
       throw new IOException(response); // enabling the following snippet didn`t seem to add anything perceptible: , ioe.getCause());
    } // end of try-catch

  } // end of ctor


  private void simple_overlappingML_config_finder() throws IOException { // I didn`t feel like putting this right in the ctor of the main/primary class of this file, even if it`s only going to be called from one place

    // REMINDER: a null returned from "get_configuration" is OK in some places in this procedure, since a matching config. for the given synthesized ML value may not exist.

    // when strictness is enabled, this procedure can throw/exit upon finding a null returned-from-"get_configuration" result for:
    //   * the preceding value only -- for '≤' only
    //   * the current   value [all three supported MLC types, i.e. '≤', '=', & '≥']
    //   * the next      value only -- for '≥' only

    for (tuple_for_key_of_a_config the_key_of_the_config : the_configurations.keySet()) {
      System.err.println("\033[30;105mINFO: about to check " + the_key_of_the_config.toString_with_inverted_MLC_kind_and_integer() + " in ''simple_overlappingML_config_finder''...\033[0m");

      // these local variables: for convenience and {readability of code}
      final int curr_ML                                 = the_key_of_the_config.the_maturity_level_to_which_to_compare; // "curr": short for "current"
      final String the_namespace                        = the_key_of_the_config.the_namespace;
      final String the_key                              = the_key_of_the_config.the_key;
      final maturityLevel_comparison_types the_MLC_kind = the_key_of_the_config.the_MLC_kind;

      final int pred_ML = curr_ML - 1; // "pred": short for "predecessor"
      final int succ_ML = curr_ML + 1; // "succ": short for "successor"

      if (curr_ML < 0) {
        final String report_without_ANSI_color = "WARNING: an internal ML to compare to was <0, and this should NOT be possible: " + curr_ML;
        System.err.println("\n\033[31m" +                report_without_ANSI_color + "\033[0m");
        if (strictness_level > 0)  throw new IOException(report_without_ANSI_color);
      }

      // in the rest of this procedure: "pretending" that MLs less than zero are supposed to be possible [i.e. valid, not only synthetically -- i.e. the -1 that comes from taking the predecessor of zero -- but also as "real data"], just as a "belt and suspenders" approach, i.e. I don`t want _this_ block of code to crash -- or, worse yet, fail to warn/throw when it _should_ -- just b/c there`s a bug somewhere _else_ in CM3000 [almost certainly in the engine] [regardless of whether the bug was along the lines of "failure to catch invalid input" or some other bug]

      { // an unpredicated inner scope, to limit the scope of "pred_result" [to prevent accidental bugs...  are there any other kind?  ;-)]

        // odd formatting of the next 2 statements: intentionally doing weird things with line breaks and spacing so as to make the e.g. "pred" in "pred_result" & "pred_ML" to line up vertically
        System.err.println("\033[35mINFO: about to check " + the_key_of_the_config + " \033[30;105musing ML = " + // ...
        /* ... */    pred_ML + "\033[0;35m in ''simple_overlappingML_config_finder''...\033[0m");
        final String pred_result = get_configuration( // ...
        /* ... */    pred_ML,                        the_namespace, the_key);

        if (pred_ML < 0) { // when testing negative MLs, null in the result is a _good_ thing
          if (null == pred_result) {
            if (verbosity > 0)  System.err.println("\033[32mINFO: the result for ML=" + pred_ML + " was null, as expected because the ML was negative.\033[0m");
          } else { // not null
            final String report_without_ANSI_color = "WARNING: the result for ML=" + pred_ML + " was _not_ null, and it was expected to be null because the ML was negative.";
            if (verbosity        > 0)  System.err.println("\033[31m" + report_without_ANSI_color + "\033[0m");
            if (strictness_level > 0)  throw new IOException(          report_without_ANSI_color);
          }
        } else if (maturityLevel_comparison_types.less_than_or_equal_to == the_MLC_kind) { // ML ≥ 0, so now null in the result is _bad_ if/when the MLC kind is less_than_or_equal_to
          if (null == pred_result) { // while checking a non-negative "pred_ML" -- since the predent code of CM3000 only internally supports the MLC specifiers '≤', '=', and '≥' -- we can assume that we should have at least one match, and therefor a non-null result
            final String report_without_ANSI_color = "WARNING: the result for ML=" + pred_ML + " was null, and it was _not_ expected to be null.";
            if (verbosity        > 0)  System.err.println("\033[31m" + report_without_ANSI_color + "\033[0m");
            if (strictness_level > 0)  throw new IOException(          report_without_ANSI_color);
          } else { // not null
            if (verbosity > 0)  System.err.println("\033[32mINFO: the result for ML=" + pred_ML + " was non-null, as expected.\033[0m");
          }
        } else { // the ML is not negative, but the MLC kind is _not_ less_than_or_equal_to so we cannot make any assumptions about the goodness/badness of null results
          if (verbosity > 0)  System.err.println("INFO: the result for ML=" + pred_ML + " was " + (null == pred_result ? "" : "non-") + "null [no expectation involved].");
        } // end if

      } // end of unpredicated inner scope


      { // an unpredicated inner scope, to limit the scope of "curr_result" [to prevent accidental bugs...  are there any other kind?  ;-)]

        // odd formatting of the next 2 statements: intentionally doing weird things with line breaks and spacing so as to make the e.g. "pred" in "pred_result" & "pred_ML" to line up vertically
        System.err.println("\033[35mINFO: about to check " + the_key_of_the_config + " \033[30;105musing ML = " + // ...
        /* ... */    curr_ML + "\033[0;35m in ''simple_overlappingML_config_finder''...\033[0m");
        final String curr_result = get_configuration( // ...
        /* ... */    curr_ML,                        the_namespace, the_key);

        if (curr_ML < 0) { // when testing negative MLs, null in the result is a _good_ thing
          if (null == curr_result) {
            if (verbosity > 0)  System.err.println("\033[32mINFO: the result for ML=" + curr_ML + " was null, as expected because the ML was negative.\033[0m");
          } else { // not null
            final String report_without_ANSI_color = "WARNING: the result for ML=" + curr_ML + " was _not_ null, and it was expected to be null because the ML was negative.";
            if (verbosity        > 0)  System.err.println("\033[31m" + report_without_ANSI_color + "\033[0m");
            if (strictness_level > 0)  throw new IOException(          report_without_ANSI_color);
          }
        } else {           // ML ≥ 0, so now null in the result is _bad_
          if (null == curr_result) { // while checking a non-negative "curr_ML" -- since the current code of CM3000 only internally supports the MLC specifiers '≤', '=', and '≥' -- we can assume that we should have at least one match, and therefor a non-null result
            final String report_without_ANSI_color = "WARNING: the result for ML=" + curr_ML + " was null, and it was _not_ expected to be null.";
            if (verbosity        > 0)  System.err.println("\033[31m" + report_without_ANSI_color + "\033[0m");
            if (strictness_level > 0)  throw new IOException(          report_without_ANSI_color);
          } else { // not null
            if (verbosity > 0)  System.err.println("\033[32mINFO: the result for ML=" + curr_ML + " was non-null, as expected.\033[0m");
          }
        } // end if
      } // end of unpredicated inner scope


      // odd formatting of the next 2 statements: intentionally doing weird things with line breaks and spacing so as to make the e.g. "pred" in "pred_result" & "pred_ML" to line up vertically
      System.err.println("\033[35mINFO: about to check " + the_key_of_the_config + " \033[30;105musing ML = " + // ...
      /* ... */    succ_ML + "\033[0;35m in ''simple_overlappingML_config_finder''...\033[0m");
      final String succ_result = get_configuration( // ...
      /* ... */    succ_ML,                        the_namespace, the_key);

      if (succ_ML < 0) { // when testing negative MLs, null in the result is a _good_ thing
        if (null == succ_result) {
          if (verbosity > 0)  System.err.println("\033[32mINFO: the result for ML=" + succ_ML + " was null, as expected because the ML was negative.\033[0m");
        } else { // not null
          final String report_without_ANSI_color = "WARNING: the result for ML=" + succ_ML + " was _not_ null, and it was expected to be null because the ML was negative.";
          if (verbosity        > 0)  System.err.println("\033[31m" + report_without_ANSI_color + "\033[0m");
          if (strictness_level > 0)  throw new IOException(          report_without_ANSI_color);
        }
      } else if (maturityLevel_comparison_types.greater_than_or_equal_to == the_MLC_kind) { // ML ≥ 0, so now null in the result is _bad_ if/when the MLC kind is greater_than_or_equal_to
        if (null == succ_result) { // while checking a non-negative "succ_ML" -- since the succent code of CM3000 only internally supports the MLC specifiers '≤', '=', and '≥' -- we can assume that we should have at least one match, and therefor a non-null result
          final String report_without_ANSI_color = "WARNING: the result for ML=" + succ_ML + " was null, and it was _not_ expected to be null.";
          if (verbosity        > 0)  System.err.println("\033[31m" + report_without_ANSI_color + "\033[0m");
          if (strictness_level > 0)  throw new IOException(          report_without_ANSI_color);
        } else { // not null
          if (verbosity > 0)  System.err.println("\033[32mINFO: the result for ML=" + succ_ML + " was non-null, as expected.\033[0m");
        }
      } else { // the ML is not negative, but the MLC kind is _not_ greater_than_or_equal_to so we cannot make any assumptions about the goodness/badness of null results
        if (verbosity > 0)  System.err.println("INFO: the result for ML=" + succ_ML + " was " + (null == succ_result ? "" : "non-") + "null [no expectation involved].");
      } // end if

    } // end for
  } // end of "simple_overlappingML_config_finder"


  // working around the fact that Java doesn`t have defaults for param.s
  public String get_configuration(int maturity_level_of_query, String namespace_of_query, String key_of_query) throws IOException {
    return get_configuration(maturity_level_of_query, namespace_of_query, key_of_query, false);
  }

  public String get_configuration(int maturity_level_of_query, String namespace_of_query, String key_of_query, boolean the_query_is_synthetic___off_AKA_false_by_default) throws IOException {
    // REMINDER: do NOT throw just b/c maturity_level_of_query<0, no _matter_ what the strictness level is, since the call with maturity_level_of_query<0 may come from a pseudo-query generated/executed by "simple_overlappingML_config_finder"

    if (null == namespace_of_query || null == key_of_query)
      throw new IOException("Internal program error in “get_configuration”: a param. was null that is not allowed to be null.");

    if (verbosity > 1) {
      System.err.println("\nINFO: maturity_level_of_query=" + maturity_level_of_query + ", namespace_of_query=" + stringize_safely(namespace_of_query) + ", key_of_query=" + stringize_safely(key_of_query) + '\n');
    }

    // collect _all_ matches, and if there is a multiplicity, check whether or not it`s redundant [i.e. all have the same value] and therefor "stupid but OK" in non-strict and only-statically-strict modes [i.e. strictness_level values of 0 and 1]
    ArrayList<tuple_for_key_of_a_config> the_matching_KeyOfConfig_objects = new ArrayList<tuple_for_key_of_a_config>();
    ArrayList<config_algebraic_type>     the_matches                      = new ArrayList<config_algebraic_type>();

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
        final config_algebraic_type the_match = the_configurations.get(the_key_of_the_config);

        if (verbosity > 3) {
          System.err.println("TESTING 22: match if not null: " +                  the_match );
        }

        if (null == the_match) // save some headaches later by omitting nulls, which probably shouldn`t be in the hashtable anyway
          System.err.println("\n\033[31mWARNING: a null was found in a value of ''the_configurations''; this is not expected to be possible."); // would it be good to also throw/exit here, at least if the strictness level is high enough?
        else {
          the_matching_KeyOfConfig_objects.add(the_key_of_the_config);
          the_matches                     .add(the_match);
        }
      } // end if
    } // end for

    if (verbosity > 3) {
      System.err.println("TESTING 23: the_matching_KeyOfConfig_objects.size() -> " + the_matching_KeyOfConfig_objects.size() + ", the_matches.size() -> " + the_matches.size());
    }
    // maybe TO DO, but low priority: I _could_ assert here [not using literally "assert", b/c that`s broken/useless in/on Java] that the_matches.size() == the_matching_KeyOfConfig_objects.size()

    // maybe TO DO, maybe not since maybe it`s "impossible" [since type checking should have caught the conflict before the relevant data got into the hashtable, since any given <namespace, key> pair has the same CM3000 datatype across _all_ MLs: disambiguate between data with different types but the same values [e.g.: a URL with the value "http://example.com/" vs. a _string_ with the value "http://example.com/"

    // System.err.println("\n\033[31mWARNING: " + base_report + "\033[0m");

    // reminder to self: do _not_ enclose the following switch block in "if (strict_checking_mode_enabled)" or similar
    switch (the_matches.size()) {
      case 0:  return null; // nothing found, so cause the server to "return" a 404 by indicating that a match was not found
      case 1:  return the_matches.get(0).get_as_String_even_if_the_value_is_an_integer(); // only 1 match, so no chance that there is a conflict
      default: // the "fun" case
        if (verbosity > 3) {
          System.err.println("TESTING 24: _did_ get to the fun case [in the run-time (AKA query-processing-time)] conflict/redundancy checker/catcher.");
        }

        if (strictness_level >= 3) { // _EXTREMELY_ strict: no multiple matches allowed!
          report_match_set_and_throw("Unacceptable when the strictness level is > 2: multiple matches for a single query", the_matching_KeyOfConfig_objects, the_matches);
          // the preceding line should always throw, so no more execution here
          System.exit(-6);
        }

        // search for conflicting _values_, i.e. different results for the same query when compared against different MLC spec.s //

        final config_algebraic_type first_match = the_matches.get(0);
        boolean all_the_same = true; // so I can report a very verbose error dump, rather than just throwing as soon as a/the conflict is found
        if (null == first_match) {
          System.err.println("\033[31mFATAL INTERNAL ERROR\033[0m");
          System.exit(-5);
        }

        for (config_algebraic_type the_match : the_matches) {
          if (! first_match.equals(the_match)) {
            all_the_same = false;
            break;
          }
        } // end for
        if (all_the_same) {
          if (verbosity > 5)  dump_multiple_matches("INFO: multiple matches, but apparently all with the same value [after type erasure of CM3000 types]", the_matching_KeyOfConfig_objects, the_matches, "93");
          return first_match.get_as_String_even_if_the_value_is_an_integer();
        } else { // not all the same, therefor bad  :-(
          if (strictness_level >= 2) {
            report_match_set_and_throw("Data conflict: a collection of matches was found to contain different results, even when ignoring CM3000 types.", the_matching_KeyOfConfig_objects, the_matches);
            // the preceding line should always throw, so no more execution here
            System.exit(-4);
          } else { // non-strict
            if (verbosity > 5)  dump_multiple_matches("WARNING: multiple matches; since engine has strictness_level < 2, going to return the first match...", the_matching_KeyOfConfig_objects, the_matches, "31");
            return first_match.get_as_String_even_if_the_value_is_an_integer();
          }
        }

      // end of "default:"...  "missing" '}' here is OK, b/c we are inside a switch
    } // end switch

    return null; // this is here just to shut up the stupid Java compiler about "missing return statement": this line should never be reached
  } // end of "get_configuration"


  private void report_match_set_and_throw(String                               base_report,
                                          ArrayList<tuple_for_key_of_a_config> the_matching_KeyOfConfig_objects,
                                          ArrayList<config_algebraic_type>     the_matches
                                         ) throws IOException {
    String string_for_exception = base_report + "  Matches:  ";

    System.err.println("\n\033[31mWARNING: " + base_report + "\033[0m");
    System.err.println();
    System.err.println("Matches found");
    System.err.println("-------------");
    for (int index = 0; index < the_matches.size(); ++index) { // intentionally not using a foreach loop, so I can get the matching elements from each ArrayList "in sync"; it would be nice if Java had something like C++`s "pair"
      final config_algebraic_type the_match = the_matches.get(index);
      final boolean the_match_is_integral = ! the_match.is_internally_a_String();
      final String mapping_string = the_matching_KeyOfConfig_objects.get(index).toString() + " ⇢ { " + the_matches.get(index) + " [i.e. the " + (the_match_is_integral ? "integer " : "string “") + the_match.get_as_String_even_if_the_value_is_an_integer() + (the_match_is_integral ? "" : "”") + "] }";
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
                                     ArrayList<config_algebraic_type>     the_matches,
                                     String                               ANSI_color_string
                                    ) throws IOException {

    System.err.println("\n\033[" + ANSI_color_string + 'm' + report_title + "\033[0m");
    System.err.println();
    System.err.println("Matches found");
    System.err.println("-------------");
    for (int index = 0; index < the_matches.size(); ++index) { // intentionally not using a foreach loop, so I can get the matching elements from each ArrayList "in sync"; it would be nice if Java had something like C++`s "pair"
      final config_algebraic_type the_match = the_matches.get(index);
      final boolean the_match_is_integral = ! the_match.is_internally_a_String();
      final String mapping_string = the_matching_KeyOfConfig_objects.get(index).toString() + " ⇢ { " + the_matches.get(index) + " [i.e. the " + (the_match_is_integral ? "integer " : "string “") + the_match.get_as_String_even_if_the_value_is_an_integer() + (the_match_is_integral ? "" : "”") + "] }";
      System.err.println("\033[" + ANSI_color_string + 'm' + mapping_string + "\033[0m");
      if (index < the_matches.size() - 1)  System.err.println();
    }
    System.err.println("-------------\n");
  }

} // end of class "Configuration_Master_engine"
