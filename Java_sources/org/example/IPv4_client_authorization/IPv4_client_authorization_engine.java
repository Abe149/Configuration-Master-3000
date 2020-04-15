package org.example.IPv4_client_authorization;

import        org.example.shared.*;
import static org.example.shared.utility_class.*;

import java.net.InetAddress;
import java.util.Set;
import java.util.HashSet;
import java.io.*;
import java.util.regex.*;

import java.util.Arrays;



// TO DO: make the WARNING messages in this file come out in red on ANSI-color-capable environments

public class IPv4_client_authorization_engine {

  private class IPv4_pattern_element {
    private short from_inclusive,
                    to_inclusive; // can`t use "byte" here b/c -- in Java -- "byte" is always _signed_, leading to [0 … 255] being interpreted as [0 … -1]!!!  Sheesh.

    public IPv4_pattern_element(short from_in, short to_in) { // ctor: simple integer inputs
      // TO DO: assert [or something] that each of the param.s is in the range 0 ≤ param. ≤ 255
      from_inclusive = from_in;
      to_inclusive   =   to_in;
    }

    private void validate_octet(short the_octet) throws Exception { // DRY
      if (the_octet <   0)  throw new Exception("In IPv4_client_authorization_engine: an IP octet was somehow found to be negative [" + the_octet + "]");

      if (the_octet > 255)  throw new Exception("In IPv4_client_authorization_engine: an IP octet [" + the_octet + "] was found to be > 255");
    }

    public IPv4_pattern_element(String input) throws Exception { // ctor: parse the input and create a pattern element
      if (null == input)  throw new Exception("The IPv4_pattern_element(String) ctor got a null input");
      // delegating ctors -- e.g. "this(0, 255);" -- don`t work in Java [at least not in 1.6] as something other than the first [only?] statement of a ctor
      if      (input.equals("*"))  { from_inclusive = 0; to_inclusive = 255; }
      else if (input.matches("\\d+")) {

        final short the_octet = Short.parseShort(input);

        validate_octet(the_octet);

        from_inclusive = the_octet;
          to_inclusive = the_octet;

      } else if (input.matches("\\[\\d+…\\d+\\]")) { // TO DO: allow ASCII spaces inside the expression

        Matcher m = Pattern.compile("\\[(\\d+)…(\\d+)\\]").matcher(input); // "this time, with _feeling_" ;-) ...  where "feeling" = "regex capture groups"
        m.find(); // the "traditional-for-Java-regex" crucial sine-qua-{crash if you _are_ lucky, proceed with incorrect results if you are _not_}
        from_inclusive = Short.parseShort(m.group(1));
          to_inclusive = Short.parseShort(m.group(2));

        validate_octet(from_inclusive);
        validate_octet(  to_inclusive);

      } // end if
    } // end of ctor

    public int hashCode() { return from_inclusive*256 + to_inclusive; }

    public boolean equals(Object other) {
      if (! (other instanceof IPv4_pattern_element))  return false;
      final IPv4_pattern_element other_with_correct_type = (IPv4_pattern_element) other;
      return from_inclusive == other_with_correct_type.from_inclusive && to_inclusive == other_with_correct_type.to_inclusive;
    }

    public String toString() {
      if (from_inclusive == to_inclusive)  return " " + to_inclusive + ' ';
      if (0 == from_inclusive && 255 == to_inclusive)  return "*";
      return "[" + from_inclusive + " … " + to_inclusive + ']';
    }

    public boolean matches(short IP_byte) { // we cannot [in any practical, convenient way] use Java`s "byte" type here  :-(
      // TO DO: assert [or something] that the param. is in the range 0 ≤ param. ≤ 255
      return from_inclusive <= IP_byte && IP_byte <= to_inclusive;
    }
  }


  private class IPv4_pattern {
    public final IPv4_pattern_element the_pattern[] = new IPv4_pattern_element[4]; // maybe TO DO: write [a] getter[s] and [a] setter[s], make this private?

