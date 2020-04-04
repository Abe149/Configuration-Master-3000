import java.io.*;
import java.util.Hashtable;
import java.util.regex.*;
  

public class Configuration_Master_engine {

  private Hashtable<String, Integer> maturityLevel_aliases;

  private int get_maturityLevel_integer_from_alias(String alias_in) {
    return maturityLevel_aliases.get(alias_in);
  }

  private class tuple_for_key_of_a_schema_or_config {
    public maturityLevel_comparison_types the_MLC;
    public int                            the_maturity_level_to_which_to_compare;
    public String                         the_namespace;
    public String                         the_key; // confusing, innit?  ;-)

    tuple_for_key_of_a_schema_or_config(maturityLevel_comparison_types MLC_in, int maturity_level_in, String namespace_in, String key_in) { // ctor
      the_MLC                                = MLC_in;
      the_maturity_level_to_which_to_compare = maturity_level_in;
      the_namespace                          = namespace_in;
      the_key                                = key_in;
    }

    public String toString() { // for debugging etc.
      return " tuple_for_key_of_a_schema_or_config<the_MLC=" + the_MLC + ", the_maturity_level_to_which_to_compare=" + the_maturity_level_to_which_to_compare + ", the_namespace=''" + the_namespace + "'', the_key=''" + the_key + "''> ";
    }
  }

  private class semiParsed_line_for_a_schema_or_config___values_are_all_Strings {
    public tuple_for_key_of_a_schema_or_config key;
    public String                              value;
    semiParsed_line_for_a_schema_or_config___values_are_all_Strings(tuple_for_key_of_a_schema_or_config key_in, String value_in) {
      key = key_in;
      value = value_in;
    }
  }


  private Hashtable<String                             , value_types> typenames_to_types; // unfortunately, initializing a hashtable in Java is a {can of worms / Pandora`s box}, so we`ll do it the old-fashioned way

  private Hashtable<tuple_for_key_of_a_schema_or_config, value_types> the_schema;


  semiParsed_line_for_a_schema_or_config___values_are_all_Strings parse_a_line_for_a_schema_or_config(String line) {
    maturityLevel_comparison_types the_MLC = maturityLevel_comparison_types.equal_to;
    int                            the_maturity_level_to_which_to_compare = -1;
    String                         the_namespace = null;
    String                         the_key       = null;
    String                         the_value     = null;

    // ... WIP ... //

    return new semiParsed_line_for_a_schema_or_config___values_are_all_Strings(new tuple_for_key_of_a_schema_or_config(the_MLC, the_maturity_level_to_which_to_compare, the_namespace, the_key), the_value);
  }


  Configuration_Master_engine(BufferedReader maturityLevel_aliases_input, BufferedReader[] schema_inputs, BufferedReader[] config_inputs, int verbosity) throws IOException { // start of ctor

    // there should be a more-elegant way to do this...
    typenames_to_types = new Hashtable<String, value_types>();
    typenames_to_types.put("integer"            , value_types.integer);
    typenames_to_types.put("nonnegative_integer", value_types.nonnegative_integer);
    typenames_to_types.put("positive_integer"   , value_types.positive_integer);
    typenames_to_types.put("nonempty_string"    , value_types.nonempty_string);
    typenames_to_types.put("string"             , value_types.string);
    typenames_to_types.put("URL"                , value_types.URL);

    maturityLevel_aliases = new Hashtable<String, Integer>();

    try {

      while (maturityLevel_aliases_input.ready()) {
        String line = maturityLevel_aliases_input.readLine();
        if (verbosity > 5) {
          System.err.println("TESTING 1: maturity-level aliases input line: ''" + line + "''");
        }
        line = line.split("#")[0]; // discard comments
        if (verbosity > 5) {
          System.err.println("TESTING 2: maturity level aliases input line after discarding comments: ''" + line + "''");
        }
        line = line.replace(" ", "").toLowerCase(); // this algorithm will result in some "unexpected interpretations" for seemingly-invalid inputs, e.g. "d e v =" is equivalent to "dev=" and "1 2 3 4 5" is equivalent to "12345"
        if (verbosity > 5) {
          System.err.println("TESTING 3: maturity level aliases input line after removing all ASCII spaces and lower-casing: ''" + line + "''");
        }
        if (line.length() > 0) {
          Matcher m1 = Pattern.compile("(\\p{javaLowerCase}+)=(\\d+).*").matcher(line); // allows trailing "garbage"
          if (verbosity > 5) {
            System.err.println("TESTING 4: m1: " + m1);
            System.err.println("TESTING 5: m1.groupCount() -> " + m1.groupCount());
          }

          if (verbosity > 5) {
            System.err.println("TESTING 6: m1: " + m1);
          }
          final boolean line_matched_the_regex = m1.find(); // CRUCIAL
          if (verbosity > 5) {
            System.err.println("TESTING 7: line_matched_the_regex: " + line_matched_the_regex);
          }

          if (line_matched_the_regex) {

            if (verbosity > 5) {
              System.err.println("TESTING 8: m1: " + m1);
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
        System.err.println("INFO: maturityLevel_aliases: " + maturityLevel_aliases);
        System.err.println();
      }

      the_schema = new Hashtable<tuple_for_key_of_a_schema_or_config, value_types>();
      for (BufferedReader schema_input : schema_inputs) {
        while (schema_input.ready()) {
          String line = schema_input.readLine();
          if (verbosity > 1) {
            System.err.println("TESTING 13: schema input line: ''" + line + "''");

            semiParsed_line_for_a_schema_or_config___values_are_all_Strings semiparse_result = parse_a_line_for_a_schema_or_config(line);
            System.err.println("TESTING 14: schema line partial parse: " + semiparse_result);

          }
        }
      }

    } catch (IOException ioe) {

       final String response = "An I/O exception occurred while trying to initialize the Configuration Master engine: " + ioe;
       System.err.println("\033[31m" + response + "\033[0m");
       throw new IOException(response); // enabling the following snippet didn`t seem to add anything perceptible: , ioe.getCause());
    } // end of try-catch

  } // end of ctor




} // end of class "Configuration_Master_engine"
