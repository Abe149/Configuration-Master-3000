package org.example.IPv4_client_authorization;

import org.example.shared.*;

import java.net.InetAddress;
import java.util.Set;
import java.util.HashSet;
import java.io.*;
import java.util.regex.*;

import java.util.Arrays;



// TO DO: add support for "syntactic sugar" for NON-pattern [i.e. literal match] FQDNs, so noobs won`t get tripped up on the period

// TO DO: make the WARNING messages in this file come out in red on ANSI-color-capable environments



public class IPv4_client_authorization_engine {

  // strategic plan: the language accepts [Java] regexes, and when these regexes are processed there is an implicit leading '^' and an implicit trailing '$'
  private Set<String>  blacklisted_FQDN_patterns = new HashSet<String>(); // maybe TO DO: replace String with Pattern here
  private Set<String>  whitelisted_FQDN_patterns = new HashSet<String>(); // maybe TO DO: replace String with Pattern here

  // strategic plan: short[4], use values >=0 for literal numbers, -1 for '*'
  private Set<short[]>   blacklisted_IP_patterns = new HashSet<short[]>();
  private Set<short[]>   whitelisted_IP_patterns = new HashSet<short[]>();

  private strategy_types the_active_strategy_type = null; // intentionally initializing to an invalid "value"

  // strategic plan: when strictness=0, ignore duplicate directives; when strictness=1, warn about them; when strictness>1, reject inputs with duplicate directives
  private boolean require_siteLocal = false;
  private boolean require_linkLocal = false;
  private boolean require_loopback  = false;

  private short verbosity, strictness_level;

