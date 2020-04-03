import java.io.*;

  

public class Configuration_Master_engine {

  Configuration_Master_engine(BufferedReader maturityLevel_aliases_input, BufferedReader[] schema_inputs, BufferedReader[] config_inputs) {

    try {

      while (maturityLevel_aliases_input.ready()) {
        final String line = maturityLevel_aliases_input.readLine();
        System.err.println("TESTING: maturity level input line: ''" + line + "''");


      } // end while maturityLevel_aliases_input.ready()

    } catch (IOException ioe) {

       System.err.println("An I/O exception occurred while trying to initialize the Configuration Master engine: " + ioe);

    }

  }




}
