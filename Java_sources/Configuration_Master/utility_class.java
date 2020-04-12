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

  public static boolean are_we_running_on_a_POSIX_environment() { // this is best-effort, due to lack of anything standardized in the Java spec to do this AFAIK
    return does_the_first_param_start_with_any_of_the_elements_of_the_second_param
             (
               System.getProperty("os.name"),
               new String[]{
                 // strings stolen from <http://commons.apache.org/proper/commons-lang/javadocs/api-release/src-html/org/apache/commons/lang3/SystemUtils.html>; this code written b/c I didn`t want to introduce an external dependency
                             "AIX", "HP-UX", "Irix", 

                           }
             );
  }

}
