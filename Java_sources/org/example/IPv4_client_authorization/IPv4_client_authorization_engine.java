package org.example.IPv4_client_authorization;

import org.example.shared.*;

import java.net.InetAddress;
import java.util.Set;
import java.util.HashSet;
import java.io.*;
import java.util.regex.*;

import java.util.Arrays;


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
    final String IP_pattern_regex = "(\\d+|\\*)\\.(\\d+|\\*)\\.(\\d+|\\*)\\.(\\d+|\\*)";

    while (input.ready()) {
      final String unstripped_line = input.readLine().replaceFirst("[#⍝].*", "").replaceAll(" +", " ").replaceFirst("^ ", "").replaceFirst(" $", ""); // remove comments, squeeze multiple contiguous ASCII spaces into one, remove leading and trailing space if any
      if (verbosity > 5)  System.err.println("INFO: in IPv4_client_authorization_engine: line before stripping: ''" + unstripped_line + "''");
      final String line = unstripped_line.replaceFirst("[#⍝].*", "").replaceAll(" +", " ").replaceFirst("^ ", "").replaceFirst(" $", ""); // remove comments, squeeze multiple contiguous ASCII spaces into one, remove leading and trailing space if any
      if (verbosity > 0)  System.err.println("INFO: in IPv4_client_authorization_engine: line after  stripping: ''" + line + "''");

      if (line.equalsIgnoreCase("require site-local")) {
        // first, detect that this is a redundant statement, if it is, and act accordingly based on strictness
        if (require_siteLocal) {
          if (strictness_level > 0)
            throw new IOException("In IPv4_client_authorization_engine: redundant statement found, unacceptable when strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println("WARNING: in IPv4_client_authorization_engine: redundant statement found, ignored it since strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        } // end if
        require_siteLocal = true;
      } else if (line.equalsIgnoreCase("require link-local")) {
        // first, detect that this is a redundant statement, if it is, and act accordingly based on strictness
        if (require_linkLocal) {
          if (strictness_level > 0)
            throw new IOException("In IPv4_client_authorization_engine: redundant statement found, unacceptable when strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println("WARNING: in IPv4_client_authorization_engine: redundant statement found, ignored it since strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        } // end if
        require_linkLocal = true;
      } else if (line.equalsIgnoreCase("require loopback")) {
        // first, detect that this is a redundant statement, if it is, and act accordingly based on strictness
        if (require_loopback) {
          if (strictness_level > 0)
            throw new IOException("In IPv4_client_authorization_engine: redundant statement found, unacceptable when strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println("WARNING: in IPv4_client_authorization_engine: redundant statement found, ignored it since strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        } // end if
        require_loopback = true;

      } else if (line.matches("(?i)strategy .+")) {
        final String second_field = line.split(" ")[1]; // TO DO: handle syntax errors more elegantly

        strategy_types the_new_strategy_type = null;

        if      (second_field.equalsIgnoreCase("requirements-only")) the_new_strategy_type = strategy_types.any_client_that_meets_all_active_requirements;
        else if (second_field.equalsIgnoreCase("blacklisting"))      the_new_strategy_type = strategy_types.blacklisting;
        else if (second_field.equalsIgnoreCase("whitelisting"))      the_new_strategy_type = strategy_types.whitelisting;
        else { // oops, user error
          if (strictness_level > 0)
            throw new IOException("In IPv4_client_authorization_engine: unrecognized strategy found, unacceptable when strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println("WARNING: in IPv4_client_authorization_engine: unrecognized strategy found, ignored it since strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        }

        if (null != the_active_strategy_type) { // oops, user error
          if (strictness_level > 1)
            throw new IOException("In IPv4_client_authorization_engine: non-first strategy statement found, unacceptable when strictness level > 1, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (strictness_level > 0 && the_new_strategy_type != the_active_strategy_type)
            throw new IOException("In IPv4_client_authorization_engine: conflicting strategy statement found, unacceptable when strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println("WARNING: in IPv4_client_authorization_engine: non-first strategy statement found, ignored it since strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        } else { // the_active_strategy_type _is_ null, which is what we "want" it to be at this point, for a certain meaning of the word "want"
          the_active_strategy_type = the_new_strategy_type;
          if (verbosity > 0)  System.err.println("INFO: in IPv4_client_authorization_engine: set the strategy to " + the_active_strategy_type);
        } // end if

      } else if (line.matches("(?i)blacklist FQDN pattern .+")) {

         if (strategy_types.blacklisting == the_active_strategy_type) {

          final String pattern = line.split(" ")[3]; // TO DO: handle syntax errors more elegantly
          if (blacklisted_FQDN_patterns.contains(pattern)) {
            if (strictness_level > 1)
              throw new IOException("In IPv4_client_authorization_engine: redundant ''blacklist FQDN pattern'' statement found, unacceptable when strictness level > 1, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
            if (verbosity > 0)  System.err.println("WARNING: in IPv4_client_authorization_engine: redundant ''blacklist FQDN pattern'' statement found, ignored it since strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          }
          else if (verbosity > 5)  System.err.println("INFO: in IPv4_client_authorization_engine: (non-redundant) ''blacklist FQDN pattern'' statement found, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          blacklisted_FQDN_patterns.add(pattern); // should cause no harm to add it "again" when duplicate

        } else { // _not_ in blacklist mode, so the input was wrong
          if (strictness_level > 0)
            throw new IOException("In IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, unacceptable when strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println("WARNING: in IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, ignored it since strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        } // end if

      } else if (line.matches("(?i)whitelist FQDN pattern .+")) {

        if (strategy_types.whitelisting == the_active_strategy_type) {

          final String pattern = line.split(" ")[3]; // TO DO: handle syntax errors more elegantly
          if (whitelisted_FQDN_patterns.contains(pattern)) {
            if (strictness_level > 1)
              throw new IOException("In IPv4_client_authorization_engine: redundant ''whitelist FQDN pattern'' statement found, unacceptable when strictness level > 1, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
            if (verbosity > 0)  System.err.println("WARNING: in IPv4_client_authorization_engine: redundant ''whitelist FQDN pattern'' statement found, ignored it since strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          }
          else if (verbosity > 5)  System.err.println("INFO: in IPv4_client_authorization_engine: (non-redundant) ''whitelist FQDN pattern'' statement found, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          whitelisted_FQDN_patterns.add(pattern); // should cause no harm to add it "again" when duplicate
        } else { // _not_ in whitelist mode, so the input was wrong
          if (strictness_level > 0)
            throw new IOException("In IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, unacceptable when strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println("WARNING: in IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, ignored it since strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        } // end if

      } else if (line.matches("(?i)blacklist IP pattern " + IP_pattern_regex)) { // cheating a little bit by using a regex

        if (strategy_types.blacklisting == the_active_strategy_type) {

          final String pattern = line.split(" ")[3]; // TO DO: handle syntax errors more elegantly
          final Matcher m1 = Pattern.compile(IP_pattern_regex).matcher(pattern); // sorry, I know it`s confusing
          final boolean matched_the_regex = m1.find(); // CRUCIAL

          final String octets_or_asterisks[] = new String[4];

          // maybe TO DO here: assert that 4==m1.groupCount()

          // the following "looks wrong" b/c of the offset in the indices, but Don`t Panic
          octets_or_asterisks[0] = m1.group(1);
          octets_or_asterisks[1] = m1.group(2);
          octets_or_asterisks[2] = m1.group(3);
          octets_or_asterisks[3] = m1.group(4);

          final short pattern_as_array_of_shorts[] = new short[4];
          for (short index = 0; index < 4; ++index) {

            if ("*".equals(octets_or_asterisks[index]))  pattern_as_array_of_shorts[index] = -1;
            else {
              final short the_octet = Short.parseShort(octets_or_asterisks[index]);
              // negative numbers should be _absolutely_ impossible here [get the pun?  ;-)], since the regex uses the decimal-digit "macro" and _not_ any kind of magical regex for "an integer even if negative"
              if (the_octet < 0)  throw new IOException("In IPv4_client_authorization_engine: an IP octet was somehow found to be negative [" + the_octet + "]; this is not supposed to be possible, i.e. the incorrect data should not have ''made it this far'' in the code; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());

              if (the_octet > 255) {
                if (strictness_level > 0)
                  throw new IOException("In IPv4_client_authorization_engine: an IP octet [" + the_octet + "] was found to be > 255, unacceptable when strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
                if (verbosity > 0)  System.err.println("WARNING: in IPv4_client_authorization_engine: an IP octet [" + the_octet + "] was found to be > 255, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
              } // end if

            } // end if

          } // end for

          if (blacklisted_IP_patterns.contains(pattern_as_array_of_shorts) {
            if (strictness_level > 1)
              throw new IOException("In IPv4_client_authorization_engine: redundant ''blacklist IP pattern'' statement found, unacceptable when strictness level > 1, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
            if (verbosity > 0)  System.err.println("WARNING: in IPv4_client_authorization_engine: redundant ''blacklist IP pattern'' statement found, ignored it since strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          }
          blacklisted_IP_patterns.add(pattern_as_array_of_shorts); // should cause no harm to add it "again" when duplicate

        } else { // _not_ in blacklist mode, so the input was wrong
          if (strictness_level > 0)
            throw new IOException("In IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, unacceptable when strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println("WARNING: in IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, ignored it since strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        } // end if



      } else if (line.matches("(?i)whitelist IP pattern " + IP_pattern_regex)) { // cheating a little bit by using a regex

  // strategic plan: short[4], use values >=0 for literal numbers, -1 for '*'

  // reminder: private Set<short[]>   blacklisted_IP_patterns = new HashSet<short[]>();
  // reminder: private Set<short[]>   whitelisted_IP_patterns = new HashSet<short[]>();
  // reminder: private Set<String>  blacklisted_FQDN_patterns = new HashSet<String>();
  // reminder: private Set<String>  whitelisted_FQDN_patterns = new HashSet<String>();



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
