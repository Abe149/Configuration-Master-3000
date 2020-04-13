package org.example.shared;

import java.io.*;

public interface debugFriendly_buffered_input {
  public boolean ready()   throws IOException;
  public String readLine() throws IOException;
  public String get_description_of_input_and_current_position();
}