    public IPv4_pattern(byte a0, byte a1, byte b0, byte b1, byte c0, byte c1, byte d0, byte d1) { // ctor for when the data is known all at once
      // TO DO: assert [or something] that each of the param.s is in the range 0 ≤ param. ≤ 255
      the_pattern[0] = new IPv4_pattern_element(a0, a1);
      the_pattern[1] = new IPv4_pattern_element(b0, b1);
      the_pattern[2] = new IPv4_pattern_element(c0, c1);
      the_pattern[3] = new IPv4_pattern_element(d0, d1);
    }

    public IPv4_pattern() { // ctor for when the data is _not_ known all at once
      // try to make "users" crash if/when they try to read an element from this array without something valid having been assigned to it yet
      the_pattern[0] = null;
      the_pattern[1] = null;
      the_pattern[2] = null;
      the_pattern[3] = null;
    }

    public int hashCode() { return the_pattern.hashCode(); } // I hope Java will Do The Right Thing for a change

    public boolean equals(Object other) {
      if (! (other instanceof IPv4_pattern))  return false;
      return Arrays.equals(the_pattern, ((IPv4_pattern)other).the_pattern);
    }

    public String toString() {
      return "«" + the_pattern[0] + '.' + the_pattern[1] + '.' + the_pattern[2] + '.' + the_pattern[3] + '»';
    }

    public boolean matches(byte IP[]) { // we need to "get clever" here to convert e.g. -1 to 255 ...  "thanks Java"  :-(
      // maybe TO DO later: assert that the length of IP is 4
      return the_pattern[0].matches((short)(IP[0] & 255)) &&
             the_pattern[1].matches((short)(IP[1] & 255)) &&
             the_pattern[2].matches((short)(IP[2] & 255)) &&
             the_pattern[3].matches((short)(IP[3] & 255));
    }
  }



  // strategic plan: the language accepts [Java] regexes, and when these regexes are processed there is an implicit leading '^' and an implicit trailing '$'
  // maybe TO DO: replace String with Pattern here
  private Set<String>       blacklisted_FQDN_patterns = new HashSet<String>();
  private Set<String>       whitelisted_FQDN_patterns = new HashSet<String>();

  private Set<IPv4_pattern>   blacklisted_IP_patterns = new HashSet<IPv4_pattern>();
  private Set<IPv4_pattern>   whitelisted_IP_patterns = new HashSet<IPv4_pattern>();

  private strategy_types the_active_strategy_type = null; // intentionally initializing to an invalid "value"

  // strategic plan: when strictness=0, ignore duplicate directives; when strictness=1, warn about them; when strictness>1, reject inputs with duplicate directives
  private boolean require_siteLocal = false;
  private boolean require_linkLocal = false;
  private boolean require_loopback  = false;

  private long verbosity;
  private short strictness_level;

