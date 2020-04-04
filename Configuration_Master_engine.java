import java.io.*;
import java.util.Hashtable;
import java.util.regex.*;
  

public class Configuration_Master_engine {

  private Hashtable<String, Integer> maturityLevel_aliases;

  Configuration_Master_engine(BufferedReader maturityLevel_aliases_input, BufferedReader[] schema_inputs, BufferedReader[] config_inputs, int verbosity) {

    maturityLevel_aliases = new Hashtable<String, Integer>();

    try {

      while (maturityLevel_aliases_input.ready()) {
        String line = maturityLevel_aliases_input.readLine();
        if (verbosity > 5) {
          System.err.println("TESTING: maturity-level input line: ''" + line + "''");
        }
        line = line.split("#")[0]; // discard comments
        if (verbosity > 5) {
          System.err.println("TESTING: maturity level input line after discarding comments: ''" + line + "''");
        }
        line = line.replace(" ", "").toLowerCase(); // this algorithm will result in some "unexpected interpretations" for seemingly-invalid inputs, e.g. "d e v =" is equivalent to "dev=" and "1 2 3 4 5" is equivalent to "12345"
        if (verbosity > 5) {
          System.err.println("TESTING: maturity level input line after removing all ASCII spaces and lower-casing: ''" + line + "''");
        }
        if (line.length() > 0) {
          Matcher m1 = Pattern.compile("(\\p{javaLowerCase}+)=(\\d+).*").matcher(line); // allows trailing "garbage"
          if (verbosity > 5) {
            System.err.println("TESTING 1: m1: " + m1);
            System.err.println("TESTING 2: m1.groupCount() -> " + m1.groupCount());
          }


          if (verbosity > 5) {
            System.err.println("TESTING 3: m1: " + m1);
          }
          m1.find(); // CRUCIAL
          if (verbosity > 5) {
            System.err.println("TESTING 4: m1: " + m1);
          }

          MatchResult mr1 = m1.toMatchResult();
          if (verbosity > 5) {
            System.err.println("TESTING 5: mr1: " + mr1);
            System.err.println("TESTING 6: mr1.groupCount() -> " + mr1.groupCount());
          }

          if (mr1.groupCount() != 2) {
            throw new IOException("Wrong number of groups in results for maturity-level input line micro-parser: expected 2, got " + String.valueOf(mr1.groupCount()));
          }

          final String            alias = m1.group(1);
          if (verbosity > 4) {
            System.err.println("TESTING: alias=''" + alias + "''");
          }
          final String number_as_string = m1.group(2);
          final int number = Integer.parseInt(number_as_string);
          if (verbosity > 4) {
            System.err.println("TESTING: number: " + number);
            System.err.println();
          }

          maturityLevel_aliases.put(alias, number);
        } // if line.length() > 0

      } // end while maturityLevel_aliases_input.ready()

      if (verbosity > 1) {
        System.err.println("INFO: maturityLevel_aliases: " + maturityLevel_aliases);
        System.err.println();
      }

    } catch (IOException ioe) {

       System.err.println("An I/O exception occurred while trying to initialize the Configuration Master engine: " + ioe);

    }

  }




}
