import java.io.*;
import java.util.Hashtable;
import java.util.regex.*;
  

public class Configuration_Master_engine {

  private Hashtable<String, Integer> maturityLevel_aliases;

  Configuration_Master_engine(BufferedReader maturityLevel_aliases_input, BufferedReader[] schema_inputs, BufferedReader[] config_inputs) {

    maturityLevel_aliases = new Hashtable<String, Integer>();

    try {

      while (maturityLevel_aliases_input.ready()) {
        String line = maturityLevel_aliases_input.readLine();
        System.err.println("TESTING: maturity-level input line: ''" + line + "''");
        // bad: line = line.substring(line.indexOf('#'));
        line = line.split("#")[0]; // discard comments
        System.err.println("TESTING: maturity level input line after discarding comments: ''" + line + "''");
        line = line.replace(" ", "").toLowerCase(); // this algorithm will result in some "unexpected interpretations" for seemingly-invalid inputs, e.g. "d e v =" is equivalent to "dev=" and "1 2 3 4 5" is equivalent to "12345"
        System.err.println("TESTING: maturity level input line after removing all ASCII spaces and lower-casing: ''" + line + "''");
        // WIP: MatchResult mr1 = Pattern.compile("(\\p{javaLowerCase}+)=(\\d).*"); // allows trailing "garbage"
        if (line.length() > 0) {
          Matcher m1 = Pattern.compile("(\\p{javaLowerCase}+)=(\\d).*").matcher(line); // allows trailing "garbage"
        }



      } // end while maturityLevel_aliases_input.ready()

    } catch (IOException ioe) {

       System.err.println("An I/O exception occurred while trying to initialize the Configuration Master engine: " + ioe);

    }

  }




}