  // start of ctor
  public IPv4_client_authorization_engine(debugFriendly_buffered_input input, short strictness_level___in, long verbosity_in) throws IOException {
    strictness_level = strictness_level___in;
    verbosity = verbosity_in;

    if (strictness_level > 0 && verbosity < 0)  throw new IOException("{verbosity [" + verbosity + "] < 0} and/but strictness_level [" + strictness_level + "] > 0 ");

    final String                    IP_pattern_regex_for_each_element = "(\\d+|\\*|\\[\\d+…\\d+\\])";

    final String IP_pattern_regex = IP_pattern_regex_for_each_element + "\\." +
                                    IP_pattern_regex_for_each_element + "\\." +
                                    IP_pattern_regex_for_each_element + "\\." +
                                    IP_pattern_regex_for_each_element;

    boolean in_the_middle_of_a_multiline_comment = false; // this _MUST_ be initialized to false or the parser will fail _spectacularly_
    while (input.ready()) {
      final String totally_unstripped_line = input.readLine();
      if (verbosity > 99)  System.err.println("\033[40;90mINFO: in IPv4_client_authorization_engine: --- line before _any_ stripping: ''" + totally_unstripped_line + "'' ---\033[0m"); // dark-grey text on a black bkgr.

      if (totally_unstripped_line.length() < 1) {
        if (verbosity > 99)  System.err.println("\033[40;90mINFO: in IPv4_client_authorization_engine: the line was _totally_ empty, so intentionally going back to the top of the parser loop\033[0m"); // dark-grey text on a black bkgr.
        continue;
      }

 // the next line [pun intended ;-)]: _intentionally_ not "final"
      String line = totally_unstripped_line.replaceFirst("[#⍝].*", "");

      if (verbosity > 5)  System.err.println("INFO: in IPv4_client_authorization_engine: line after stripping until-end-of-line comments: ''" + line + "''");

      if (line.length() < 1) {
        if (verbosity > 99)  System.err.println("\033[40;90mINFO: in IPv4_client_authorization_engine: the line was empty after the stripping of until-end-of-line comments, so intentionally going back to the top of the parser loop\033[0m"); // dark-grey text on a black bkgr.
        continue;
      }

      if (in_the_middle_of_a_multiline_comment) {
        final int index_of_asterisk_slash = line.indexOf("*/");
        if (index_of_asterisk_slash >= 0) { // equivalently: if (line.contains("*/")) {
        final String to_ignore = line.substring(0, index_of_asterisk_slash); // so I can cut down on the verbosity when this is empty
          if (verbosity > 0 && to_ignore.length() > 0)  System.err.println("INFO: in IPv4_client_authorization_engine: ignoring ''" + to_ignore + "'' because it seems to be part of a multi-line comment.");
          line = line.substring(index_of_asterisk_slash + 2);
          // equivalently to the preceding line [pun not intended ;-)]:  line = line.replaceFirst("^.*?\\*/", ""); // important: the '?' makes the corresponding regex component "reluctant", i.e. non-greedy
          if (verbosity > 5)  System.err.println("INFO: in IPv4_client_authorization_engine: line after stripping the remainder of a multi-line comment: ''" + line + "''");
          in_the_middle_of_a_multiline_comment = false;
        } else { // in_the_middle_of_a_multiline_comment and/but _not_ index_of_asterisk_slash >= 0
          if (verbosity > 0 && line.length() > 0)  System.err.println("INFO: in IPv4_client_authorization_engine: ignoring ''" + line + "'' because it seems to be part of a multi-line comment.");
          continue; // go back to the start of the input loop... I hope!  ;-)
        } // end if
      } // end if

      // now, get rid of any _single-line_ "/* ... */"-style comments that a whackadoo may have used to comment out only _part_ of a line
      line = line.replaceAll("/\\*.*?\\*/", ""); // important: the '?' makes the corresponding regex component "reluctant", i.e. non-greedy; this is needed so as to not accidentally remove input "in the middle" in a test case such as "/* comment 1 */ valid input /* comment 2 */"
      if (verbosity > 5)  System.err.println("INFO: in IPv4_client_authorization_engine: line after stripping single-line /* ... */ comments: ''" + line + "''");

      // this must come _after_ the stripping of any _single-line_ "/* ... */"-style comments
      final int index_of_slash_asterisk = line.indexOf("/*");
      if (index_of_slash_asterisk >= 0) { // equivalently: if (line.contains("/*")) {
        in_the_middle_of_a_multiline_comment = true;
        final String to_ignore = line.substring(index_of_slash_asterisk); // so I can cut down on the verbosity when this is empty
        if (verbosity > 0 && to_ignore.length() > 0)  System.err.println("INFO: in IPv4_client_authorization_engine: ignoring ''" + to_ignore + "'' because it seems to be part of a multi-line comment.");
        line = line.substring(0, index_of_slash_asterisk);
        if (verbosity > 1)  System.err.println("INFO: in IPv4_client_authorization_engine: line after removing the part that seems to be the start of a multiline comment: ''" + line + "''");
      }

      line = line.trim();
      if (verbosity > 5)  System.err.println("INFO: in IPv4_client_authorization_engine: line after trimming after stripping comments: ''" + line + "''");

      if (line.length() < 1) {
        if (verbosity > 99)  System.err.println("\033[40;90mINFO: in IPv4_client_authorization_engine: the line was empty after trimming after the stripping of comments, so intentionally going back to the top of the parser loop\033[0m"); // dark-grey text on a black bkgr.
        continue;
      }

      line = line.replaceAll(" +", " ");
      if (verbosity > 5)  System.err.println("INFO: in IPv4_client_authorization_engine: line after squeezing multiple contiguous ASCII spaces: ''" + line + "''");


      if (line.equalsIgnoreCase("require site-local")) {
        // first, detect that this is a redundant statement, if it is, and act accordingly based on strictness
        if (require_siteLocal) {
          if (strictness_level > 0)  throw new IOException("In IPv4_client_authorization_engine: redundant statement found, unacceptable when"+" strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: redundant statement found, ignored it since" +" strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        } else {
          if (verbosity > 0)  System.err.println("INFO: in IPv4_client_authorization_engine: adding requirement: site-local");
        } // end if
        require_siteLocal = true;
      } else if (line.equalsIgnoreCase("require link-local")) {
        // first, detect that this is a redundant statement, if it is, and act accordingly based on strictness
        if (require_linkLocal) {
          if (strictness_level > 0)  throw new IOException("In IPv4_client_authorization_engine: redundant statement found, unacceptable when"+" strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: redundant statement found, ignored it since" +" strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        } else {
          if (verbosity > 0)  System.err.println("INFO: in IPv4_client_authorization_engine: adding requirement: link-local");
        } // end if
        require_linkLocal = true;
      } else if (line.equalsIgnoreCase("require loopback")) {
        // first, detect that this is a redundant statement, if it is, and act accordingly based on strictness
        if (require_loopback) {
          if (strictness_level > 0)  throw new IOException("In IPv4_client_authorization_engine: redundant statement found, unacceptable when"+" strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: redundant statement found, ignored it since" +" strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        } else {
          if (verbosity > 0)  System.err.println("INFO: in IPv4_client_authorization_engine: adding requirement: loopback");
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

      } else if (line.matches("(?i)blacklist FQDN literal .+")) {

         if (strategy_types.blacklisting == the_active_strategy_type) {

          // TO DO: handle syntax errors more elegantly

          final String pattern = literalize_regex(line.split(" ")[3]);

          if (blacklisted_FQDN_patterns.contains(pattern)) {
            if (strictness_level > 1)  throw new IOException("IN IPv4_client_authorization_engine: redundant ''blacklist FQDN literal'' statement found, unacceptable when "+"strictness level > 1, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
            if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: redundant ''blacklist FQDN literal'' statement found, ignored it since " +"strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          }
          else if (verbosity > 0)  System.err.println("INFO: in IPv4_client_authorization_engine: (non-redundant) ''blacklist FQDN literal'' statement found, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position() + ", going to add the pattern ''" + pattern + "''");
          blacklisted_FQDN_patterns.add(pattern); // should cause no harm to add it "again" when duplicate

        } else { // _not_ in blacklist mode, so the input was wrong
          if (strictness_level > 0)  throw new IOException("In IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, unacceptable when"+" strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, ignored it since" +" strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        } // end if

      } else if (line.matches("(?i)whitelist FQDN literal .+")) {

         if (strategy_types.whitelisting == the_active_strategy_type) {

          // TO DO: handle syntax errors more elegantly

          final String pattern = literalize_regex(line.split(" ")[3]);

          if (whitelisted_FQDN_patterns.contains(pattern)) {
            if (strictness_level > 1)  throw new IOException("IN IPv4_client_authorization_engine: redundant ''blacklist FQDN literal'' statement found, unacceptable when "+"strictness level > 1, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
            if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: redundant ''blacklist FQDN literal'' statement found, ignored it since " +"strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          }
          else if (verbosity > 0)  System.err.println("INFO: in IPv4_client_authorization_engine: (non-redundant) ''blacklist FQDN literal'' statement found, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position() + ", going to add the pattern ''" + pattern + "''");
          whitelisted_FQDN_patterns.add(pattern); // should cause no harm to add it "again" when duplicate

        } else { // _not_ in whitelist mode, so the input was wrong
          if (strictness_level > 0)  throw new IOException("In IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, unacceptable when"+" strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, ignored it since" +" strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        } // end if

      } else if (line.matches("(?i)blacklist FQDN pattern .+")) {

         if (strategy_types.blacklisting == the_active_strategy_type) {

          // TO DO: handle syntax errors more elegantly
          final String pattern = line.split(" ")[3];
          if (blacklisted_FQDN_patterns.contains(pattern)) {
            if (strictness_level > 1)  throw new IOException("In IPv4_client_authorization_engine: redundant ''blacklist FQDN pattern'' statement found, unacceptable when "+"strictness level > 1, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
            if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: redundant ''blacklist FQDN pattern'' statement found, ignored it since " +"strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          }
          else if (verbosity > 0)  System.err.println("INFO: in IPv4_client_authorization_engine: (non-redundant) ''blacklist FQDN pattern'' statement found, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position() + ", going to add the pattern ''" + pattern + "''");
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
          else if (verbosity > 0)  System.err.println("INFO: in IPv4_client_authorization_engine: (non-redundant) ''whitelist FQDN pattern'' statement found, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position() + ", going to add the pattern ''" + pattern + "''");
          whitelisted_FQDN_patterns.add(pattern); // should cause no harm to add it "again" when duplicate
        } else { // _not_ in whitelist mode, so the input was wrong
          if (strictness_level > 0)  throw new IOException("In IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, unacceptable when "+"strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, ignored it since " +"strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        } // end if

      } else if (line.matches("(?i)blacklist IP pattern " + IP_pattern_regex)) { // cheating a little bit by using a regex

        if (strategy_types.blacklisting == the_active_strategy_type) {

          final String pattern = line.split(" ")[3]; // TO DO: handle syntax errors more elegantly, allow spaces in IP patterns
          final Matcher m1 = Pattern.compile(IP_pattern_regex).matcher(pattern); // sorry, I know it`s confusing
          final boolean matched_the_regex = m1.find(); // CRUCIAL

          final String pattern_segments[] = new String[4];

          // maybe TO DO here: assert that 4==m1.groupCount()

          // the following "looks wrong" b/c of the offset in the indices, but Don`t Panic
          pattern_segments[0] = m1.group(1);
          pattern_segments[1] = m1.group(2);
          pattern_segments[2] = m1.group(3);
          pattern_segments[3] = m1.group(4);

          final IPv4_pattern new_IPv4_pattern = new IPv4_pattern(); // see how nicely that reads?  ;-)
          for (short index = 0; index < 4; ++index) {
            final String this_pattern_segment = pattern_segments[index];
            if (null == this_pattern_segment)  throw new IOException("In IPv4_client_authorization_engine: an IP pattern segment was somehow found to be a null reference instead of a valid String reference");

            try {
              new_IPv4_pattern.the_pattern[index] = new IPv4_pattern_element(this_pattern_segment); // use the (String) ctor

            } catch (Exception e) {

              if (strictness_level > 0)  throw new IOException("In IPv4_client_authorization_engine: got an exception [" + e + "] while trying to construct an IP pattern segment object; probably the syntax was invalid; location: " + input.get_description_of_input_and_current_position());

              if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: got an exception [" + e + "] while trying to construct an IP pattern segment object; probably the syntax was invalid; will try to overlook this, since strictness_level ≤ 0, and move on to the next line of input; location of source of exception: " + input.get_description_of_input_and_current_position());

              continue;
            } // end try...catch

          } // end for

          if (blacklisted_IP_patterns.contains(new_IPv4_pattern)) {
            if (strictness_level > 1)  throw new IOException("In IPv4_client_authorization_engine: redundant ''blacklist IP pattern'' statement found, unacceptable when "+"strictness level > 1, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
            if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: redundant ''blacklist IP pattern'' statement found, ignored it since " +"strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          } else {
            if (verbosity > 0)  System.err.println("INFO: in IPv4_client_authorization_engine: adding as a blacklist rule the internal-style IP pattern " + new_IPv4_pattern);
          } // end if
          blacklisted_IP_patterns.add(new_IPv4_pattern); // should cause no harm to add it "again" when duplicate

        } else { // _not_ in blacklist mode, so the input was wrong
          if (strictness_level > 0)  throw new IOException("In IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, unacceptable when "+"strictness level > 0, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: a statement was found that was incompatible with the strategy, ignored it since " +"strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
        } // end if

      } else if (line.matches("(?i)whitelist IP pattern " + IP_pattern_regex)) { // cheating a little bit by using a regex

        if (strategy_types.whitelisting == the_active_strategy_type) {

          final String pattern = line.split(" ")[3]; // TO DO: handle syntax errors more elegantly, allow spaces in IP patterns
          final Matcher m1 = Pattern.compile(IP_pattern_regex).matcher(pattern); // sorry, I know it`s confusing
          final boolean matched_the_regex = m1.find(); // CRUCIAL

          final String pattern_segments[] = new String[4];

          // maybe TO DO here: assert that 4==m1.groupCount()

          // the following "looks wrong" b/c of the offset in the indices, but Don`t Panic
          pattern_segments[0] = m1.group(1);
          pattern_segments[1] = m1.group(2);
          pattern_segments[2] = m1.group(3);
          pattern_segments[3] = m1.group(4);

          final IPv4_pattern new_IPv4_pattern = new IPv4_pattern(); // see how nicely that reads?  ;-)
          for (short index = 0; index < 4; ++index) {
            final String this_pattern_segment = pattern_segments[index];
            if (null == this_pattern_segment)  throw new IOException("In IPv4_client_authorization_engine: an IP pattern segment was somehow found to be a null reference instead of a valid String reference");

            try {
              new_IPv4_pattern.the_pattern[index] = new IPv4_pattern_element(this_pattern_segment); // use the (String) ctor

            } catch (Exception e) {

              if (strictness_level > 0)  throw new IOException("In IPv4_client_authorization_engine: got an exception [" + e + "] while trying to construct an IP pattern segment object; probably the syntax was invalid; location: " + input.get_description_of_input_and_current_position());

              if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: got an exception [" + e + "] while trying to construct an IP pattern segment object; probably the syntax was invalid; will try to overlook this, since strictness_level ≤ 0, and move on to the next line of input; location of source of exception: " + input.get_description_of_input_and_current_position());

              continue;
            } // end try...catch

          } // end for


          if (whitelisted_IP_patterns.contains(new_IPv4_pattern)) {
            if (strictness_level > 1)  throw new IOException("In IPv4_client_authorization_engine: redundant ''blacklist IP pattern'' statement found, unacceptable when "+"strictness level > 1, line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
            if (verbosity > 0)  System.err.println( "WARNING: in IPv4_client_authorization_engine: redundant ''blacklist IP pattern'' statement found, ignored it since " +"strictness level ≤ 0; line content [after comment stripping etc.] ''" + line +"'', " + input.get_description_of_input_and_current_position());
          } else {
            if (verbosity > 0)  System.err.println("INFO: in IPv4_client_authorization_engine: adding as a whitelist rule the internal-style IP pattern " + new_IPv4_pattern);
          } // end if
          whitelisted_IP_patterns.add(new_IPv4_pattern); // should cause no harm to add it "again" when duplicate

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
    if (verbosity > 8) {
      System.err.println("INFO: in IPv4_client_authorization_engine, in ''is_connection_from_this_address_authorized'':: got input «" + addr + '»');

      System.err.println("INFO: in IPv4_client_authorization_engine, in ''is_connection_from_this_address_authorized'': addr.isSiteLocalAddress() -> " + addr.isSiteLocalAddress());
      System.err.println("INFO: in IPv4_client_authorization_engine, in ''is_connection_from_this_address_authorized'': addr.isLinkLocalAddress() -> " + addr.isLinkLocalAddress());
      System.err.println("INFO: in IPv4_client_authorization_engine, in ''is_connection_from_this_address_authorized'': addr. isLoopbackAddress() -> " + addr. isLoopbackAddress());
    }

    if (require_siteLocal && ! addr.isSiteLocalAddress())  return false;
    if (require_linkLocal && ! addr.isLinkLocalAddress())  return false;
    if (require_loopback  && ! addr. isLoopbackAddress())  return false;

    switch (the_active_strategy_type) {
      case any_client_that_meets_all_active_requirements:
        if (verbosity > 8)  System.err.println("INFO: in IPv4_client_authorization_engine: in requirements-only mode [''strategy''] and all requirements were met, so returning true");
        return true; // one of the close-by above group of 3 adjacent "if"s would have already returned false if that was the correct answer

      // TO DO if/when I have good working test cases with good coverage for this file of code: DRY-ify the blacklisting-vs.-whitelisting matching code

      case blacklisting:

        // first, the easy part: the textual matching
        { // an unpredicated inner scope to hide _this_ "FQDN" variable from the one in the whitelisting-processing code block
          final String FQDN = addr.getCanonicalHostName();
          for (String pattern : blacklisted_FQDN_patterns) {
            if (verbosity > 8)  System.err.print("INFO: in IPv4_client_authorization_engine: comparing pattern ''" + pattern + "'' with FQDN ''" + FQDN + "''... ");
            final boolean it_matched = FQDN.matches(pattern);
            if (verbosity > 8)  System.err.println(" it_matched = " + it_matched);
            if (it_matched)  return true;
          } // end for
        }

        for (IPv4_pattern the_IP_pattern : blacklisted_IP_patterns) {
          final byte[] IP_addr_as_byte_array = addr.getAddress(); // TO DO: check this has the right length, throw/WARN if not

          if (verbosity > 8)  System.err.println("INFO: in IPv4_client_authorization_engine: considering the IPv4 pattern " + the_IP_pattern);
          if (the_IP_pattern.matches(IP_addr_as_byte_array)) {
            if (verbosity > 8)  System.err.println("INFO: in IPv4_client_authorization_engine: returning false because a rule matched.");
            return false;
          } // end if
        } // end for

        if (verbosity > 8)  System.err.println("INFO: in IPv4_client_authorization_engine: returning true in blacklisting mode because no rules matched.");
        return true; // the default answer when blacklisting


      case whitelisting:

        // first, the easy part: the textual matching
        final String FQDN = addr.getCanonicalHostName();
        for (String pattern : whitelisted_FQDN_patterns) {
          if (verbosity > 8)  System.err.print("INFO: in IPv4_client_authorization_engine: comparing pattern ''" + pattern + "'' with FQDN ''" + FQDN + "''... ");
          final boolean it_matched = FQDN.matches(pattern);
          if (verbosity > 8)  System.err.println(" it_matched = " + it_matched);
          if (it_matched)  return true;
        } // end for

        for (IPv4_pattern the_IP_pattern : whitelisted_IP_patterns) {
          final byte[] IP_addr_as_byte_array = addr.getAddress(); // TO DO: check this has the right length, throw/WARN if not

          if (verbosity > 8)  System.err.println("INFO: in IPv4_client_authorization_engine: considering the IPv4 pattern " + the_IP_pattern);
          if (the_IP_pattern.matches(IP_addr_as_byte_array)) {
            if (verbosity > 8)  System.err.println("INFO: in IPv4_client_authorization_engine: returning true because a rule matched.");
            return true;
          } // end if
        } // end for

        if (verbosity > 8)  System.err.println("INFO: in IPv4_client_authorization_engine: returning false in whitelisting mode because no rules matched.");
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
