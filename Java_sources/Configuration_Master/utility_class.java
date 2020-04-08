package Configuration_Master;

public final class utility_class {

  public static String stringize_safely(String input) { // TO DO [but low-priority]: pull this out into a separate "library class"
    if (null == input)  return "«null»";
    return "“" + input + "”";
  }

  private utility_class() { }

}
