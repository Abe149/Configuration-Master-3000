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

// "Configuration_Master.keystore", "self-signed"

// https://stackoverflow.com/questions/2308479/simple-java-https-server


public class Configuration_Master_server {

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


    public static class GetHandler implements HttpHandler {
        private static String my_prefix;

        public GetHandler(String prefix_in) {
            my_prefix = prefix_in;
        }

        protected static void http_assert(HttpExchange he, boolean assertion, int status, String desc) throws IOException {
            if (! assertion) {
                final String base_response = "Assertion failed: " + desc;
                final String extended_response = base_response + "; returning HTTP status code " + String.valueOf(status);
                System.out.println("\033[31m" + extended_response + "\033[0m");
                final String extended_response_plus_newline = extended_response + '\n';
                myLogger.warning(extended_response_plus_newline);

                final byte[] extended_response_plus_newline_arrayOfBytes = extended_response_plus_newline.getBytes();
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
                final String lowered_and_despaced = rc.toLowerCase().replaceAll(" ", "");
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



            String response = "Configuration Master 3000 got a seemingly-valid ''get:'' request.\n";



            // HttpsExchange httpsExchange = (HttpsExchange) he;
            he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            he.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {

        try {
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



            // set up the engine

            BufferedReader maturityLevel_aliases_input = new BufferedReader(new FileReader(data_directory + "/maturity-level_aliases")); // HARD-CODED
            ArrayList<BufferedReader> schema_inputs = new ArrayList<BufferedReader>();
            ArrayList<BufferedReader> config_inputs = new ArrayList<BufferedReader>();

            BufferedReader[] dummy_for_conversion = new BufferedReader[0];

            Configuration_Master_engine my_engine = new Configuration_Master_engine(maturityLevel_aliases_input, schema_inputs.toArray(dummy_for_conversion), config_inputs.toArray(dummy_for_conversion));


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

                    } catch (Exception ex) {
                        System.out.println("Failed to create HTTPS port");
                    }
                }
            });
            httpsServer.createContext("/test", new TestHandler());

            final String get_prefix = "/get:"; // DRY
            httpsServer.createContext(get_prefix, new  GetHandler(get_prefix));

            httpsServer.setExecutor(new ThreadPoolExecutor(4, 8, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100))); // thanks to "rustyx" at <https://stackoverflow.com/questions/2308479/simple-java-https-server>
            httpsServer.start();

        } catch (Exception exception) {
            System.out.println("\033[31mAn exception was caught in the Configuration Master server.  Stack trace to follow.\033[0m");
            exception.printStackTrace();

        }
    }

}