  // start of ctor
  public IPv4_client_authorization_engine(debugFriendly_buffered_input input, short strictness_level___in, short verbosity_in) throws IOException {
    strictness_level = strictness_level___in;

    verbosity = verbosity_in;
    final String IP_pattern_regex = "(\\d+|\\*)\\.(\\d+|\\*)\\.(\\d+|\\*)\\.(\\d+|\\*)";

    while (input.ready()) {
      final String unstripped_line = input.readLine();
      if (verbosity > 5)  System.err.println("INFO: in IPv4_client_authorization_engine: line before stripping: ''" + unstripped_line + "''");
      final String line = unstripped_line.replaceFirst("[#⍝].*", "").replaceAll(" +", " ").replaceFirst("^ ", "").replaceFirst(" $", ""); // remove comments, squeeze multiple contiguous ASCII spaces into one, remove leading and trailing space if any
      if (verbosity > 0)  System.err.println("INFO: in IPv4_client_authorization_engine: line after  stripping: ''" + line + "''");

      if (line.equalsIgnoreCase("require site-local")) {
        // first, detect that this is a redundant statement, if it is, and act accordingly based on strictness
        if (require_siteLocal) {
          if (strictness_level > 0)  throw new IOException("In IPv4_client_authorization_engine: redundant statement found, unacceptable when"+" strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: redundant statement found, ignored it since" +" strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        } // end if
        require_siteLocal = true;
      } else if (line.equalsIgnoreCase("require link-local")) {
        // first, detect that this is a redundant statement, if it is, and act accordingly based on strictness
        if (require_linkLocal) {
          if (strictness_level > 0)  throw new IOException("In IPv4_client_authorization_engine: redundant statement found, unacceptable when"+" strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: redundant statement found, ignored it since" +" strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        } // end if
        require_linkLocal = true;
      } else if (line.equalsIgnoreCase("require loopback")) {
        // first, detect that this is a redundant statement, if it is, and act accordingly based on strictness
        if (require_loopback) {
          if (strictness_level > 0)  throw new IOException("In IPv4_client_authorization_engine: redundant statement found, unacceptable when"+" strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: redundant statement found, ignored it since" +" strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        } // end if
        require_loopback = true;

      } else if (line.matches("(?i)strategy .+")) {
        final String second_field = line.split(" ")[1]; // TO DO: handle syntax errors more elegantly

        strategy_types the_new_strategy_type = null;

        if      (second_field.equalsIgnoreCase("requirements-only")) the_new_strategy_type = strategy_types.any_client_that_meets_all_active_requirements;
        else if (second_field.equalsIgnoreCase("blacklisting"))      the_new_strategy_type = strategy_types.blacklisting;
        else if (second_field.equalsIgnoreCase("whitelisting"))      the_new_strategy_type = strategy_types.whitelisting;
        else { // oops, user error
          if (strictness_level > 0)  throw new IOException("In IPv4_client_authorization_engine: unrecognized strategy found, unacceptable when"+" strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: unrecognized strategy found, ignored it since" +" strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        }

        if (null != the_active_strategy_type) { // oops, user error
          if (strictness_level > 1)  throw new IOException("In IPv4_client_authorization_engine: non-first strategy statement found, unacceptable when strictness level > 1, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
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
            if (strictness_level > 1)  throw new IOException("In IPv4_client_authorization_engine: redundant ''blacklist FQDN pattern'' statement found, unacceptable when "+"strictness level > 1, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
            if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: redundant ''blacklist FQDN pattern'' statement found, ignored it since " +"strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          }
          else if (verbosity > 5)  System.err.println("INFO: in IPv4_client_authorization_engine: (non-redundant) ''blacklist FQDN pattern'' statement found, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          blacklisted_FQDN_patterns.add(pattern); // should cause no harm to add it "again" when duplicate

        } else { // _not_ in blacklist mode, so the input was wrong
          if (strictness_level > 0)  throw new IOException("In IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, unacceptable when"+" strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, ignored it since" +" strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        } // end if

      } else if (line.matches("(?i)whitelist FQDN pattern .+")) {

        if (strategy_types.whitelisting == the_active_strategy_type) {

          final String pattern = line.split(" ")[3]; // TO DO: handle syntax errors more elegantly
          if (whitelisted_FQDN_patterns.contains(pattern)) {
            if (strictness_level > 1)  throw new IOException("In IPv4_client_authorization_engine: redundant ''whitelist FQDN pattern'' statement found, unacceptable when "+"strictness level > 1, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
            if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: redundant ''whitelist FQDN pattern'' statement found, ignored it since " +"strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          }
          else if (verbosity > 5)  System.err.println("INFO: in IPv4_client_authorization_engine: (non-redundant) ''whitelist FQDN pattern'' statement found, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          whitelisted_FQDN_patterns.add(pattern); // should cause no harm to add it "again" when duplicate
        } else { // _not_ in whitelist mode, so the input was wrong
          if (strictness_level > 0)  throw new IOException("In IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, unacceptable when "+"strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, ignored it since " +"strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
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
                if (strictness_level > 0)  throw new IOException("In IPv4_client_authorization_engine: an IP octet [" + the_octet + "] was found to be > 255, unacceptable when strictness level > 0;"+" line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
                if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: an IP octet [" + the_octet + "] was found to be > 255, so this rule should never match;"       +" line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
              } // end if

            } // end if

          } // end for

          if (blacklisted_IP_patterns.contains(pattern_as_array_of_shorts)) {
            if (strictness_level > 1)  throw new IOException("In IPv4_client_authorization_engine: redundant ''blacklist IP pattern'' statement found, unacceptable when "+"strictness level > 1, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
            if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: redundant ''blacklist IP pattern'' statement found, ignored it since " +"strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          }
          blacklisted_IP_patterns.add(pattern_as_array_of_shorts); // should cause no harm to add it "again" when duplicate

        } else { // _not_ in blacklist mode, so the input was wrong
          if (strictness_level > 0)  throw new IOException("In IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, unacceptable when "+"strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, ignored it since " +"strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        } // end if

      } else if (line.matches("(?i)whitelist IP pattern " + IP_pattern_regex)) { // cheating a little bit by using a regex

        if (strategy_types.whitelisting == the_active_strategy_type) {

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
                if (strictness_level > 0)  throw new IOException("In IPv4_client_authorization_engine: an IP octet [" + the_octet + "] was found to be > 255, unacceptable when strictness level > 0;"+" line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
                if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: an IP octet [" + the_octet + "] was found to be > 255, so this rule should never match;"       +" line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
              } // end if

            } // end if

          } // end for

          if (whitelisted_IP_patterns.contains(pattern_as_array_of_shorts)) {
            if (strictness_level > 1)  throw new IOException("In IPv4_client_authorization_engine: redundant ''blacklist IP pattern'' statement found, unacceptable when "+"strictness level > 1, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
            if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: redundant ''blacklist IP pattern'' statement found, ignored it since " +"strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          }
          whitelisted_IP_patterns.add(pattern_as_array_of_shorts); // should cause no harm to add it "again" when duplicate

        } else { // _not_ in whitelist mode, so the input was wrong
          if (strictness_level > 0)  throw new IOException("In IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, unacceptable when "+"strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, ignored it since " +"strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        } // end if

      } // end if

    } // end while

    if (verbosity > 8) {



            // TO DO: brain dump




      } // end if

  } // end of ctor


  public boolean is_connection_from_this_address_authorized(InetAddress addr) throws IOException {
    if (require_siteLocal && ! addr.isSiteLocalAddress())  return false;
    if (require_linkLocal && ! addr.isLinkLocalAddress())  return false;
    if (require_loopback  && ! addr. isLoopbackAddress())  return false;

    switch (the_active_strategy_type) {
      case any_client_that_meets_all_active_requirements:  return true; // one of the close-by above 3 "if"s would have already returned false if that was the correct answer

      // TO DO if/when I have good working test cases with good coverage for this file of code: DRY-ify the blacklisting-vs.-whitelisting matching code

      case blacklisting:

        // first, the easy part: the textual matching
        for (String pattern : blacklisted_FQDN_patterns)  if (pattern.matches(addr.getCanonicalHostName()))  return false;

        for (short[] IP_pattern_array : blacklisted_IP_patterns) {
          // cheating again: going to use string matching
          String IP_pattern_regex = "^";
          boolean bad_pattern = false;
          for (short index = 0; index < 4; ++index) {
            if (      IP_pattern_array[index] < -1) {
              if (strictness_level > 1)  throw new IOException("In IPv4_client_authorization_engine: internal error: an IP pattern-array element " + IP_pattern_array[index] + " was < -1, unacceptable when "+"strictness level > 1");
              System.err.println(                     "WARNING: in IPv4_client_authorization_engine: internal error: an IP pattern-array element " + IP_pattern_array[index] + " was < -1, ignored it since " +"strictness level ≤ 0");
              bad_pattern = true;
            }
            else if (-1 == IP_pattern_array[index]) // wildcard
              IP_pattern_regex = IP_pattern_regex + "\\d+";
            else if       (IP_pattern_array[index] >= 0 && IP_pattern_array[index] <= 255) // a normal octet
              IP_pattern_regex = IP_pattern_regex + IP_pattern_array[index];
            else { // IP_pattern_array[index] must be > 255
              if (strictness_level > 1)  throw new IOException("In IPv4_client_authorization_engine: internal error: an IP pattern-array element " + IP_pattern_array[index] + " was > 255, unacceptable when "+"strictness level > 1");
              System.err.println(                     "WARNING: in IPv4_client_authorization_engine: internal error: an IP pattern-array element " + IP_pattern_array[index] + " was > 255, ignored it since " +"strictness level ≤ 0");
              bad_pattern = true;
            } // end if
            if (index < 3)  IP_pattern_regex = IP_pattern_regex + "\\."; // IP octet separator
          } // end for
          final byte[] IP_addr_as_byte_array = addr.getAddress(); // TO DO: check this has the right length, throw/WARN if not
          if (bad_pattern) {
            // there`s no need to check the strictness level and maybe throw here, since the code that sets "bad_pattern" would have already thrown before execution got here [i.e. to this comment`s "line"] if the pattern was bad in a way that this code can detect
            System.err.println("WARNING: in IPv4_client_authorization_engine: internal error: an IP pattern-array`s contents [{" + IP_addr_as_byte_array[0] + ", " + IP_addr_as_byte_array[1] + ", " + IP_addr_as_byte_array[2] + ", " + IP_addr_as_byte_array[3] + "}] were bad; ignored it since strictness level ≤ 0; please tell Abe to debug this engine.  Allowing the matching loop to continue.");
          } else { // _not_ a bad internal pattern
            IP_pattern_regex = IP_pattern_regex + '$'; // just so it shows up correctly in the super-verbose output, when enabled
            if (verbosity > 8)  System.err.println("INFO: in IPv4_client_authorization_engine: regex pattern for IP address computed by a transformation from an array of integers: ''" + IP_pattern_regex + "''");
            if ((IP_pattern_regex).matches(String.valueOf(IP_addr_as_byte_array[0]) + '.' + IP_addr_as_byte_array[1] + '.' + IP_addr_as_byte_array[2] + '.' + IP_addr_as_byte_array[3])) {
              if (verbosity > 8)  System.err.println("INFO: in IPv4_client_authorization_engine: returning false because a rule matched.");
              return false;
            } // end if
          } // end if
        } // end for

        if (verbosity > 8)  System.err.println("INFO: in IPv4_client_authorization_engine: returning true because no rules matched.");
        return true; // the default answer when blacklisting


      case whitelisting:

        // first, the easy part: the textual matching
        for (String pattern : whitelisted_FQDN_patterns)  if (pattern.matches(addr.getCanonicalHostName()))  return true;

        for (short[] IP_pattern_array : whitelisted_IP_patterns) {
          // cheating again: going to use string matching
          String IP_pattern_regex = "^";
          boolean bad_pattern = false;
          for (short index = 0; index < 4; ++index) {
            if (      IP_pattern_array[index] < -1) {
              if (strictness_level > 1)  throw new IOException("In IPv4_client_authorization_engine: internal error: an IP pattern-array element " + IP_pattern_array[index] + " was < -1, unacceptable when "+"strictness level > 1");
              System.err.println(                     "WARNING: in IPv4_client_authorization_engine: internal error: an IP pattern-array element " + IP_pattern_array[index] + " was < -1, ignored it since " +"strictness level ≤ 0");
              bad_pattern = true;
            }
            else if (-1 == IP_pattern_array[index]) // wildcard
              IP_pattern_regex = IP_pattern_regex + "\\d+";
            else if       (IP_pattern_array[index] >= 0 && IP_pattern_array[index] <= 255) // a normal octet
              IP_pattern_regex = IP_pattern_regex + IP_pattern_array[index];
            else { // IP_pattern_array[index] must be > 255
              if (strictness_level > 1)  throw new IOException("In IPv4_client_authorization_engine: internal error: an IP pattern-array element " + IP_pattern_array[index] + " was > 255, unacceptable when "+"strictness level > 1");
              System.err.println(                     "WARNING: in IPv4_client_authorization_engine: internal error: an IP pattern-array element " + IP_pattern_array[index] + " was > 255, ignored it since " +"strictness level ≤ 0");
              bad_pattern = true;
            } // end if
            if (index < 3)  IP_pattern_regex = IP_pattern_regex + "\\."; // IP octet separator
          } // end for
          final byte[] IP_addr_as_byte_array = addr.getAddress(); // TO DO: check this has the right length, throw/WARN if not
          if (bad_pattern) {
            // there`s no need to check the strictness level and maybe throw here, since the code that sets "bad_pattern" would have already thrown before execution got here [i.e. to this comment`s "line"] if the pattern was bad in a way that this code can detect
            System.err.println("WARNING: in IPv4_client_authorization_engine: internal error: an IP pattern-array`s contents [{" + IP_addr_as_byte_array[0] + ", " + IP_addr_as_byte_array[1] + ", " + IP_addr_as_byte_array[2] + ", " + IP_addr_as_byte_array[3] + "}] were bad; ignored it since strictness level ≤ 0; please tell Abe to debug this engine.  Allowing the matching loop to continue.");
          } else { // _not_ a bad internal pattern
            IP_pattern_regex = IP_pattern_regex + '$'; // just so it shows up correctly in the super-verbose output, when enabled
            if (verbosity > 8)  System.err.println("INFO: in IPv4_client_authorization_engine: regex pattern for IP address computed by a transformation from an array of integers: ''" + IP_pattern_regex + "''");
            if ((IP_pattern_regex).matches(String.valueOf(IP_addr_as_byte_array[0]) + '.' + IP_addr_as_byte_array[1] + '.' + IP_addr_as_byte_array[2] + '.' + IP_addr_as_byte_array[3])) {
              if (verbosity > 8)  System.err.println("INFO: in IPv4_client_authorization_engine: returning true because a rule matched.");
              return true;
            } // end if
          } // end if
        } // end for

        if (verbosity > 8)  System.err.println("INFO: in IPv4_client_authorization_engine: returning false because no rules matched.");
        return false; // the default answer when whitelisting

      default:
        if (strictness_level > 0) throw new IOException("In IPv4_client_authorization_engine: invalid input to a switch statement.  This is not supposed to be possible.");
        if (verbosity > 0)  System.err.println("WARNING: in IPv4_client_authorization_engine: invalid input to a switch statement.  This is not supposed to be possible.");
      // the lack of a '}' here is OK
    } // end switch

    if (strictness_level > 0) throw new IOException("In IPv4_client_authorization_engine: ''is_connection_from_this_address_authorized'' reached a point in the code that it is not supposed to be able to reach.");
    if (verbosity > 0) System.err.println( "WARNING: in IPv4_client_authorization_engine: ''is_connection_from_this_address_authorized'' reached a point in the code that it is not supposed to be able to reach.  Returning false.");

    return false;
  } // end of "is_connection_from_this_address_authorized"

} // end of class "IPv4_client_authorization_engine"
