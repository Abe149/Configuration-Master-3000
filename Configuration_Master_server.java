package Configuration_Master;

import java.io.*;
import java.net.InetSocketAddress;
import java.lang.*;
import java.net.URL;
import com.sun.net.httpserver.HttpsServer;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import com.sun.net.httpserver.*;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

import java.net.InetAddress;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsExchange;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ArrayBlockingQueue;

import java.util.logging.Logger;
import java.util.ArrayList;

import java.net.URLDecoder;

import static Configuration_Master.utility_class.*;

// "Configuration_Master.keystore", "self-signed"

// https://stackoverflow.com/questions/2308479/simple-java-https-server


public class Configuration_Master_server {

    private final static int default_verbosity = 5;
    private static int verbosity = default_verbosity;

    private final static String data_directory = "data/"; // DRY

    private final static Logger myLogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); // <https://www.vogella.com/tutorials/Logging/article.html>, <https://docs.oracle.com/javase/7/docs/api/java/util/logging/Logger.html>, <https://docs.oracle.com/javase/6/docs/api/java/util/logging/Logger.html>

    public static class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {
            String response = "Hello World from Configuration Master 3000 !!!\n\nProtocol: "
              + he.getProtocol() + "\nHTTP request method: " + he.getRequestMethod() + "\nRequest URI [toASCIIString()]: ''"
              + he.getRequestURI().toASCIIString() + "''\nRequest URI [toString()]: ''" + he.getRequestURI().toString() + "''\n"
              + "Request URI [toString()], decoded: ''" + URLDecoder.decode(he.getRequestURI().toString()) + "''\n";

            // HttpsExchange httpsExchange = (HttpsExchange) he;
            he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            he.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }


    static Configuration_Master_engine the_engine;


    public static class GetHandler implements HttpHandler {
        private static String my_prefix;

        public GetHandler(String prefix_in) {
            my_prefix = prefix_in;
        }

        protected static void http_assert(HttpExchange he, boolean assertion, int status, String desc) throws IOException {
            if (! assertion) {
                final String base_response = "Assertion failed: " + desc;
                final String extended_response = base_response + "; returning HTTP status code " + String.valueOf(status);
                System.err.println("\033[31m" + extended_response + "\033[0m");
                final String extended_response_plus_newline = extended_response + '\n';
                myLogger.warning(extended_response_plus_newline);

                final byte[] extended_response_plus_newline_arrayOfBytes = extended_response_plus_newline.getBytes();
                he.getResponseHeaders().add("content-type", "text/plain; charset=utf-8");
                he.sendResponseHeaders(status, extended_response_plus_newline_arrayOfBytes.length);
                OutputStream os = he.getResponseBody();
                os.write(extended_response_plus_newline_arrayOfBytes);
                os.close();
                throw new IOException();
            }
        }

        @Override
        public void handle(HttpExchange he) throws IOException {
            
            myLogger.info("Protocol: " + he.getProtocol());
            myLogger.info("HTTP request method: " + he.getRequestMethod());
            myLogger.info("Request URI [toASCIIString()]: ''" + he.getRequestURI().toASCIIString() + "''");
            myLogger.info("Request URI [toString()]: ''" + he.getRequestURI().toString() + "''");
            final String decoded_URI = URLDecoder.decode(he.getRequestURI().toString());
            myLogger.info("Request URI [toString()], decoded: ''" + decoded_URI + "''");

            assert decoded_URI.startsWith(my_prefix);
            final String decoded_and_stripped_URI = decoded_URI.substring(my_prefix.length());
            myLogger.info("Request URI [toString()], decoded and prefix-stripped: ''" + decoded_and_stripped_URI + "''");

            final String[] request_components = decoded_and_stripped_URI.split(","); // IMPORTANT string constant
            // myLogger.info("Request components: " + request_components); // useless output...  thanks, Java  :-(

            String maturity_level_string = "", namespace = "", key = "";

            for (String rc : request_components) {
                myLogger.info("Request component: ''" + rc + "''");

                // final String lowered_and_despaced = rc.toLowerCase().replaceAll(" ", ""); // imperfect: we need to be more careful with _which_ spaces we remove

                final String lowered = rc.toLowerCase();
                final String[] split_for_careful_despacing = lowered.split("=", 2); // the 2 here really means "split _once_"; "thanks, Java" [<https://docs.oracle.com/javase/6/docs/api/java/lang/String.html#split(java.lang.String,%20int)>]

                // in principle, I could assert here if the array length of the split isn`t exactly 2 [TO DO?]

                final String lowered_and_despaced = split_for_careful_despacing[0].trim() + '=' + split_for_careful_despacing[1].trim();
                myLogger.info("Request component, lowered and despaced: ''" + lowered_and_despaced + "''");

                // TO DO: clean this up, DRY-wise...  maybe with a[n] [inner?] class-level method, maybe with a lambda

                if (lowered_and_despaced.startsWith("maturity_level=")) {
                    http_assert(he, "".equals(maturity_level_string), 400, "each request must include exactly one maturity level");
                    maturity_level_string = lowered_and_despaced.substring("maturity_level=".length());
                    myLogger.info("Maturity level of request, as a string: ''" + maturity_level_string + "''");
                }

                if (lowered_and_despaced.startsWith("namespace=")) {
                    http_assert(he, "".equals(namespace), 400, "each request must include exactly one namespace");
                    namespace = lowered_and_despaced.substring("namespace=".length());
                    myLogger.info("Namespace of request: ''" + namespace + "''");
                }

                if (lowered_and_despaced.startsWith("key=")) {
                    http_assert(he, "".equals(key), 400, "each request must include exactly one key");
                    key = lowered_and_despaced.substring("key=".length());
                    myLogger.info("Key of request: ''" + key + "''");
                }
            }

            http_assert(he, ! "".equals(maturity_level_string), 400, "each request must include a maturity level");
            http_assert(he, ! "".equals(namespace            ), 400, "each request must include a namespace");
            http_assert(he, ! "".equals(key                  ), 400, "each request must include a key");

            int maturity_level = -1;
            try {
                maturity_level = Integer.parseInt(maturity_level_string);
                http_assert(he, maturity_level >= 0, 400, "maturity levels must not be negative");
            } catch (NumberFormatException nfe) {
                maturity_level = the_engine.get_maturityLevel_integer_from_alias(maturity_level_string);
            }
            http_assert(he, maturity_level >= 0, 400, "internal error while trying to parse maturity level from HTTP input");

            // String response = "Configuration Master 3000 got a seemingly-valid ''get:'' request.\n"; // early-ＷＩＰ code; keeping it here for now "just for the heck of it"

   // WIP:         boolean get_configuration_completed = false; // true here doesn`t mean _succeeded_, just _completed_ [i.e. did not throw/propagate an exception]
            try {
              final String response = the_engine.get_configuration(maturity_level, namespace, key);
          // WIP:    get_configuration_completed = true;
              http_assert(he, response!=null, 404, "the Configuration Master engine did not find a match for the given query of: maturity_level=" + maturity_level + ", namespace=" + stringize_safely(namespace) + ", key=" + stringize_safely(key));

              if (verbosity > 1) {
                System.err.println("INFO: result of query: " + stringize_safely(response) + '\n');
              }

              he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
              he.getResponseHeaders().add("content-type", "text/plain; charset=utf-8");
              he.sendResponseHeaders(200, response.getBytes().length);
              OutputStream os = he.getResponseBody();
              os.write(response.getBytes());
              os.close();
            } catch (IOException ioe) {
              http_assert(he, false, 500, "The Configuration Master engine threw/propagated the following exception: " + ioe);
            }

        }
    }


    private static boolean strict_checking_mode_enabled = false;

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        System.err.println("\n\n"); // to separate "our" output from Ant`s when e.g. running this program via "ant run"

        boolean check_only = false;

        for (String arg : args) {
            arg = arg.replaceFirst("^-*", "").toLowerCase(); // allow e.g. "-help" & "-help" to work just as well as "help" [as a side effect: so do e.g. "---help" & "----------help" ;-)]
            if        ("help".equals(arg) || "h".equals(arg)) {
                System.out.println(
                  "Supported CLI arg.s\n" +
                  "-------------------\n" +
                  "h / help : help, duh.\n" +
                  "\n"+
                  "strict_checking : makes the grammar checking of the engine strict.\n" +
                  "                  It`s probably best to leave this at the default [off] when running the server “for real”.\n" +
                  "\n"+
                  "check_only : _only_ start up the engine, i.e. mainly to run syntax+grammar checking of the data.\n" +
                  "\n"+
                  "v : increase verbosity by 1; hard-coded default is " + default_verbosity + "\n" +
                  "\n"+
                  "verbosity=<integer> : _sets_ the verbosity level, thus overwriting the value that was in effect just prior.\n" +
                  // "\n"+
                  "\n"
                );
                System.exit(0);
            } else if ("strict_checking".equals(arg)) {
                strict_checking_mode_enabled = true;
                if (verbosity > 0) {
                    System.err.println("INFO: activated strict-checking mode, according to CLI arg.; this is probably not something you really want when running the server “for real”.");
                }
            } else if (arg != null && arg.matches("v+")) { // supports not only e.g. "-v" but also e.g. "-vv" and "vvv"
                verbosity += arg.length();
                if (verbosity > 0) {
                    System.err.println("INFO: increased verbosity [by " + arg.length() + "] to " + verbosity + " according to CLI arg.");
                }
            } else if ("check_only"     .equals(arg)) {
                check_only = true;
                if (verbosity > 0) {
                    System.err.println("INFO: check-only mode enabled, in accordance with CLI arg.");
                }
            } else if (arg != null && arg.startsWith("verbosity=")) {
                try {
                    final int new_verbosity = Integer.parseInt(arg.substring("verbosity=".length()));
                    if (verbosity > 0 || new_verbosity > 0) {
                        System.err.println("INFO: setting verbosity to " + new_verbosity + " according to CLI arg.");
                    }
                    verbosity = new_verbosity;
                } catch (NumberFormatException nfe) {
                    // intentionally doing nothing
                }
            }
        }


        if (verbosity > 0) {
            System.err.println("\nINFO: running with a verbosity level of " + verbosity);
        }


        try {
            // set up the engine

            BufferedReader maturityLevel_aliases_input = new BufferedReader(new FileReader(data_directory + "/maturity-level_aliases")); // HARD-CODED
            ArrayList<BufferedReader> config_inputs = new ArrayList<BufferedReader>();
            ArrayList<BufferedReader> schema_inputs = new ArrayList<BufferedReader>();

            // thanks to "jjnguy" at <https://stackoverflow.com/questions/4852531/find-files-in-a-folder-using-java>
            final File[] config_files = new File(data_directory).listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".configurations");
                }
            });
            final File[] schema_files = new File(data_directory).listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".schema");
                }
            });

            if (verbosity > 3) {
                System.err.println();
                for (File config_file : config_files) {
                    System.err.println("DEBUG: config. file found at ''" + config_file + "''");
                }
                for (File schema_file : schema_files) {
                    System.err.println("DEBUG: schema  file found at ''" + schema_file + "''");
                }
                System.err.println();
            }

            for (File config_file : config_files) {
              config_inputs.add(new BufferedReader(new FileReader(config_file)));
            }
            for (File schema_file : schema_files) {
              schema_inputs.add(new BufferedReader(new FileReader(schema_file)));
            }

            if (verbosity > 3) {
                System.err.println();
                System.err.println("DEBUG: # of BufferedReader objects created for config. files: " + config_inputs.size());
                System.err.println("DEBUG: # of BufferedReader objects created for schema  files: " + schema_inputs.size());
                System.err.println();
            }

            BufferedReader[] dummy_for_conversion = new BufferedReader[0];

            the_engine = new Configuration_Master_engine(maturityLevel_aliases_input, schema_inputs.toArray(dummy_for_conversion), config_inputs.toArray(dummy_for_conversion), verbosity, strict_checking_mode_enabled);



            if (check_only) {
                if (verbosity > 0) {
                    System.err.println("INFO: exiting before/instead-of launching the server [since check-only mode is enabled, in accordance with a CLI arg.].");
                }
                System.exit(0);
            }



            // set up the socket address
            InetSocketAddress address = new InetSocketAddress(4430); // IMPORTANT hard-coded value: 4430 instead of 443 on purpose, for non-root use

            // initialise the HTTPS server
            HttpsServer httpsServer = HttpsServer.create(address, 0);
            SSLContext sslContext = SSLContext.getInstance("TLS");

            // initialise the keystore
            final char[] password = "Configuration_Master_3000".toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream(data_directory + "/Configuration_Master.keystore"); // HARD-CODED
            ks.load(fis, password);

            // set up the key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);

            // set up the trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);



            // set up the HTTPS context and parameters
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {
                        // initialise the SSL context
                        SSLContext context = getSSLContext();
                        SSLEngine engine = context.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());

                        // Set the SSL parameters
                        SSLParameters sslParameters = context.getSupportedSSLParameters();
                        params.setSSLParameters(sslParameters);

                    } catch (Exception e) {
                        System.err.println("\033[31mFailed to create HTTPS port... " + e + "\033[0m");
                    }
                }
            });
            httpsServer.createContext("/test", new TestHandler());

            final String get_prefix = "/get:"; // DRY
            httpsServer.createContext(get_prefix, new  GetHandler(get_prefix));

            httpsServer.setExecutor(new ThreadPoolExecutor(4, 8, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100))); // thanks to "rustyx" at <https://stackoverflow.com/questions/2308479/simple-java-https-server>
            myLogger.info("About to start the Configuration Master server...");
            httpsServer.start();

        } catch (Exception exception) {
            System.err.flush();
            System.out.flush();
            System.err.println("\n\033[31mAn exception was caught in the Configuration Master server.  Exception string and stack trace to follow.\033[0m\n");
            System.err.println("Exception as a string: ''" + exception + "''\n");
            System.err.println("Stack trace");
            System.err.println("-----------");
            System.err.flush();
            exception.printStackTrace();
            System.err.println("-----------");
            System.err.flush();

            System.exit(-1);
        }
    }

}
