package org.example.shared;

import java.io.*;

public class debugFriendly_buffered_FileReader implements debugFriendly_buffered_input {
  public     debugFriendly_buffered_FileReader(String pathname_in) throws IOException { // ctor
    // TO DO: assert pathname is not null
    pathname = pathname_in;
    line_number = 0;
    my_BR = new BufferedReader(new FileReader(pathname));
  }

  public boolean ready()   throws IOException { return my_BR.ready(); }

  public String readLine() throws IOException {
    line_number += 1;
    return my_BR.readLine();
  }

  public String get_description_of_input_and_current_position() {
    return " <file at pathname “" + pathname + "”, line " + line_number + "> ";
  }

  private String         pathname;
  private BufferedReader my_BR;
  private long           line_number = Long.MIN_VALUE; // so it will almost-certainly result in nonsense if Java does the wrong thing
}
