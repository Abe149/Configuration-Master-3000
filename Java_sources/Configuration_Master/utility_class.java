package Configuration_Master;

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

    // "?i" is Java`s way of saying "this regex is case-insensitive"
    // <https://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html#CASE_INSENSITIVE>
    for (String foo : rest)  if (first.matches("?i" + foo))  return true;
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




  public static String sort_lines(String input) {

    // it _should_ be possible to do this in "pure Java", but it`s a headache and a half, and I`m just too sick and tired of Java`s stupidity to write lots of code just to compensate for it




    return ""; // WIP

  }

}
