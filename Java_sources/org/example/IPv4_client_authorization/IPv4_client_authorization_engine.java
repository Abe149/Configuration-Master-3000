package org.example.IPv4_client_authorization;

import org.example.shared.*;

import java.net.InetAddress;
import java.util.Set;
import java.util.HashSet;
import java.io.*;


// private class engine_state // I realized after typing this: why bother? // DE

public class IPv4_client_authorization_engine {

  // strategic plan: the language accepts [Java] regexes, and when these regexes are processed there is an implicit leading '^' and an implicit trailing '$'
  private Set<String>  blacklisted_FQDN_patterns = new HashSet<String>();
  private Set<String>  whitelisted_FQDN_patterns = new HashSet<String>();

  // strategic plan: short[4], use values >=0 for literal numbers, -1 for '*'
  private Set<short[]>   blacklisted_IP_patterns = new HashSet<short[]>();
  private Set<short[]>   whitelisted_IP_patterns = new HashSet<short[]>();

  private strategy_types the_active_strategy_type = null; // intentionally initializing to an invalid "value"

  // strategic plan: when strictness=0, ignore duplicate directives; when strictness=1, warn about them; when strictness>1, reject inputs with duplicate directives
  private boolean require_siteLocal = false;
  private boolean require_linkLocal = false;
  private boolean require_loopback  = false;

  // start of ctor
  public IPv4_client_authorization_engine(debugFriendly_buffered_input input, short strictness_level___in, short verbosity_in) throws IOException {
    // both of the next 2 lines: doing it "the idiom way" on purpose in case I will later need to move this [i.e. the non-"_in" version] to being a class data member variable thingy
    final short strictness_level = strictness_level___in;
    final short verbosity        = verbosity_in;

    while (input.ready()) {
      final String unstripped_line = input.readLine().replaceFirst("[#⍝].*", "").replaceAll(" +", " ").replaceFirst("^ ", "").replaceFirst(" $", ""); // remove comments, squeeze multiple contiguous ASCII spaces into one, remove leading and trailing space if any
      if (verbosity > 5)  System.err.println("INFO: in IPv4_client_authorization_engine: line before stripping: ''" + unstripped_line + "''");
      final String line = unstripped_line.replaceFirst("[#⍝].*", "").replaceAll(" +", " ").replaceFirst("^ ", "").replaceFirst(" $", ""); // remove comments, squeeze multiple contiguous ASCII spaces into one, remove leading and trailing space if any
      if (verbosity > 0)  System.err.println("INFO: in IPv4_client_authorization_engine: line after  stripping: ''" + line + "''");

      if (line.equalsIgnoreCase("require site-local")) {
        // first, detect that this is a redundant statement, if it is, and act accordingly based on strictness
        if (require_siteLocal && strictness_level > 0) {
          if                    (strictness_level > 1)
            throw new IOException("In IPv4_client_authorization_engine: redundant statement found, unacceptable when strictness level > 1, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          System.err.println("WARNING: in IPv4_client_authorization_engine: redundant statement found, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        }
        require_siteLocal = true;
      } else if (line.equalsIgnoreCase("require link-local")) {
        // first, detect that this is a redundant statement, if it is, and act accordingly based on strictness
        if (require_linkLocal && strictness_level > 0) {
          if                    (strictness_level > 1)
            throw new IOException("In IPv4_client_authorization_engine: redundant statement found, unacceptable when strictness level > 1, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          System.err.println("WARNING: in IPv4_client_authorization_engine: redundant statement found, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        }
        require_linkLocal = true;
      } else if (line.equalsIgnoreCase("require loopback")) {
        // first, detect that this is a redundant statement, if it is, and act accordingly based on strictness
        if (require_loopback  && strictness_level > 0) {
          if                    (strictness_level > 1)
            throw new IOException("In IPv4_client_authorization_engine: redundant statement found, unacceptable when strictness level > 1, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          System.err.println("WARNING: in IPv4_client_authorization_engine: redundant statement found, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        }
        require_loopback = true;

      } else if (line.matches("(?i)strategy .*")) {
        if (null != the_active_strategy_type) { // oops, user error
          if (strictness_level > 1)
            throw new IOException("In IPv4_client_authorization_engine: non-first strategy statement found, unacceptable when strictness level > 1, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          System.err.println("WARNING: in IPv4_client_authorization_engine: non-first strategy statement found, ignoring it; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        }

        final String second_field = line.split(" ")[1]; // TO DO: handle syntax errors more elegantly

        if      (second_field.equalsIgnoreCase("requirements-only")) the_active_strategy_type = strategy_types.any_client_that_meets_all_active_requirements;
        else if (second_field.equalsIgnoreCase("blacklisting"))      the_active_strategy_type = strategy_types.blacklisting;
        else if (second_field.equalsIgnoreCase("whitelisting"))      the_active_strategy_type = strategy_types.whitelisting;
        else { // oops, user error

        }






        // more TO DO here //



      } // end if



    } // end while

  } // end of ctor


  public boolean is_connection_from_this_address_authorized(InetAddress addr) {
    if (require_siteLocal && ! addr.isSiteLocalAddress())  return false;
    if (require_linkLocal && ! addr.isLinkLocalAddress())  return false;
    if (require_loopback  && ! addr. isLoopbackAddress())  return false;

    // WIP WIP WIP //
    // WIP WIP WIP //
    // WIP WIP WIP //

    return false; // WIP WIP WIP //
  }

} // end of class "IPv4_client_authorization_engine"
