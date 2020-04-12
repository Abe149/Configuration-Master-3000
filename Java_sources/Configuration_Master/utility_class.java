package Configuration_Master;

import java.io.*;

public final class utility_class {

  private utility_class() { } // blocked ctor

  public static String stringize_safely(String input) { // TO DO [but low-priority]: pull this out into a separate "library class"
    if (null == input)  return "«null»";
    return "“" + input + "”";
  }

  public static boolean does_the_first_param_start_with_any_of_the_elements_of_the_second_param(String first, String[] rest) {
    // allowing this to crash if "first" is null
    for (String foo : rest)  if (first.startsWith(foo))  return true;
    return false;
  }

  public static boolean does_the_first_param_start_with_any_of_the_elements_of_the_second_param___ignore_case // ...
    /* ... */             (String first, String[] rest) {
    // allowing this to crash if "first" is null
    for (String foo : rest)  if (first.toLowerCase().startsWith(foo.toLowerCase()))  return true; // slow and stupid
    return false;
  }


  public static boolean does_the_first_param_match_any_of_the_elements_of_the_second_param___ignore_case // ...
    /* ... */             (String first, String[] rest) {
    // allowing this to crash if "first" is null

    // "(?i)" is Java`s way of saying "this regex is case-insensitive"
    // <https://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html#CASE_INSENSITIVE>
    for (String foo : rest)  if (first.matches("(?i)" + foo))  return true;
    return false;
  }


  public static boolean are_we_running_on_a_POSIX_environment() { // this is best-effort, due to lack of anything standardized in the Java spec to do this AFAIK
    return does_the_first_param_match_any_of_the_elements_of_the_second_param___ignore_case // ...
             (
               System.getProperty("os.name"),
               new String[]{
                 // strings stolen from <http://commons.apache.org/proper/commons-lang/javadocs/api-release/src-html/org/apache/commons/lang3/SystemUtils.html>; this code written b/c I didn`t want to introduce an external dependency
                             "AIX", "HP-UX", "Irix",
                             "Linux", // the disrespectful morons at Sun didn`t call this by its true name, i.e. "GNU/Linux"
                             "Mac OS X",
                             "macOS", // Apple`s new-ish marketing moniker for Mac OS X
                             "FreeBSD",
                             "OpenBSD",
                             "NetBSD",
                             "Dragon.*fly.*BSD", // IMO it should be "Dragonfly BSD", but IIRC the guy responsible spells it with a capital 'F'
                             "Solaris",
                             "SunOS" // should be deader than a doornail, but was supported by the Java team at Sun for obvious reasons
                             // maybe TO DO: are there any _other_ POSIX-ish OSes that can run Java code?

                             // maybe TO DO: support Cygwin?

                             // an embedded assumption here: most Interix-like things on WinDOS can NOT run Java code
                           }
             );
  }

  public static String read_all_lines_from_a_BufferedReader(BufferedReader in) throws IOException { // there ought to be a better way to do "read _all_ the input" in one call
    if (null == in)  return null; // would it be better to throw?
    String temp = "";

    // the next block of code: allowing "IOException" exceptions to escape
    while (in.ready()) {
      temp = temp + in.readLine() + '\n';
    }
    in.close();

    return temp;
  }


  public static String pipe_first_param_through_POSIX_command_in_second_param(String input, String cmd) throws IOException {
    // in Java`s "Process", input is output and output is input  :-P
    // https://docs.oracle.com/javase/6/docs/api/java/lang/Process.html#getOutputStream()

    if (null == input || null == cmd)               return null; // would it be better to throw?
    if (! are_we_running_on_a_POSIX_environment())  return null; // would it be better to throw?

    // the rest of this function: allowing "IOException" exceptions to escape

    final Process my_Process = Runtime.getRuntime().exec(cmd);
//  final Process my_Process = Runtime.getRuntime().exec(cmd.split(" "));
//  final Process my_Process = new ProcessBuilder(cmd.split(" ")).start();
 // -- NOT to uncomment: final Process my_Process = new ProcessBuilder(cmd).start(); // no good: barfs on param.s, e.g. the " -a" in "uname -a"

    final OutputStream my_OutputStream = my_Process.getOutputStream();
    final InputStream  my_InputStream  = my_Process.getInputStream();
    my_OutputStream.write(input.getBytes());
    my_OutputStream.flush();
    my_OutputStream.close();

    boolean done = false;
    do {
      try {
        my_Process.waitFor(); // potentially throws InterruptedException, so you gotta do this in a loop  :-(
        done = true;
      } catch (InterruptedException ie) {
      }
    } while (! done);


    return read_all_lines_from_a_BufferedReader(new BufferedReader(new InputStreamReader(new BufferedInputStream(my_InputStream)))); // GOD how I hate Java I/O
  }


  public static String sort_lines(String input) throws IOException {
    // it _should_ be possible to do this in "pure Java", but it`s a headache and a half, and I`m just too sick and tired of Java`s stupidity to write lots of code just to compensate for it
    return pipe_first_param_through_POSIX_command_in_second_param(input, "sort");
  }

  public static void main(String[] args) throws IOException { // for testing

    System.out.println("\nTEST 1");
    System.out.println(pipe_first_param_through_POSIX_command_in_second_param("",      "uname -a"));

    System.out.println("\nTEST 2");
    System.out.println(pipe_first_param_through_POSIX_command_in_second_param("", "/bin/uname")); // 

    System.out.println("\nTEST 3");
    System.out.println(pipe_first_param_through_POSIX_command_in_second_param("",      "uname"));

    System.out.println("\nTEST 4");
    System.out.println(pipe_first_param_through_POSIX_command_in_second_param("", "/bin/uname"));

//  return pipe_first_param_through_POSIX_command_in_second_param(input, "ls -l /");
//  return pipe_first_param_through_POSIX_command_in_second_param(input, "/bin/ls -l /");

    System.out.println("\nTEST 5");
    System.out.println("Testing ''sort_lines''...\n\nTest case\n---------");
    final String sort_test_case = "Z\nB\nC\n";
    System.out.println(sort_test_case);
    System.out.println("Test result\n-----------");
    System.out.println(sort_lines(sort_test_case));

    System.out.println("\nTEST 6");
    System.out.println("Testing ''ls -l /''...\n");
    System.out.println("Test result\n-----------");
    System.out.println(pipe_first_param_through_POSIX_command_in_second_param("", "ls -l /"));

    System.out.println("\nTEST 7");
    System.out.println("Testing ''ls -l /'' and ''sort_lines''...\n");
    System.out.println("Test result at the end of the chain\n-----------");
    System.out.println(sort_lines(pipe_first_param_through_POSIX_command_in_second_param("", "ls -l /")));


  }
}
