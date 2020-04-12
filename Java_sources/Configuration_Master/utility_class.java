package Configuration_Master;

public final class utility_class {

  private utility_class() { } // blocked ctor

  public static String stringize_safely(String input) { // TO DO [but low-priority]: pull this out into a separate "library class"
    if (null == input)  return "«null»";
    return "“" + input + "”";
  }

  public static boolean does_the_first_param_include_any_of_the_elements_of_the_second_param_as_a_string_prefix
                        (
                         String first,
                         String[] rest
                        )
  {
    return false; // WIP
  }

  public static boolean are_we_running_on_a_POSIX_environment() { // this is best-effort, due to lack of anything standardized in the Java spec to do this AFAIK
    return does_the_first_param_include_any_of_the_elements_of_the_second_param_as_a_string_prefix
             (
               System.getProperty("os.name"),
               new String[]{} // WIP
             );
  }

}
